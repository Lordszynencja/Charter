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
import log.charter.sound.system.SoundSystem.ISoundSystem.ISoundLine;

public class SoundSystem {
	private static int playerId = 0;

	public interface ISoundSystem {
		public interface ISoundLine {
			public int write(byte[] bytes);

			void close();

			void stop();

			boolean wantsMoreData();
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

		}

		ISoundLine getNewLine(AudioFormat format);
	}

	private static ISoundSystem currentSoundSystem = new StandardSoundSystem();

	public static void setCurrentSoundSystem() {
		currentSoundSystem = switch (Config.audioSystemType) {
			case ASIO -> new ASIOSoundSystem();
			default -> new StandardSoundSystem();
		};

		ASIOHandler.refresh();
	}

	public static ISoundSystem getCurrentSoundSystem() {
		return currentSoundSystem;
	}

	public static class Player {
		private final AudioData<?> musicData;
		private final DoubleSupplier volume;
		private final int speed;

		private final int sampleSizeInBits;
		@SuppressWarnings("unused")
		private final int sampleSize;
		private final int frameSize;
		private final ISoundLine line;
		private boolean stopped;

		private final RubberBandStretcher rubberBandStretcher;

		public long playingStartTime = -1;

		private Player(final AudioData<?> musicData, final DoubleSupplier volume, final int speed) {
			this.musicData = musicData;
			this.volume = volume;
			this.speed = speed;
			rubberBandStretcher = new RubberBandStretcher((int) musicData.sampleRate(), musicData.channels(),
					RubberBandStretcher.OptionProcessRealTime //
							| RubberBandStretcher.OptionTransientsSmooth//
							| RubberBandStretcher.OptionThreadingNever//
							| RubberBandStretcher.OptionPitchHighQuality//
							| RubberBandStretcher.OptionStretchPrecise//
							| RubberBandStretcher.OptionPhaseIndependent, //
					100f / speed, 1.0);

			sampleSizeInBits = musicData.format().getSampleSizeInBits();
			if (sampleSizeInBits <= 8) {
				sampleSize = 1;
			} else {
				sampleSize = 2;
			}
			frameSize = musicData.format().getFrameSize();

			final ISoundSystem soundSystem = getCurrentSoundSystem();
			line = soundSystem.getNewLine(musicData.format());
		}

		public boolean isStopped() {
			return stopped;
		}

		private int getStartByte(final double startTime) {
			final int startFrame = (int) (musicData.frameRate() * startTime / 1000);
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

		private float[][] stretch(final float[][] samples) {
			if (speed == 100) {
				return samples;
			}

			boolean lastBlock = false;
			if (samples[0].length < audioBufferSize) {
				lastBlock = true;
			}

			final int samplesRequired = rubberBandStretcher.getSamplesRequired();
			if (samples[0].length < samplesRequired) {
				return samples;
			}

			rubberBandStretcher.process(samples, lastBlock);

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

		private int writeBuffer(final byte[] data, int startByte) {
			final byte[] buffer = Arrays.copyOfRange(data, startByte,
					min(data.length, startByte + audioBufferSize * 4));

			final float[][] samples = splitStereoAudioFloat(buffer);
			setVolume(samples);
			final float[][] stretchedSamples = stretch(samples);

			startByte += buffer.length;
			line.write(AudioUtils.toBytes(stretchedSamples, 2, 2));
			return startByte;
		}

		private void waitIfNeeded() throws InterruptedException {
			while (!line.wantsMoreData()) {
				Thread.sleep(1);
			}
		}

		private void finishPlaying() {
			line.close();
			stopped = true;
		}

		private void playSound(int startByte) throws InterruptedException {
			final byte[] data = musicData.getBytes();

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

	public static Player play(final AudioData<?> audioData, final DoubleSupplier volumeSupplier, final int speed) {
		return play(audioData, volumeSupplier, speed, 0);
	}

	public static Player play(final AudioData<?> audioData, final DoubleSupplier volumeSupplier, final int speed,
			final double startTime) {
		return new Player(audioData, volumeSupplier, speed).start(startTime);
	}
}