package log.charter.sound.asio;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.synthbot.jasiohost.AsioChannel;
import com.synthbot.jasiohost.AsioDriver;
import com.synthbot.jasiohost.AsioDriverListener;

import log.charter.data.config.Config;
import log.charter.io.Logger;
import log.charter.sound.data.AudioUtils;
import log.charter.sound.data.FloatMixer;
import log.charter.sound.data.FloatQueue;
import log.charter.sound.effects.Distortion;
import log.charter.sound.effects.Effect;
import log.charter.sound.effects.Tremolo;
import log.charter.util.fft.FFTTest;

public class ASIOHandler {
	public static final int inputCopyBufferSize = 1024;

	private static int bufferSize = 128;
	private static float sampleRate = AudioUtils.DEF_RATE;
	private static int desiredFill = 1024;

	private static AsioDriver asioDriver;
	private static AsioChannel[] inputChannels;

	public static float[] inputCopyBuffer = new float[inputCopyBufferSize];
	public static FFTTest fft = new FFTTest();

	private static AsioChannel[] outputChannels;
	private static FloatMixer[] mixChannels;

	public static int getBufferSize() {
		return bufferSize;
	}

	public static float getSampleRate() {
		return sampleRate;
	}

	public static int getDesiredFill() {
		return desiredFill;
	}

	private static void addInputToCopy(final float[] buffer) {
		final float[] newCopy = Arrays.copyOfRange(inputCopyBuffer, buffer.length, buffer.length + inputCopyBufferSize);
		System.arraycopy(buffer, 0, newCopy, inputCopyBufferSize - buffer.length, buffer.length);
		inputCopyBuffer = newCopy;
	}

	static Effect silencer = new Effect() {

		@Override
		public float apply(final int channel, final float sample) {
			return (float) (sample - Math.pow(sample, 3));
		}
	};
	static Effect tremolo = new Tremolo(sampleRate / 4);
	static Effect arctanDistortion = new Effect() {

		@Override
		public float apply(final int channel, final float sample) {
			return (float) ((2 / Math.PI) * Math.atan(sample * 10));
		}
	};
	static Effect softClipDistortion = new Effect() {
		@Override
		public float apply(final int channel, final float sample) {
			return (float) (sample - Math.pow(sample, 3) / 3 - Math.pow(sample, 5) / 5 - Math.pow(sample, 7) / 7);
		}
	};

	private static FloatQueue inToOutQueue0;
	private static FloatQueue inToOutQueue1;

	private static void createInToOutQueues() {
		if (inToOutQueue0 != null) {
			inToOutQueue0.close();
		}
		inToOutQueue0 = mixChannels[0].generateQueue(1);

		if (inToOutQueue1 != null) {
			inToOutQueue1.close();
		}
		inToOutQueue1 = mixChannels[1].generateQueue(1);
	}

	private static void addEffectsAndSendToOutput(final float[] buffer) throws Exception {
		if (inToOutQueue0.available() > bufferSize * 2) {
			inToOutQueue0.take();
		}
		if (inToOutQueue1.available() > bufferSize * 2) {
			inToOutQueue1.take();
		}

		final Effect distortion = new Distortion(0);

		for (final float f : buffer) {
			float sample = f;
			// sample = sample > 1 ? 1 : sample < -1 ? -1 : sample;
			sample = sample * 4f;
			// sample = distortion.apply(0, sample);
			// sample = tremolo.apply(0, sample);
			// sample = arctanDistortion.apply(0, sample);
			// sample = softClipDistortion.apply(0, sample);

			sample = sample > 1 ? 1 : sample < -1 ? -1 : sample;
			sample = sample * 0.125f;

			inToOutQueue0.add(sample);
			inToOutQueue1.add(sample);
		}
	}

	private static void handleInput(final float[] buffer) throws Exception {
		addInputToCopy(buffer);
		fft.addData(buffer);
		addEffectsAndSendToOutput(buffer);
	}

	private static void regenerateInputQueue() {
		// createInToOutQueues();
		mixChannels = new FloatMixer[2];
		mixChannels[0] = new FloatMixer(bufferSize);
		mixChannels[1] = new FloatMixer(bufferSize);
	}

	private static void setDriver() {
		if (asioDriver != null) {
			asioDriver.shutdownAndUnloadDriver();
			asioDriver = null;
		}

		if (Config.audioOutSystemName != null) {
			asioDriver = AsioDriver.getDriver(Config.audioOutSystemName);
		}
	}

	private static boolean outputReady() {
		for (final FloatMixer mixChannel : mixChannels) {
			if (!mixChannel.hasNextBuffer()) {
				return false;
			}
		}

		return true;
	}

	private static void setDriverListener() {
		asioDriver.addAsioDriverListener(new AsioDriverListener() {
			@Override
			public void bufferSwitch(final long systemTime, final long samplePosition,
					final Set<AsioChannel> channelsSet) {
				try {
					if (Config.specialDebugOption) {
						// final float[] newInput = new float[bufferSize];
						// inputChannels[0].read(newInput);
						// handleInput(newInput);
					}

					if (!outputReady()) {
						for (int channel = 0; channel < outputChannels.length; channel++) {
							outputChannels[channel].write(new float[bufferSize]);
						}

						return;
					}

					for (int channel = 0; channel < outputChannels.length; channel++) {
						outputChannels[channel].write(mixChannels[channel].getNextBuffer());
					}
				} catch (final Exception e) {
					Logger.error("Error in ASIO driver listener buffer switching", e);
				}
			}

			@Override
			public void sampleRateDidChange(final double sampleRate) {
				ASIOHandler.sampleRate = (float) sampleRate;
				desiredFill = (int) (Config.audioBufferMs * sampleRate / 1000);
				regenerateInputQueue();
			}

			@Override
			public void resetRequest() {
			}

			@Override
			public void resyncRequest() {
			}

			@Override
			public void bufferSizeChanged(final int newBufferSize) {
				bufferSize = newBufferSize;
				regenerateInputQueue();
			}

			@Override
			public void latenciesChanged(final int inputLatency, final int outputLatency) {
			}
		});
	}

	private static void setInputChannels() {
		inputChannels = new AsioChannel[1];
		inputChannels[0] = asioDriver.getChannelInput(Config.inChannel0Id);
	}

	private static void setOutputChannels() {
		outputChannels = new AsioChannel[2];
		outputChannels[0] = asioDriver.getChannelOutput(Config.leftOutChannelId);
		outputChannels[1] = asioDriver.getChannelOutput(Config.rightOutChannelId);

		mixChannels = new FloatMixer[2];
		mixChannels[0] = new FloatMixer(bufferSize);
		mixChannels[1] = new FloatMixer(bufferSize);
	}

	public static void refresh() {
		setDriver();
		if (asioDriver == null) {
			return;
		}

		setDriverListener();
		if (Config.specialDebugOption) {
			setInputChannels();
		}
		setOutputChannels();

		bufferSize = asioDriver.getBufferPreferredSize();
		desiredFill = (int) (Config.audioBufferMs * asioDriver.getSampleRate() / 1000);
		sampleRate = (float) asioDriver.getSampleRate();

		final Set<AsioChannel> channels = new HashSet<>(Set.of(outputChannels));
		if (Config.specialDebugOption) {
			channels.addAll(Set.of(inputChannels));
		}
		asioDriver.createBuffers(channels);

		asioDriver.start();
		regenerateInputQueue();
	}

	public static FloatQueue createLeftChannelStream(final float sampleRate) {
		return mixChannels[0].generateQueue(sampleRate / ASIOHandler.sampleRate);
	}

	public static FloatQueue createRightChannelStream(final float sampleRate) {
		return mixChannels[1].generateQueue(sampleRate / ASIOHandler.sampleRate);
	}
}
