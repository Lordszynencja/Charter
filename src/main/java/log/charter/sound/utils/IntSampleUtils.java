package log.charter.sound.utils;

import static java.lang.Math.abs;

public class IntSampleUtils {
	public static int readSample(final byte[] bytes, final int position, final int sampleSize) {
		if (bytes.length < position * sampleSize) {
			return 0;
		}

		int sample = 0;
		for (int i = 0; i < sampleSize; i++) {
			final int part = bytes[position * sampleSize + sampleSize - 1 - i];
			if (i == 0) {
				sample = part;
			} else {
				sample = (sample << 8) | (part & 0xFF);
			}
		}

		return sample;
	}

	public static int[][] readSamples(final byte[] bytes, final int channels, final int sampleSize) {
		final int frames = bytes.length / channels / sampleSize;
		final int[][] data = new int[channels][frames];
		for (int i = 0; i < frames; i++) {
			final int offset = i * channels;
			for (int channel = 0; channel < channels; channel++) {
				data[channel][i] = readSample(bytes, offset + channel, sampleSize);
			}
		}

		return data;
	}

	public static void writeSample(final byte[] bytes, final int position, final int sample, final int sampleSize) {
		for (int i = 0; i < sampleSize; i++) {
			bytes[position + i] = (byte) ((sample >> (i * 8)) & 0xFF);
		}
	}

	public static byte[] writeSamples(final int[][] data, final int channels, final int sampleSize,
			final int targetSampleSize) {
		if (data.length < channels) {
			return new byte[0];
		}
		for (int i = 0; i < channels - 1; i++) {
			if (data[i].length != data[i + 1].length) {
				throw new IllegalArgumentException("channels have different lengths");
			}
		}

		final int l = data[0].length;
		final byte[] bytes = new byte[l * channels * targetSampleSize];

		final int movement = abs(targetSampleSize - sampleSize) * 8;
		for (int i = 0; i < l; i++) {
			final int offset = i * channels * targetSampleSize;
			for (int channel = 0; channel < channels; channel++) {
				final int channelOffset = offset + channel * targetSampleSize;
				if (data[channel][i] == 0) {
					continue;
				}

				int sample = data[channel][i];
				if (sampleSize > targetSampleSize) {
					sample = (data[channel][i] >> movement);
				} else if (sampleSize < targetSampleSize) {
					sample = (data[channel][i] << movement);
				}

				writeSample(bytes, channelOffset, sample, targetSampleSize);
			}
		}

		return bytes;
	}

	public static byte[] writeSamples(final int[][] data, final int channels, final int sampleSize) {
		if (data.length < channels) {
			return new byte[0];
		}
		for (int i = 0; i < channels - 1; i++) {
			if (data[i].length != data[i + 1].length) {
				throw new IllegalArgumentException("channels have different lengths");
			}
		}

		final int frameSize = channels * sampleSize;
		final int l = data[0].length;
		final byte[] bytes = new byte[l * frameSize];

		for (int frame = 0; frame < l; frame++) {
			final int offset = frame * frameSize;
			for (int channel = 0; channel < channels; channel++) {
				final int channelOffset = offset + channel * sampleSize;
				if (data[channel][frame] == 0) {
					continue;
				}

				writeSample(bytes, channelOffset, data[channel][frame], sampleSize);
			}
		}

		return bytes;
	}
}
