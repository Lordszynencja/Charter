package log.charter.sound.data;

import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static java.lang.Math.sin;

public class AudioUtils {
	public static final int DEF_RATE = 44100;

	public static AudioData generateEmpty(final double lengthSeconds) {
		return generateSilence(lengthSeconds, DEF_RATE, 1, 2);
	}

	public static AudioData generateSound(final double pitchHz, final double lengthSeconds, final double loudness) {
		return generateSound(pitchHz, lengthSeconds, loudness, DEF_RATE);
	}

	public static AudioData generateSound(final double pitchHz, final double lengthSeconds, final double loudness,
			final float sampleRate) {
		final int[] data = new int[(int) (lengthSeconds * sampleRate)];
		for (int i = 0; i < data.length; i++) {
			data[i] = (int) (pow(sin((pitchHz * Math.PI * i) / sampleRate), 2) * loudness * AudioData.getMax(2));
		}

		return new AudioData(new int[][] { data }, sampleRate, 2);
	}

	public static AudioData generateSilence(final double lengthSeconds, final float sampleRate, final int channels,
			final int sampleSize) {
		final int[] data = new int[(int) (lengthSeconds * sampleRate)];
		final int[][] dataChannels = new int[channels][];
		for (int i = 0; i < channels; i++) {
			dataChannels[i] = data;
		}

		return new AudioData(dataChannels, sampleRate, sampleSize);
	}

	private static short fromBytes(final byte b0, final byte b1) {
		return (short) (((b0 & 0xFF) | (b1 << 8)) & 0xFFFF);
	}

	public static short fromBytes(final byte[] bytes, final int position, final int length) {
		if (length == 1) {
			return (short) (bytes[position] & 0xFF);
		}

		return fromBytes(bytes[position * 2], bytes[position * 2 + 1]);
	}

	public static short[][] splitStereoAudioShort(final byte[] b) {
		final short[][] d = new short[2][b.length / 4];
		for (int i = 0; i < d[0].length; i++) {
			d[0][i] = fromBytes(b, i * 2, 2);
			d[1][i] = fromBytes(b, i * 2 + 1, 2);
		}

		return d;
	}

	public static short[][] splitAudioShort(final byte[] bytes, final int channels, final int sampleBytes) {
		final int frames = bytes.length / channels / sampleBytes;
		final short[][] data = new short[channels][frames];
		for (int i = 0; i < frames; i++) {
			final int offset = i * channels * sampleBytes;
			for (int channel = 0; channel < channels; channel++) {
				final int channelOffset = offset + channel * sampleBytes;
				short value = 0;
				for (int j = sampleBytes - 1; j >= 0; j--) {
					value = (short) ((value << 8) | bytes[channelOffset + j] & 0xFF);
				}
				data[channel][i] = value;
			}
		}

		return data;
	}

	public static int[][] splitAudioInt(final byte[] bytes, final int channels, final int sampleBytes) {
		final int frames = bytes.length / channels / sampleBytes;
		final int[][] data = new int[channels][frames];
		for (int i = 0; i < frames; i++) {
			final int offset = i * channels * sampleBytes;
			for (int channel = 0; channel < channels; channel++) {
				final int channelOffset = offset + channel * sampleBytes;
				int value = 0;
				for (int j = sampleBytes - 1; j >= 0; j--) {
					value = (value << 8) | (bytes[channelOffset + j] & 0xFF);
				}

				data[channel][i] = value;
			}
		}

		return data;
	}

	private static float floatFromBytes(final byte b0, final byte b1) {
		return (float) fromBytes(b0, b1) / 0x8000;
	}

	public static float floatFromBytes(final byte[] bytes, final int position, final int length) {
		if (length == 1) {
			return (short) (bytes[position] & 0xFF) / 128f - 1;
		}

		return floatFromBytes(bytes[position * 2], bytes[position * 2 + 1]);
	}

	public static float[][] splitStereoAudioFloat(final byte[] b) {
		final float[][] d = new float[2][b.length / 4];
		for (int i = 0; i < d[0].length; i++) {
			d[0][i] = floatFromBytes(b, i * 2, 2);
			d[1][i] = floatFromBytes(b, i * 2 + 1, 2);
		}

		return d;
	}

	private static void writeBytes(final byte[] bytes, final int position, final int sample, final int sampleSize) {
		for (int i = 0; i < sampleSize; i++) {
			bytes[position + i] = (byte) ((sample >> (i * 8)) & 0xFF);
		}
	}

	public static byte[] toBytes(final int[][] data, final int channels, final int sampleSize,
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

				writeBytes(bytes, channelOffset, sample, targetSampleSize);
			}
		}

		return bytes;
	}

	public static byte[] toBytes(final int[][] data, final int channels, final int sampleBytes) {
		return toBytes(data, channels, sampleBytes, sampleBytes);
	}

	private static void toBytes(final float sample, final byte[] bytes) {
		switch (bytes.length) {
			case 1:
				bytes[0] = (byte) (((sample + 1) / 2) * 255);
				return;
			case 2:
				final short value = (short) (sample * 0x7FFF);
				bytes[0] = (byte) (value & 0xFF);
				bytes[1] = (byte) (value >> 8 & 0xFF);
				return;
			default:
				return;
		}
	}

	public static byte[] toBytes(final float[][] data, final int channels, final int sampleBytes) {
		if (data.length < channels) {
			return new byte[0];
		}
		for (int i = 0; i < channels - 1; i++) {
			if (data[i].length != data[i + 1].length) {
				throw new IllegalArgumentException("channels have different lengths");
			}
		}

		final int l = data[0].length;
		final byte[] bytes = new byte[l * channels * sampleBytes];
		final byte[] buffer = new byte[sampleBytes];

		for (int i = 0; i < l; i++) {
			final int offset = i * channels * sampleBytes;
			for (int channel = 0; channel < channels; channel++) {
				final int channelOffset = offset + channel * sampleBytes;
				toBytes(data[channel][i], buffer);

				System.arraycopy(buffer, 0, bytes, channelOffset, sampleBytes);
			}
		}

		return bytes;
	}

	public static int[][] setChannels(final int[][] channels, final int desiredChannels) {
		if (channels.length == desiredChannels) {
			return channels;
		}
		if (channels.length == 0) {
			return new int[desiredChannels][0];
		}

		final int[][] newChannels = new int[desiredChannels][];
		for (int i = 0; i < desiredChannels; i++) {
			newChannels[i] = i >= channels.length ? channels[0] : channels[i];
		}

		return newChannels;
	}

	public static short[][] setChannels(final short[][] channels, final int desiredLength) {
		if (channels.length == desiredLength) {
			return channels;
		}
		if (channels.length == 0) {
			return new short[desiredLength][0];
		}

		final short[][] newChannels = new short[desiredLength][];
		for (int i = 0; i < desiredLength; i++) {
			newChannels[i] = i >= channels.length ? channels[0] : channels[i];
		}

		return newChannels;
	}

	public static double centsToPitch(final int basePitch, final double cents) {
		return basePitch * Math.pow(2, cents / 1200);
	}

	public static double pitchToCents(final int basePitch, final double pitch) {
		return Math.log(pitch / basePitch) / Math.log(2) * 1200;
	}
}
