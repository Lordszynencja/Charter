package log.charter.sound.data;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.sound.data.AudioUtils.splitAudioShort;
import static log.charter.sound.data.AudioUtils.toBytes;

import java.io.File;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;

import log.charter.io.Logger;
import log.charter.sound.HighPassFilter;
import log.charter.sound.HighPassFilter.PassType;
import log.charter.sound.mp3.Mp3Loader;
import log.charter.sound.ogg.OggLoader;

public class MusicDataShort extends MusicData<MusicDataShort> {
	public static final short minValue = -0x8000;
	public static final short maxValue = 0x7FFF;

	public static MusicDataShort readFile(final File file) {
		try {
			if (file.getName().endsWith(".mp3")) {
				if (file.exists()) {
					return Mp3Loader.load(file.getAbsolutePath());
				}
			} else if (file.getName().endsWith(".ogg")) {
				if (file.exists()) {
					return OggLoader.load(file.getAbsolutePath());
				}
			}
		} catch (final Exception e) {
			Logger.error("Couldn't load file " + file, e);
		}

		return null;
	}

	public final short[][] data;
	public final AudioFormat format;
	private final byte[] bytes;

	public MusicDataShort() {
		this(new short[2][0], new byte[0], AudioUtils.DEF_RATE, 2, 2);
	}

	public MusicDataShort(final byte[] bytes, final float rate, final int channels, final int sampleBytes) {
		this(splitAudioShort(bytes, channels, sampleBytes), bytes, rate, channels, sampleBytes);
	}

	public MusicDataShort(final short[][] data, final float rate) {
		this(data, toBytes(data, data.length, 2), rate, data.length, 2);
	}

	private MusicDataShort(final short[][] data, final byte[] bytes, final float rate, final int channels,
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

	public MusicDataShort pass(final float frequency, final float resonance, final PassType type) {
		final float rate = format.getSampleRate();
		final short[][] newData = new short[data.length][];
		final int valueRange = maxValue - minValue;

		for (int i = 0; i < data.length; i++) {
			final short[] oldChannel = data[i];
			newData[i] = new short[oldChannel.length];
			final HighPassFilter filter = new HighPassFilter(frequency, (int) rate, type, resonance);
			for (int j = 0; j < oldChannel.length; j++) {
				final float oldVal = (float) ((oldChannel[j] - minValue) / valueRange);
				final float newVal = max(0, min(1, filter.update(oldVal)));
				newData[i][j] = (short) (newVal * valueRange + minValue);
			}
		}

		return new MusicDataShort(newData, rate);
	}

	@Override
	public MusicDataShort join(final MusicDataShort other) {
		int length0 = 0;
		for (int i = 0; i < data.length; i++) {
			length0 = max(length0, data[i].length);
		}
		int length1 = 0;
		for (int i = 0; i < other.data.length; i++) {
			length1 = max(length1, other.data[i].length);
		}

		final int channels = max(data.length, other.data.length);
		final short[][] newData = new short[channels][length0 + length1];
		for (int channel = 0; channel < data.length; channel++) {
			final short[] targetChannel = newData[channel];
			System.arraycopy(data[channel], 0, targetChannel, 0, data[channel].length);
			System.arraycopy(other.data[channel], 0, targetChannel, length0, other.data[channel].length);
		}

		return new MusicDataShort(newData, format.getSampleRate());
	}

	@Override
	public MusicDataShort cut(final double startTime, final double endTime) {
		int length = 0;
		for (int i = 0; i < data.length; i++) {
			length = max(length, data[i].length);
		}

		final int start = (int) (startTime * format.getSampleRate());
		final int end = (int) (endTime * format.getSampleRate());

		final short[][] newData = new short[data.length][end - start + 1];
		for (int channel = 0; channel < data.length; channel++) {
			final short[] src = data[channel];
			if (src.length < start) {
				continue;
			}

			final int toMove = min(end - start + 1, src.length - start);
			System.arraycopy(src, start, newData[channel], 0, toMove);
		}

		return new MusicDataShort(newData, format.getSampleRate());
	}

	@Override
	public MusicDataShort remove(final double time) {
		int length = 0;
		for (int i = 0; i < data.length; i++) {
			length = max(length, data[i].length);
		}

		final int samplesRemoved = (int) (time * format.getSampleRate());
		length -= samplesRemoved;

		final int channels = data.length;
		final short[][] newData = new short[channels][length];
		for (int channel = 0; channel < data.length; channel++) {
			final short[] src = data[channel];
			final short[] dest = newData[channel];
			final int toMove = max(0, data[channel].length - samplesRemoved);
			System.arraycopy(src, samplesRemoved, dest, 0, toMove);
		}

		return new MusicDataShort(newData, format.getSampleRate());
	}

	@Override
	public MusicDataShort volume(final double volume) {
		final short[][] newData = new short[data.length][];
		for (int channel = 0; channel < data.length; channel++) {
			newData[channel] = Arrays.copyOf(data[channel], data[channel].length);
			for (int i = 0; i < newData[channel].length; i++) {
				newData[channel][i] *= volume;
			}
		}

		return new MusicDataShort(newData, format.getSampleRate());
	}
}