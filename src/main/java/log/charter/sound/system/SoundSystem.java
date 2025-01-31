package log.charter.sound.system;

import static java.lang.Math.min;
import static log.charter.data.config.Config.audioBufferSize;
import static log.charter.sound.data.AudioUtils.splitStereoAudioFloat;

import java.util.Arrays;
import java.util.function.DoubleSupplier;

import javax.sound.sampled.AudioFormat;

import com.breakfastquay.rubberband.RubberBandStretcher;

import log.charter.data.config.Config;
import log.charter.io.Logger;
import log.charter.sound.asio.ASIOHandler;
import log.charter.sound.data.AudioData;
import log.charter.sound.data.AudioUtils;
import log.charter.sound.data.FloatQueue;
import log.charter.sound.effects.Effect;
import log.charter.sound.system.SoundSystem.ISoundSystem.ISoundLine;

public class SoundSystem {
	private static int playerId = 0;

	public interface ISoundSystem {
		public interface ISoundLine {
			public int write(byte[] bytes);

			void close();

			void stop();

			boolean wantsMoreData();

			boolean stopped();
		}

		public static class EmptySoundLine implements ISoundLine {
			@Override
			public int write(final byte[] bytes) {
				return bytes.length;
			}

			@Override
			public void close() {
			}

			@Override
			public void stop() {
			}

			@Override
			public boolean wantsMoreData() {
				return true;
			}

			@Override
			public boolean stopped() {
				return true;
			}

		}

		ISoundLine getNewLine(AudioFormat format);
	}

	private static ISoundSystem currentSoundSystem = new StandardSoundSystem();

	public static void setCurrentSoundSystem() {
		currentSoundSystem = switch (Config.audioOutSystemType) {
			case ASIO -> new ASIOSoundSystem();
			default -> new StandardSoundSystem();
		};

		ASIOHandler.refresh();
	}

	public static ISoundSystem getCurrentSoundSystem() {
		return currentSoundSystem;
	}

	public static class Player {
		private final AudioData musicData;
		private final DoubleSupplier volume;
		private final int speed;

		private final int frameSize;
		private final ISoundLine line;
		private boolean stopped;

		private final Effect effect;
		private final RubberBandStretcher rubberBandStretcher;
		private final FloatQueue[] stretchingSamplesQueue;

		public long playingStartTime = -1;

		private Player(final AudioData musicData, final DoubleSupplier volume, final int speed, final Effect effect) {
			this.musicData = musicData;
			this.volume = volume;
			this.speed = speed;

			frameSize = musicData.playingFormat.getFrameSize();

			this.effect = effect;
			if (speed == 100) {
				rubberBandStretcher = null;
				stretchingSamplesQueue = null;
			} else {
				rubberBandStretcher = createStretcher();
				stretchingSamplesQueue = new FloatQueue[2];
				for (int i = 0; i < 2; i++) {
					stretchingSamplesQueue[i] = new FloatQueue(rubberBandStretcher.getSamplesRequired(), 1, 1);
				}
			}

			final ISoundSystem soundSystem = getCurrentSoundSystem();
			line = soundSystem.getNewLine(musicData.playingFormat);
		}

		private RubberBandStretcher createStretcher() {
			try {
				return new RubberBandStretcher((int) musicData.playingFormat.getSampleRate(),
						musicData.playingFormat.getChannels(), RubberBandStretcher.OptionProcessRealTime //
								| RubberBandStretcher.OptionTransientsSmooth//
								| RubberBandStretcher.OptionThreadingNever//
								| RubberBandStretcher.OptionPitchHighQuality//
								| RubberBandStretcher.OptionStretchPrecise//
								| RubberBandStretcher.OptionPhaseIndependent, //
						100f / speed, 1.0);
			} catch (final Throwable e) {
				Logger.error("Couldn't create audio stretcher", e);
			}

			return null;
		}

		public boolean isStopped() {
			return stopped;
		}

		private int getStartByte(final double startTime) {
			final int startFrame = (int) (musicData.playingFormat.getFrameRate() * startTime / 1000);
			return startFrame * frameSize;
		}

		private void setVolume(final float[][] samples) {
			final float volumeValue = volume == null ? 0.5f : (float) volume.getAsDouble() * 0.5f;

			for (int channel = 0; channel < samples.length; channel++) {
				for (int i = 0; i < samples[channel].length; i++) {
					samples[channel][i] *= volumeValue;
				}
			}
		}

		private void applyEffect(final float[][] samples) {
			for (int channel = 0; channel < samples.length; channel++) {
				for (int i = 0; i < samples[channel].length; i++) {
					samples[channel][i] = effect.apply(channel, samples[channel][i]);
				}
			}
		}

		private float[][] stretch(final float[][] samples, final boolean lastBlock) {
			if (speed == 100) {
				return samples;
			}

			for (int i = 0; i < 2; i++) {
				for (final float sample : samples[i]) {
					try {
						stretchingSamplesQueue[i].add(sample);
					} catch (final Exception e) {
					}
				}
			}
			if (lastBlock) {
				for (int i = 0; i < 2; i++) {
					stretchingSamplesQueue[i].close();
				}
			}

			if (!stretchingSamplesQueue[0].bufferAvailable()) {
				return new float[2][0];
			}

			final float[][] stretchingSamples = new float[2][];
			for (int i = 0; i < 2; i++) {
				try {
					stretchingSamples[i] = stretchingSamplesQueue[i].take();
				} catch (final InterruptedException e) {
				}
			}
			rubberBandStretcher.process(stretchingSamples, lastBlock);

			int available = 0;
			while (available == 0) {
				available = rubberBandStretcher.available();
			}

			final float[][] output = new float[2][available];
			rubberBandStretcher.retrieve(output);
			for (final float[] channel : output) {
				for (int i = 0; i < channel.length; i++) {
					channel[i] = Math.max(-1f, min(1f, channel[i] * 2f));
				}
			}

			return output;
		}

		private int writeBuffer(final byte[] data, final int startByte) {
			final int length = audioBufferSize * speed / 100 * 4;

			int endByte = startByte + length;
			boolean lastBlock = false;
			if (endByte >= data.length) {
				endByte = data.length;
				lastBlock = true;
			}

			final byte[] buffer = Arrays.copyOfRange(data, startByte, endByte);

			float[][] samples = splitStereoAudioFloat(buffer);
			applyEffect(samples);
			setVolume(samples);

			if (rubberBandStretcher != null) {
				samples = stretch(samples, lastBlock);
			}
			line.write(AudioUtils.toBytes(samples, 2, 2));

			return endByte;
		}

		private void waitIfNeeded() throws InterruptedException {
			while (!line.stopped() && !line.wantsMoreData()) {
				Thread.sleep(0, 10_000);
			}
		}

		private void finishPlaying() {
			line.close();
			stopped = true;
		}

		private void playSound(int startByte) throws InterruptedException {
			final byte[] data = musicData.playingBytes;

			while (startByte < data.length) {
				if (stopped) {
					if (playingStartTime < 0) {
						playingStartTime = System.nanoTime();
					}

					return;
				}

				startByte = writeBuffer(data, startByte);

				if (playingStartTime < 0) {
					playingStartTime = System.nanoTime() + Config.delay * 1_000_000;
				}
				waitIfNeeded();
			}

			finishPlaying();
		}

		private Player start(final double startTime) {
			final Thread thread = new Thread(() -> {
				try {
					playSound(getStartByte(startTime));
				} catch (final InterruptedException e) {
					Logger.error("playing audio interrupted", e);
					line.stop();
				}
			});
			thread.setName("Sound thread " + playerId++);
			thread.setPriority(Thread.MAX_PRIORITY);
			thread.start();

			return this;
		}

		public void stop() {
			if (stopped) {
				return;
			}

			stopped = true;
			line.stop();
		}
	}

	public static Player play(final AudioData audioData, final DoubleSupplier volumeSupplier, final int speed) {
		return play(audioData, volumeSupplier, speed, 0, Effect.emptyEffect);
	}

	public static Player play(final AudioData audioData, final DoubleSupplier volumeSupplier, final int speed,
			final double startTime, final Effect effect) {
		return new Player(audioData, volumeSupplier, speed, effect).start(startTime);
	}
}