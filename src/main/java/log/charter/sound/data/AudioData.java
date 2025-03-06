package log.charter.sound.data;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.io.File;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;

import log.charter.io.Logger;
import log.charter.sound.SoundFileType;
import log.charter.sound.utils.FloatSamplesUtils;
import log.charter.sound.utils.IntSampleUtils;

public class AudioData {
	public static int getMin(final int sampleSize) {
		return AudioUtils.spans[max(0, min(3, sampleSize - 1))][0];
	}

	public static int getMax(final int sampleSize) {
		return AudioUtils.spans[max(0, min(3, sampleSize - 1))][1];
	}

	public static int[][] shortToInt(final short[][] data, final int sampleSize) {
		final int[][] newSamples = new int[data.length][];
		for (int channel = 0; channel < data.length; channel++) {
			final short[] channelSamples = data[channel];
			final int[] newChannelSamples = new int[channelSamples.length];

			for (int i = 0; i < channelSamples.length; i++) {
				newChannelSamples[i] = channelSamples[i];
			}

			newSamples[channel] = newChannelSamples;
		}

		return newSamples;
	}

	public static AudioData readFile(final File file) {
		try {
			for (final SoundFileType fileType : SoundFileType.values()) {
				if (file.getName().endsWith("." + fileType.extension)) {
					return fileType.loader.apply(file);
				}
			}
		} catch (final Exception e) {
			Logger.error("Couldn't load file " + file, e);
		}

		return null;
	}

	public final byte[] data;
	public final AudioFormat format;

	public AudioData(final int[][] data, final float sampleRate, final int sampleSize) {
		this(IntSampleUtils.writeSamples(data, data.length, sampleSize), sampleRate, sampleSize, data.length);
	}

	public AudioData(final byte[] data, final float sampleRate, final int sampleSize, final int channels) {
		this.data = data;
		format = new AudioFormat(Encoding.PCM_SIGNED, sampleRate, sampleSize * 8, channels, channels * sampleSize,
				sampleRate, false);
	}

	public int frames() {
		return data.length / max(1, format.getFrameSize());
	}

	public float getSample(final int frame, final int channel) {
		return FloatSamplesUtils.readSample(data, frame * format.getChannels(), format.getSampleSizeInBits() / 8);
	}

	public float[] getFrame(final int frame) {
		final float[] frameData = new float[format.getChannels()];

		for (int channel = 0; channel < format.getChannels(); channel++) {
			frameData[channel] = FloatSamplesUtils.readSample(data, frame * format.getChannels(),
					format.getSampleSizeInBits() / 8);
		}

		return frameData;
	}

	public double msLength() {
		return frames() * 1000.0 / format.getFrameRate();
	}

	public AudioFormat getPlayingFormat() {
		final float sampleRate = format.getSampleRate();
		final int byteDepth = min(2, format.getSampleSizeInBits() / 8);
		final int channels = min(format.getChannels(), 2);
		final int frameSize = channels * byteDepth;

		return new AudioFormat(Encoding.PCM_SIGNED, sampleRate, byteDepth * 8, channels, frameSize, sampleRate, false);
	}

	public byte[] generatePlayingBuffer(final int bufferSize) {
		final int byteDepth = min(2, format.getSampleSizeInBits() / 8);
		final int channels = min(2, format.getChannels());

		return new byte[bufferSize * byteDepth * channels];
	}

	public int fillPlayingBuffer(final int fromFrame, final byte[] buffer) {
		final int byteDepth = format.getSampleSizeInBits() / 8;
		final int writtenByteDepth = min(2, byteDepth);
		final int channels = format.getChannels();
		final int writtenChannels = min(2, channels);
		final int writtenFrameSize = writtenByteDepth * writtenChannels;
		final int framesToWrite = min(buffer.length / writtenFrameSize, frames() - fromFrame);
		if (byteDepth == writtenByteDepth && channels == writtenChannels) {
			final int from = fromFrame * writtenFrameSize;
			final int length = framesToWrite * writtenFrameSize;
			System.arraycopy(data, from, buffer, 0, length);
			if (length < buffer.length) {
				Arrays.fill(buffer, length, buffer.length, (byte) 0);
			}

			return framesToWrite;
		}

		int currentByte = fromFrame * byteDepth * channels;
		int currentBufferByte = 0;
		for (int i = 0; i < framesToWrite; i++) {
			for (int channel = 0; channel < writtenChannels; channel++) {
				for (int j = 0; j < writtenByteDepth; j++) {
					buffer[currentBufferByte++] = data[currentByte++];
				}

				currentByte += byteDepth - writtenByteDepth;
			}

			currentByte += (channels - writtenChannels) * byteDepth;
		}

		return framesToWrite;
	}

	public byte[] getPlayingBufferByte(final int fromFrame, final int bufferSize) {
		final byte[] buffer = generatePlayingBuffer(bufferSize);
		fillPlayingBuffer(fromFrame, buffer);
		return buffer;
	}

	public static class DifferentSampleSizesException extends Exception {
		private static final long serialVersionUID = -6437477047963463037L;
	}

	public static class DifferentChannelAmountException extends Exception {
		private static final long serialVersionUID = -6437477047963463037L;
	}

	public static class DifferentSampleRateException extends Exception {
		private static final long serialVersionUID = -6437477047963463037L;
	}

	private AudioData withCopiedFormat(final byte[] newData) {
		final float sampleRate = format.getSampleRate();
		final int sampleSize = format.getSampleSizeInBits() / 8;
		final int channels = format.getChannels();
		return new AudioData(newData, sampleRate, sampleSize, channels);
	}

	public AudioData join(final AudioData other)
			throws DifferentSampleSizesException, DifferentChannelAmountException, DifferentSampleRateException {
		if (format.getSampleSizeInBits() != other.format.getSampleSizeInBits()) {
			throw new DifferentSampleSizesException();
		}
		if (format.getChannels() != other.format.getChannels()) {
			throw new DifferentChannelAmountException();
		}
		if (format.getSampleRate() != other.format.getSampleRate()) {
			throw new DifferentSampleRateException();
		}

		final byte[] newData = Arrays.copyOf(data, data.length + other.data.length);
		System.arraycopy(other.data, 0, newData, data.length, other.data.length);

		return withCopiedFormat(newData);
	}

	private AudioData copyPart(int from, int to) {
		to -= to % format.getFrameSize();
		from -= from % format.getFrameSize();
		if (to > data.length) {
			to = data.length;
		}
		if (from > to) {
			from = to;
		}

		final byte[] newData = Arrays.copyOfRange(data, from, to);
		return withCopiedFormat(newData);
	}

	public int frame(final double time) {
		return (int) (time * format.getSampleRate());
	}

	private int byteOffset(final double time) {
		return frame(time) * format.getFrameSize();
	}

	public AudioData cut(final double startTime, final double endTime) {
		final int from = byteOffset(startTime);
		final int to = byteOffset(endTime);
		return copyPart(from, to);
	}

	public AudioData removeFromStart(final double time) {
		final int from = byteOffset(time);
		return copyPart(from, data.length);
	}

	public AudioData cutToLength(final double time) {
		final int to = byteOffset(time);
		return copyPart(0, to);
	}
}
