package log.charter.sound.system;

import static java.lang.Math.pow;
import static log.charter.sound.data.AudioUtils.setChannels;
import static log.charter.sound.data.AudioUtils.splitAudioShort;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;

import log.charter.io.Logger;
import log.charter.sound.asio.ASIOHandler;
import log.charter.sound.data.FloatQueue;
import log.charter.sound.system.data.EmptySoundLine;
import log.charter.sound.system.data.ISoundLine;
import log.charter.sound.system.data.ISoundSystem;

public class ASIOSoundSystem implements ISoundSystem {
	public class ASIOSoundLine implements ISoundLine {
		private final AudioFormat format;
		private final FloatQueue[] channels;
		private boolean stopped = false;

		private ASIOSoundLine(final AudioFormat format) throws LineUnavailableException {
			this.format = format;

			channels = new FloatQueue[2];
			channels[0] = ASIOHandler.createLeftChannelStream(format.getSampleRate());
			channels[1] = ASIOHandler.createRightChannelStream(format.getSampleRate());
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
						this.channels[channel].add(floatAudioData[channel]);
					}
				}
			} catch (final Exception e) {
				Logger.error("Error when writing bytes", e);
			}

			return written;
		}

		@Override
		public boolean wantsMoreData() {
			return channels[0].available() < ASIOHandler.getDesiredFill();
		}

		@Override
		public void close() {
			stopped = true;

			for (int i = 0; i < channels.length; i++) {
				channels[i].close();
			}

			try {
				for (int i = 0; i < channels.length; i++) {
					while (channels[i].bufferAvailable()) {
						Thread.sleep(10);
					}
				}
			} catch (final InterruptedException e) {
				Logger.error("Error when waiting for buffers to be available", e);
			}
		}

		@Override
		public void stop() {
			stopped = true;

			for (int i = 0; i < channels.length; i++) {
				channels[i].close();
			}
		}

		@Override
		public boolean stopped() {
			return stopped;
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
