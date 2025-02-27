package log.charter.sound.utils;

import static java.lang.Math.abs;

import log.charter.sound.data.AudioUtils;

public class FloatSamplesUtils {
	public static float readSample(final byte[] bytes, final int position, final int sampleSize) {
		final int sample = IntSampleUtils.readSample(bytes, position, sampleSize);
		final float divisor = abs(
				(float) (sample < 0 ? AudioUtils.spans[sampleSize - 1][0] : AudioUtils.spans[sampleSize - 1][1]));

		return sample / divisor;
	}

	public static float[][] splitStereoAudioFloat(final byte[] b, final int sampleSize, final int channels) {
		final float[][] d = new float[channels][b.length / sampleSize / channels];
		for (int i = 0; i < d[0].length; i++) {
			for (int channel = 0; channel < channels; channel++) {
				d[channel][i] = readSample(b, i * channels + channel, sampleSize);
			}
		}

		return d;
	}

	public static byte[] toBytes(final float[][] data, final int channels, final int sampleSize) {
		if (data.length < channels) {
			return new byte[0];
		}
		for (int i = 0; i < channels - 1; i++) {
			if (data[i].length != data[i + 1].length) {
				throw new IllegalArgumentException("channels have different lengths");
			}
		}

		final int l = data[0].length;
		final byte[] bytes = new byte[l * channels * sampleSize];

		for (int i = 0; i < l; i++) {
			final int offset = i * channels * sampleSize;
			for (int channel = 0; channel < channels; channel++) {
				FloatSamplesUtils.writeSample(bytes, offset + channel * sampleSize, data[channel][i], sampleSize);
			}
		}

		return bytes;
	}

	public static void writeSample(final byte[] bytes, final int position, final float sample, final int sampleSize) {
		final float divisor = abs(
				(float) (sample < 0 ? AudioUtils.spans[sampleSize - 1][0] : AudioUtils.spans[sampleSize - 1][1]));
		final int sampleInt = (int) (sample * divisor);
		IntSampleUtils.writeSample(bytes, position, sampleInt, sampleSize);
	}

}
