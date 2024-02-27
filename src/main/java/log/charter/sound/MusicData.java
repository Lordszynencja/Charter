package log.charter.sound;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static log.charter.sound.SoundPlayer.generateBeep;
import static log.charter.sound.SoundPlayer.toBytes;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;

import log.charter.io.Logger;
import log.charter.sound.HighPassFilter.PassType;
import log.charter.sound.mp3.Mp3Loader;
import log.charter.sound.ogg.OggLoader;

public class MusicData {
	public static final int DEF_RATE = 44100;

	public static MusicData generateSound(final double pitchHz, final double lengthSeconds, final double loudness) {
		return generateSound(pitchHz, lengthSeconds, loudness, DEF_RATE);
	}

	public static MusicData generateSound(final double pitchHz, final double lengthSeconds, final double loudness,
			final float sampleRate) {
		final int[] data = new int[(int) (lengthSeconds * sampleRate)];
		for (int i = 0; i < data.length; i++) {
			data[i] = (int) (pow(sin((pitchHz * Math.PI * i) / sampleRate), 2) * loudness * 32767);
		}

		return new MusicData(new int[][] { data, data }, sampleRate);
	}

	public static MusicData generateSilence(final double lengthSeconds, final float sampleRate) {
		final int[] data = new int[(int) (lengthSeconds * sampleRate)];
		return new MusicData(new int[][] { data, data }, sampleRate);
	}

	public static MusicData readFile(final File file) {
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

	public static MusicData readSongFile(final String dir) {
		File musicFile = new File(dir, "guitar.mp3");
		if (musicFile.exists()) {
			return Mp3Loader.load(musicFile.getAbsolutePath());
		}

		musicFile = new File(dir, "guitar.ogg");
		if (musicFile.exists()) {
			return OggLoader.load(musicFile.getAbsolutePath());
		}

		return null;
	}

	private static int[][] splitAudio(final byte[] b) {
		final int[][] d = new int[2][b.length / 4];
		for (int i = 0; i < d[0].length; i++) {
			final byte b0 = b[i * 4];
			final byte b1 = b[i * 4 + 1];
			final byte b2 = b[i * 4 + 2];
			final byte b3 = b[i * 4 + 3];

			d[0][i] = (b0 & 0xFF) + (b1 << 8);
			d[1][i] = (b2 & 0xFF) + (b3 << 8);
		}

		return d;
	}

	public final int[][] data;
	public final AudioFormat outFormat;
	private final byte[] preparedData;

	public MusicData(final byte[] b) {
		this(b, DEF_RATE);
	}

	public MusicData(final byte[] b, final float rate) {
		preparedData = b;
		data = splitAudio(b);
		outFormat = new AudioFormat(Encoding.PCM_SIGNED, rate, 16, 2, 4, rate, false);
	}

	public MusicData(final int[][] data) {
		this(data, DEF_RATE);
	}

	public MusicData(int[][] data, final float rate) {
		if (data.length == 1) {
			data = new int[][] { data[0], data[0] };
		}
		preparedData = toBytes(data);
		this.data = data;
		outFormat = new AudioFormat(Encoding.PCM_SIGNED, rate, 16, 2, 4, rate, false);
	}

	public byte[] getStereoData() {
		return preparedData;
	}

	public int msLength() {
		return (int) ((data[0].length * 1000.0) / outFormat.getFrameRate());
	}

	public List<Double> positionsOfHighs() {
		final double rate = outFormat.getSampleRate();
		final int timeout = (int) (25 * rate / 1000);
		final List<Double> highs = new ArrayList<>();

		int soundCounter = 0;
		for (int j = 0; j < data[0].length; j++) {
			for (int i = 0; i < data.length; i++) {
				if (soundCounter > 0) {
					soundCounter--;
				} else {
					if (data[i][j] > 32_700) {
						highs.add(j / rate);
						soundCounter = timeout;
					}
				}
			}
		}

		return highs;
	}

	public MusicData highsToSounds() {
		final float rate = outFormat.getSampleRate();
		final int[][] newData = new int[data.length][];
		final int[] sound = generateBeep((int) (20 * rate / 1000), 440, 32767, (int) rate);

		for (int i = 0; i < data.length; i++) {
			final int[] oldChannel = data[i];
			final int[] newChannel = new int[oldChannel.length];

			int soundCounter = 0;
			for (int j = 0; j < oldChannel.length; j++) {
				if (soundCounter > 0) {
					newChannel[j] = sound[sound.length - soundCounter];
					soundCounter--;
				} else {
					newChannel[j] = 0;
					if (oldChannel[j] > 32_000) {
						soundCounter = sound.length;
					}
				}
			}

			newData[i] = newChannel;
		}

		return new MusicData(newData, rate);
	}

	public MusicData pass(final float frequency, final float resonance, final PassType type) {
		final float rate = outFormat.getSampleRate();
		final int[][] newData = new int[data.length][];
		for (int i = 0; i < data.length; i++) {
			final int[] oldChannel = data[i];
			newData[i] = new int[oldChannel.length];
			final HighPassFilter filter = new HighPassFilter(frequency, (int) rate, type, resonance);
			for (int j = 0; j < oldChannel.length; j++) {
				final float oldVal = (float) ((oldChannel[j] + 32768) / 65536.0);
				final float newVal = max(0, min(1, filter.update(oldVal) * 1.0f));
				newData[i][j] = (int) (newVal * 65535 - 32768);
			}
		}

		return new MusicData(newData, rate);
	}

	public MusicData join(final MusicData other) {
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

		return new MusicData(newData, outFormat.getSampleRate());
	}

	public MusicData cut(final double startTime, final double endTime) {
		int length = 0;
		for (int i = 0; i < data.length; i++) {
			length = max(length, data[i].length);
		}

		final int start = (int) (startTime * outFormat.getSampleRate());
		final int end = (int) (endTime * outFormat.getSampleRate());

		final int[][] newData = new int[data.length][end - start + 1];
		for (int channel = 0; channel < data.length; channel++) {
			final int[] src = data[channel];
			if (src.length < start) {
				continue;
			}

			final int toMove = min(end - start + 1, src.length - start);
			System.arraycopy(src, start, newData[channel], 0, toMove);
		}

		return new MusicData(newData, outFormat.getSampleRate());
	}

	public MusicData remove(final double time) {
		int length = 0;
		for (int i = 0; i < data.length; i++) {
			length = max(length, data[i].length);
		}

		final int samplesRemoved = (int) (time * outFormat.getSampleRate());
		length -= samplesRemoved;

		final int channels = data.length;
		final int[][] newData = new int[channels][length];
		for (int channel = 0; channel < data.length; channel++) {
			final int[] src = data[channel];
			final int[] dest = newData[channel];
			final int toMove = max(0, data[channel].length - samplesRemoved);
			System.arraycopy(src, samplesRemoved, dest, 0, toMove);
		}

		return new MusicData(newData, outFormat.getSampleRate());
	}

	public MusicData volume(final double volume) {
		final int[][] newData = new int[data.length][];
		for (int channel = 0; channel < data.length; channel++) {
			newData[channel] = Arrays.copyOf(data[channel], data[channel].length);
			for (int i = 0; i < newData[channel].length; i++) {
				newData[channel][i] *= volume;
			}
		}

		return new MusicData(newData, outFormat.getSampleRate());
	}

	public int getFirstDifferent(final MusicData other) {
		for (int i = 0; i < data[0].length; i++) {
			if (data[0][i] != other.data[0][i]) {
				return i;
			}
		}

		return -1;
	}
}