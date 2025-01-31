package log.charter.sound.data;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.sound.data.AudioUtils.toBytes;

import java.io.File;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;

import log.charter.io.Logger;
import log.charter.sound.SoundFileType;

public class AudioData {
	private static final int[][] spans = { //
			{ -0x80, 0x7F }, // 1 byte
			{ -0x80_00, 0x7F_FF }, // 2 bytes
			{ -0x80_00_00, 0x7F_FF_FF }, // 3 bytes
			{ -0x80_00_00_00, 0x7F_FF_FF_FF },// 4 bytes
	};

	public static int getMin(final int sampleSize) {
		return spans[max(0, min(3, sampleSize - 1))][0];
	}

	public static int getMax(final int sampleSize) {
		return spans[max(0, min(3, sampleSize - 1))][1];
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

	public final int[][] data;
	public final AudioFormat format;
	public final AudioFormat playingFormat;
	public final byte[] playingBytes;
	public final int minValue;
	public final int maxValue;

	public AudioData(final int[][] data, final float sampleRate, final int sampleBytes) {
		this.data = data;
		final int channels = data.length;
		format = new AudioFormat(Encoding.PCM_SIGNED, sampleRate, sampleBytes * 8, channels, channels * sampleBytes,
				sampleRate, false);

		final int playingChannels = min(channels, 2);
		playingFormat = new AudioFormat(Encoding.PCM_SIGNED, sampleRate, 16, playingChannels, playingChannels * 2,
				sampleRate, false);
		playingBytes = toBytes(data, playingChannels, sampleBytes, 2);

		minValue = spans[sampleBytes - 1][0];
		maxValue = spans[sampleBytes - 1][1];
	}

	public double msLength() {
		return (data[0].length * 1000.0) / format.getFrameRate();
	}

	public static class DifferentSampleSizesException extends Exception {
		private static final long serialVersionUID = -6437477047963463037L;

	}

	public AudioData join(final AudioData other) throws DifferentSampleSizesException {
		if (format.getSampleSizeInBits() != other.format.getSampleSizeInBits()) {
			throw new DifferentSampleSizesException();
		}

		int length0 = 0;
		for (int i = 0; i < data.length; i++) {
			length0 = max(length0, data[i].length);
		}
		int length1 = 0;
		for (int i = 0; i < other.data.length; i++) {
			length1 = max(length1, other.data[i].length);
		}

		final int channels = max(data.length, other.data.length);
		final int[][] newData = new int[channels][length0 + length1];
		for (int channel = 0; channel < data.length; channel++) {
			final int[] targetChannel = newData[channel];
			System.arraycopy(data[channel], 0, targetChannel, 0, data[channel].length);
			System.arraycopy(other.data[channel], 0, targetChannel, length0, other.data[channel].length);
		}

		return new AudioData(newData, format.getSampleRate(), format.getSampleSizeInBits() / 8);
	}

	public AudioData cut(final double startTime, final double endTime) {
		int length = 0;
		for (int i = 0; i < data.length; i++) {
			length = max(length, data[i].length);
		}

		final int start = (int) (startTime * format.getSampleRate());
		final int end = (int) (endTime * format.getSampleRate());

		final int[][] newData = new int[data.length][end - start + 1];
		for (int channel = 0; channel < data.length; channel++) {
			final int[] src = data[channel];
			if (src.length < start) {
				continue;
			}

			final int toMove = min(end - start + 1, src.length - start);
			System.arraycopy(src, start, newData[channel], 0, toMove);
		}

		return new AudioData(newData, format.getSampleRate(), format.getSampleSizeInBits() / 8);
	}

	public AudioData removeFromStart(final double time) {
		int length = 0;
		for (int i = 0; i < data.length; i++) {
			length = max(length, data[i].length);
		}

		final int samplesRemoved = (int) (time * format.getSampleRate());
		length -= samplesRemoved;

		final int channels = data.length;
		final int[][] newData = new int[channels][length];
		for (int channel = 0; channel < data.length; channel++) {
			final int[] src = data[channel];
			final int[] dest = newData[channel];
			final int toMove = max(0, data[channel].length - samplesRemoved);
			System.arraycopy(src, samplesRemoved, dest, 0, toMove);
		}

		return new AudioData(newData, format.getSampleRate(), format.getSampleSizeInBits() / 8);
	}

	public AudioData volume(final double volume) {
		final int[][] newData = new int[data.length][];
		for (int channel = 0; channel < data.length; channel++) {
			newData[channel] = Arrays.copyOf(data[channel], data[channel].length);
			for (int i = 0; i < newData[channel].length; i++) {
				newData[channel][i] *= volume;
			}
		}

		return new AudioData(newData, format.getSampleRate(), format.getSampleSizeInBits() / 8);
	}
}
