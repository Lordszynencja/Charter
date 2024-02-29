package log.charter.sound.data;

import static java.lang.Math.pow;
import static java.lang.Math.sin;

public class AudioUtils {
	public static final int DEF_RATE = 44100;

	public static MusicDataShort generateSound(final double pitchHz, final double lengthSeconds,
			final double loudness) {
		return generateSound(pitchHz, lengthSeconds, loudness, DEF_RATE);
	}

	public static MusicDataShort generateSound(final double pitchHz, final double lengthSeconds, final double loudness,
			final float sampleRate) {
		final short[] data = new short[(int) (lengthSeconds * sampleRate)];
		for (int i = 0; i < data.length; i++) {
			data[i] = (short) (pow(sin((pitchHz * Math.PI * i) / sampleRate), 2) * loudness * 32767);
		}

		return new MusicDataShort(new short[][] { data }, sampleRate);
	}

	public static MusicDataShort generateSilence(final double lengthSeconds, final float sampleRate,
			final int channels) {
		final short[] data = new short[(int) (lengthSeconds * sampleRate)];
		final short[][] dataChannels = new short[channels][];
		for (int i = 0; i < channels; i++) {
			dataChannels[i] = data;
		}
		return new MusicDataShort(dataChannels, sampleRate);
	}

	private static short fromBytes(final byte b0, final byte b1) {
		return (short) (((b0 & 0xFF) | (b1 << 8)) & 0xFFFF);
	}

	public static short[][] splitStereoAudioShort(final byte[] b) {
		final short[][] d = new short[2][b.length / 4];
		for (int i = 0; i < d[0].length; i++) {
			final byte b0 = b[i * 4];
			final byte b1 = b[i * 4 + 1];
			final byte b2 = b[i * 4 + 2];
			final byte b3 = b[i * 4 + 3];

			d[0][i] = fromBytes(b0, b1);
			d[1][i] = fromBytes(b2, b3);
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
					value = (value << 8) | bytes[channelOffset + j] & 0xFF;
				}
				data[channel][i] = value;
			}
		}

		return data;
	}

	public static byte[] toBytes(final short[][] data, final int channels, final int sampleBytes) {
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

		for (int i = 0; i < l; i++) {
			final int offset = i * channels * sampleBytes;
			for (int channel = 0; channel < channels; channel++) {
				final int channelOffset = offset + channel * sampleBytes;
				for (int j = 0; j < sampleBytes; j++) {
					bytes[channelOffset + j] = (byte) ((data[channel][i] >> (j * 8)) & 0xFF);
				}
			}
		}

		return bytes;
	}

	public static byte[] toBytes(final int[][] data, final int channels, final int sampleBytes) {
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

		for (int i = 0; i < l; i++) {
			final int offset = i * channels * sampleBytes;
			for (int channel = 0; channel < channels; channel++) {
				final int channelOffset = offset + channel * sampleBytes;
				for (int j = 0; j < sampleBytes; j++) {
					bytes[channelOffset + j] = (byte) ((data[channel][i] >> (j * 8)) & 0xFF);
				}
			}
		}

		return bytes;
	}

	public static int[][] setChannels(final int[][] channels, final int desiredLength) {
		if (channels.length == desiredLength) {
			return channels;
		}
		if (channels.length == 0) {
			return new int[desiredLength][0];
		}

		final int[][] newChannels = new int[desiredLength][];
		for (int i = 0; i < desiredLength; i++) {
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
}
