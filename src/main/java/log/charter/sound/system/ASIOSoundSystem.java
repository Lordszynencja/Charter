package log.charter.sound.system;

import static java.lang.Math.pow;
import static log.charter.sound.data.AudioUtils.setChannels;
import static log.charter.sound.data.AudioUtils.splitAudioShort;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;

import com.synthbot.jasiohost.AsioChannel;
import com.synthbot.jasiohost.AsioDriver;
import com.synthbot.jasiohost.AsioDriverListener;

import log.charter.io.Logger;
import log.charter.sound.system.SoundSystem.ISoundSystem;

public class ASIOSoundSystem implements ISoundSystem {
	private static class FloatBufferQueue {
		private final List<float[]> buffers = new LinkedList<>();
		private float[] buffer;
		private int position = 0;

		public FloatBufferQueue(final int size) {
			if (size <= 0) {
				throw new IllegalArgumentException("Size must be positive");
			}

			buffer = new float[size];
		}

		public int available() {
			return buffers.size() * buffer.length;
		}

		public boolean bufferAvailable() {
			return !buffers.isEmpty();
		}

		public void add(final float f) throws InterruptedException {
			buffer[position++] = f;
			if (position >= buffer.length) {
				synchronized (buffers) {
					buffers.add(buffer);
				}

				buffer = new float[buffer.length];
				position = 0;
			}
		}

		public float[] take() throws InterruptedException {
			synchronized (buffers) {
				return buffers.remove(0);
			}
		}

		public void finish() {
			synchronized (buffers) {
				buffers.add(buffer);
			}
		}
	}

	public class ASIOSoundLine implements ISoundLine {
		private int bufferSize = 128;
		private final AudioFormat format;
		private final AsioDriver asioDriver;
		private final AsioChannel[] channels;
		private final FloatBufferQueue[] queues;

		private ASIOSoundLine(final AudioFormat format) throws LineUnavailableException {
			this.format = format;

			// get a list of available ASIO drivers
//			final List<String> driverNameList = AsioDriver.getDriverNames();

			// load the names ASIO driver
			asioDriver = AsioDriver.getDriver("Focusrite USB ASIO");

			// add an AsioDriverListener in order to receive callbacks from the driver
			asioDriver.addAsioDriverListener(new AsioDriverListener() {
				@Override
				public void bufferSwitch(final long systemTime, final long samplePosition,
						final Set<AsioChannel> channelsSet) {
					for (final FloatBufferQueue queue : queues) {
						if (!queue.bufferAvailable()) {
							return;
						}
					}

					try {
						for (int channel = 0; channel < channels.length; channel++) {
							final float[] buffer = queues[channel].take();
							channels[channel].write(buffer);
						}
					} catch (final Exception e) {
					}
				}

				@Override
				public void sampleRateDidChange(final double sampleRate) {
					// TODO Auto-generated method stub

				}

				@Override
				public void resetRequest() {
					// TODO Auto-generated method stub

				}

				@Override
				public void resyncRequest() {
					// TODO Auto-generated method stub

				}

				@Override
				public void bufferSizeChanged(final int newBufferSize) {
					bufferSize = newBufferSize;
				}

				@Override
				public void latenciesChanged(final int inputLatency, final int outputLatency) {
				}
			});

			channels = new AsioChannel[2];
			channels[0] = asioDriver.getChannelOutput(0);
			channels[1] = asioDriver.getChannelOutput(1);
			bufferSize = asioDriver.getBufferPreferredSize();

			queues = new FloatBufferQueue[2];
			for (int i = 0; i < 2; i++) {
				queues[i] = new FloatBufferQueue(bufferSize);
			}

			asioDriver.createBuffers(new HashSet<>(Set.of(channels)));

			asioDriver.start();
		}

		private float[][] transformToFloat(final short[][] audioData) {
			final double multiplier = pow(2, format.getSampleSizeInBits());

			final float[][] newData = new float[2][];
			for (int i = 0; i < 2; i++) {
				newData[i] = new float[audioData[i].length];
				for (int j = 0; j < audioData[i].length; j++) {
					newData[i][j] = (float) (audioData[i][j] / multiplier);
				}
			}

			return newData;
		}

		@Override
		public int write(final byte[] bytes) {
			int written = 0;
			try {
				final int channels = format.getChannels();
				final int sampleSize = format.getSampleSizeInBits() / 8;
				final short[][] audioData = setChannels(splitAudioShort(bytes, channels, sampleSize), 2);
				final float[][] floatAudioData = transformToFloat(audioData);

				for (int frame = 0; frame < floatAudioData[0].length; frame++) {
					written += channels * sampleSize;
					for (int channel = 0; channel < this.channels.length; channel++) {
						queues[channel].add(floatAudioData[channel][frame]);
					}
				}
			} catch (final InterruptedException e) {
			}

			return written;
		}

		@Override
		public boolean wantsMoreData() {
			return queues[0].available() < bufferSize * 2;
		}

		@Override
		public void close() {
			for (int i = 0; i < queues.length; i++) {
				queues[i].finish();
			}

			try {
				for (int i = 0; i < queues.length; i++) {
					while (queues[i].bufferAvailable()) {
						Thread.sleep(10);
					}
				}
			} catch (final InterruptedException e) {
			}

			asioDriver.shutdownAndUnloadDriver();
		}

		@Override
		public void stop() {
			asioDriver.shutdownAndUnloadDriver();
		}
	}

	@Override
	public ISoundLine getNewLine(final AudioFormat format) {
		try {
			return new ASIOSoundLine(format);
		} catch (final LineUnavailableException e) {
			Logger.error("Couldn't open standard line", e);
			return new EmptySoundLine();
		}
	}

}
