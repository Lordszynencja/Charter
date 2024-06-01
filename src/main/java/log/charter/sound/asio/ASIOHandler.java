package log.charter.sound.asio;

import java.util.HashSet;
import java.util.Set;

import com.synthbot.jasiohost.AsioChannel;
import com.synthbot.jasiohost.AsioDriver;
import com.synthbot.jasiohost.AsioDriverListener;

import log.charter.data.config.Config;
import log.charter.sound.data.FloatMixer;
import log.charter.sound.data.FloatQueue;
import log.charter.util.fft.FFTTest;

public class ASIOHandler {
	private static int bufferSize = 128;
	private static double sampleRate = 44100;
	private static int desiredFill = 1024;

	private static AsioDriver asioDriver;
	private static AsioChannel[] inputChannels;

	public static float[] inputBuffer = new float[bufferSize];
	public static FFTTest fft = new FFTTest();

	private static AsioChannel[] outputChannels;
	private static FloatMixer[] mixChannels;

	public static int getBufferSize() {
		return bufferSize;
	}

	public static double getSampleRate() {
		return sampleRate;
	}

	public static int getDesiredFill() {
		return desiredFill;
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

	private static void setDriverListener() {
		asioDriver.addAsioDriverListener(new AsioDriverListener() {
			@Override
			public void bufferSwitch(final long systemTime, final long samplePosition,
					final Set<AsioChannel> channelsSet) {
				try {
					for (int channel = 0; channel < outputChannels.length; channel++) {
						outputChannels[channel].write(mixChannels[channel].getNextBuffer());
					}

					final float[] newInput = new float[bufferSize];
					inputChannels[0].read(newInput);
					inputBuffer = newInput;

					fft.addData(newInput);
				} catch (final Exception e) {
				}
			}

			@Override
			public void sampleRateDidChange(final double sampleRate) {
				ASIOHandler.sampleRate = sampleRate;
				desiredFill = (int) (Config.audioBufferMs * sampleRate / 1000);
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
		mixChannels[0] = new FloatMixer();
		mixChannels[1] = new FloatMixer();
	}

	public static void refresh() {
		setDriver();
		if (asioDriver == null) {
			return;
		}

		setDriverListener();
		setInputChannels();
		setOutputChannels();

		bufferSize = asioDriver.getBufferPreferredSize();
		desiredFill = (int) (Config.audioBufferMs * asioDriver.getSampleRate() / 1000);
		sampleRate = asioDriver.getSampleRate();

		final Set<AsioChannel> channels = new HashSet<>(Set.of(outputChannels));
		channels.addAll(Set.of(inputChannels));
		asioDriver.createBuffers(channels);

		asioDriver.start();
	}

	public static FloatQueue createLeftChannelStream(final int sampleRate) {
		return mixChannels[0].generateQueue(sampleRate);
	}

	public static FloatQueue createRightChannelStream(final int sampleRate) {
		return mixChannels[1].generateQueue(sampleRate);
	}
}
