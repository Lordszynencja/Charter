package log.charter.sound.system.data;

import static java.lang.Math.min;

import java.util.function.DoubleSupplier;

import javax.sound.sampled.AudioFormat;

import com.breakfastquay.rubberband.RubberBandStretcher;

import log.charter.data.config.values.AudioConfig;
import log.charter.io.Logger;
import log.charter.sound.data.AudioData;
import log.charter.sound.data.FloatQueue;
import log.charter.sound.effects.Effect;
import log.charter.sound.system.SoundSystem;
import log.charter.sound.utils.FloatSamplesUtils;

public class Player {
	private final AudioData musicData;
	private final AudioFormat audioFormat;
	private final DoubleSupplier volume;
	private final int speed;

	private final ISoundLine line;
	private Thread thread = null;
	private boolean stopped = false;

	private final Effect effect;
	private final RubberBandStretcher rubberBandStretcher;
	private final FloatQueue[] stretchingSamplesQueue;

	public long playingStartTime = -1;

	public Player(final AudioData musicData, final DoubleSupplier volume, final int speed, final Effect effect) {
		this.musicData = musicData;
		audioFormat = musicData.getPlayingFormat();
		this.volume = volume;
		this.speed = speed;

		this.effect = effect;
		if (speed == 100 || !RubberBandStretcher.loaded) {
			rubberBandStretcher = null;
			stretchingSamplesQueue = null;
		} else {
			rubberBandStretcher = createStretcher();
			stretchingSamplesQueue = new FloatQueue[2];
			for (int i = 0; i < 2; i++) {
				stretchingSamplesQueue[i] = new FloatQueue(rubberBandStretcher.getSamplesRequired(), 1, 1);
			}
		}

		final ISoundSystem soundSystem = SoundSystem.getCurrentSoundSystem();
		line = soundSystem.getNewLine(audioFormat);
	}

	private RubberBandStretcher createStretcher() {
		try {
			return new RubberBandStretcher((int) audioFormat.getSampleRate(), audioFormat.getChannels(),
					RubberBandStretcher.OptionProcessRealTime //
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

	private int getStartFrame(final double startTime) {
		return (int) (musicData.getPlayingFormat().getFrameRate() * startTime / 1000);
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
		if (rubberBandStretcher == null) {
			return samples;
		}

		for (int i = 0; i < 2; i++) {
			for (final float sample : samples[i]) {
				try {
					stretchingSamplesQueue[i].add(sample);
				} catch (final Exception e) {
					Logger.error("Error when trying to add samples to the queue", e);
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
				Logger.error("Error when trying to take samples from the queue", e);
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

	private void writeBuffer(final byte[] buffer, final boolean lastBlock) {
		final int channels = audioFormat.getChannels();
		final int sampleSize = audioFormat.getSampleSizeInBits() / 8;

		float[][] samples = FloatSamplesUtils.splitStereoAudioFloat(buffer, sampleSize, channels);
		applyEffect(samples);
		setVolume(samples);
		if (rubberBandStretcher != null) {
			samples = stretch(samples, lastBlock);
		}

		line.write(FloatSamplesUtils.toBytes(samples, channels, sampleSize));
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

	private void playSound(int startFrame) throws InterruptedException {
		final int frames = musicData.frames();
		final byte[] buffer = musicData.generatePlayingBuffer(AudioConfig.bufferSize);

		while (startFrame < frames) {
			if (stopped) {
				if (playingStartTime < 0) {
					playingStartTime = System.nanoTime();
				}

				return;
			}

			startFrame += musicData.fillPlayingBuffer(startFrame, buffer);
			final boolean lastBlock = startFrame >= frames;
			writeBuffer(buffer, lastBlock);

			if (playingStartTime < 0) {
				playingStartTime = System.nanoTime() + AudioConfig.delay * 1_000_000;
			}
			waitIfNeeded();
		}

		finishPlaying();
	}

	public Player start(final double startTime, final int id) {
		if (thread != null) {
			return this;
		}

		thread = new Thread(() -> {
			try {
				playSound(getStartFrame(startTime));
			} catch (final InterruptedException e) {
				Logger.error("playing audio interrupted", e);
				line.stop();
			}
		});
		thread.setName("Sound thread #" + id);
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