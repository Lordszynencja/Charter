package log.charter.sound.data;

import org.jcodec.common.logging.Logger;

public class AudioUtils {
	public static final int[][] spans = { //
			{ -0x80, 0x7F }, // 1 byte
			{ -0x80_00, 0x7F_FF }, // 2 bytes
			{ -0x80_00_00, 0x7F_FF_FF }, // 3 bytes
			{ -0x80_00_00_00, 0x7F_FF_FF_FF },// 4 bytes
	};

	public static final int DEF_RATE = 44100;

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

	public static void fixValues(final int sampleBytes, final int[][] samples) {
		if (sampleBytes >= 4) {
			return;
		}

		final int max = AudioData.getMax(sampleBytes);
		final int delta = max - AudioData.getMin(sampleBytes) + 1;
		for (final int[] channel : samples) {
			for (int i = 0; i < channel.length; i++) {
				if (channel[i] > max) {
					Logger.info("Wrong sample");
					channel[i] -= delta;
				}
			}
		}
	}
}
