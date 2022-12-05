package log.charter.sound;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static log.charter.sound.SoundPlayer.generateBeep;
import static log.charter.sound.SoundPlayer.slow;
import static log.charter.sound.SoundPlayer.toBytes;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;

import log.charter.sound.HighPassFilter.PassType;

public class MusicData {
	public static final int DEF_RATE = 44100;

	public static MusicData generateSound(final double pitch, final double length, final double loudness) {
		final int[] data = new int[(int) (length * DEF_RATE)];
		for (int i = 0; i < data.length; i++) {
			data[i] = (int) (pow(sin((pitch * Math.PI * i) / DEF_RATE), 2) * loudness * 32767);
		}

		return new MusicData(new int[][] { data, data }, DEF_RATE);
	}

	public static MusicData readFile(final String path) {
		if (path.endsWith(".mp3")) {
			final File f = new File(path);
			if (f.exists()) {
				return Mp3Loader.load(f.getAbsolutePath());
			}
		} else if (path.endsWith(".ogg")) {
			final File f = new File(path);
			if (f.exists()) {
				return OggLoader.load(f.getAbsolutePath());
			}
		}
		return null;
	}

	public static MusicData readSongFile(final String dir) {
		File musicFile = new File(dir + "/guitar.mp3");
		if (musicFile.exists()) {
			return Mp3Loader.load(musicFile.getAbsolutePath());
		}

		musicFile = new File(dir + "/guitar.ogg");
		if (musicFile.exists()) {
			return OggLoader.load(musicFile.getAbsolutePath());
		}

		return null;
	}

	private static int[][] splitAudio(final byte[] b) {
		final int[][] d = new int[2][b.length / 4];
		for (int i = 0; i < b.length; i += 4) {
			d[0][i / 4] = b[i] + (b[i + 1] * 256);
			d[1][i / 4] = b[i + 2] + (b[i + 3] * 256);
		}

		return d;
	}

	public final int[][] data;
	public final AudioFormat outFormat;
	private byte[] preparedData;
	private int slow = 1;

	public MusicData(final byte[] b, final float rate) {
		preparedData = b;
		data = splitAudio(b);
		outFormat = new AudioFormat(Encoding.PCM_SIGNED, rate, 16, 2, 4, rate, false);
	}

	public MusicData(final int[][] data, final float rate) {
		preparedData = toBytes(data);
		this.data = data;
		outFormat = new AudioFormat(Encoding.PCM_SIGNED, rate, 16, 2, 4, rate, false);
	}

	public byte[] getData() {
		return preparedData;
	}

	public int msLength() {
		return (int) ((data[0].length * 1000.0) / outFormat.getFrameRate());
	}

	public void setSlow(final int newSlow) {
		if (newSlow == 0) {
			return;
		}
		if (newSlow != slow) {
			slow = newSlow;
			preparedData = toBytes(slow(data, slow));
		}
	}

	public double slowMultiplier() {
		return slow > 0 ? 1.0 / slow : -slow / (-slow + 1.0);
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
}