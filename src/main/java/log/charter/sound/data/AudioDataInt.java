package log.charter.sound.data;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.sound.data.AudioUtils.splitAudioInt;
import static log.charter.sound.data.AudioUtils.toBytes;

import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;

import log.charter.sound.HighPassFilter;
import log.charter.sound.HighPassFilter.PassType;

public class AudioDataInt extends AudioData<AudioDataInt> {
	public static final int minValue = -0x8000;
	public static final int maxValue = 0x7FFF;

	public final int[][] data;
	public final AudioFormat format;
	private final byte[] bytes;

	public AudioDataInt(final byte[] bytes, final float rate, final int channels, final int sampleBytes) {
		this(splitAudioInt(bytes, channels, sampleBytes), bytes, rate, channels, sampleBytes);
	}

	public AudioDataInt(final int[][] data, final float rate) {
		this(data, toBytes(data, data.length, 2), rate, data.length, 2);
	}

	private AudioDataInt(final int[][] data, final byte[] bytes, final float rate, final int channels,
			final int sampleBytes) {
		this.data = data;
		format = new AudioFormat(Encoding.PCM_SIGNED, rate, sampleBytes * 8, channels, channels * sampleBytes, rate,
				false);
		this.bytes = bytes;
	}

	@Override
	public AudioFormat format() {
		return format;
	}

	@Override
	public byte[] getBytes() {
		return bytes;

	}

	@Override
	public int msLength() {
		return (int) ((data[0].length * 1000.0) / format.getFrameRate());
	}

	public AudioDataInt pass(final float frequency, final float resonance, final PassType type) {
		final float rate = format.getSampleRate();
		final int[][] newData = new int[data.length][];
		final int valueRange = maxValue - minValue;

		for (int i = 0; i < data.length; i++) {
			final int[] oldChannel = data[i];
			newData[i] = new int[oldChannel.length];
			final HighPassFilter filter = new HighPassFilter(frequency, (int) rate, type, resonance);
			for (int j = 0; j < oldChannel.length; j++) {
				final float oldVal = (float) ((oldChannel[j] - minValue) / valueRange);
				final float newVal = max(0, min(1, filter.update(oldVal)));
				newData[i][j] = (int) (newVal * valueRange + minValue);
			}
		}

		return new AudioDataInt(newData, rate);
	}

	@Override
	public AudioDataInt join(final AudioDataInt other) {
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

		return new AudioDataInt(newData, format.getSampleRate());
	}

	@Override
	public AudioDataInt cut(final double startTime, final double endTime) {
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

		return new AudioDataInt(newData, format.getSampleRate());
	}

	@Override
	public AudioDataInt remove(final double time) {
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

		return new AudioDataInt(newData, format.getSampleRate());
	}

	@Override
	public AudioDataInt volume(final double volume) {
		final int[][] newData = new int[data.length][];
		for (int channel = 0; channel < data.length; channel++) {
			newData[channel] = Arrays.copyOf(data[channel], data[channel].length);
			for (int i = 0; i < newData[channel].length; i++) {
				newData[channel][i] *= volume;
			}
		}

		return new AudioDataInt(newData, format.getSampleRate());
	}
}