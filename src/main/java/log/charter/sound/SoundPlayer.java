package log.charter.sound;

import static java.lang.Math.floor;
import static java.lang.Math.sin;
import static java.lang.System.arraycopy;
import static javax.sound.sampled.AudioSystem.getLine;

import javax.sound.sampled.DataLine.Info;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class SoundPlayer {

	public static class Player {
		private static final int BUFF_SIZE = 1024 * 128;

		private final MusicData musicData;
		private final SourceDataLine line;
		private boolean stopped;
		public long startTime = -1;

		private Player(final MusicData musicData) throws LineUnavailableException {
			this.musicData = musicData;
			final Info info = new Info(SourceDataLine.class, musicData.outFormat);
			line = (SourceDataLine) getLine(info);
		}

		public boolean isStopped() {
			return stopped;
		}

		private Player start(final int startMs) {
			new Thread(() -> {
				try {
					int startByte = (int) floor(
							((musicData.outFormat.getFrameRate() * startMs) / musicData.slowMultiplier()) / 250);
					startByte -= startByte % 4;

					if (stopped) {
						return;
					}

					line.open(musicData.outFormat);
					line.start();
					final byte[] data = musicData.getData();
					startTime = System.nanoTime();
					if (stopped) {
						while ((data.length - startByte) > BUFF_SIZE) {
							if (stopped) {
								return;
							}
							line.write(data, startByte, BUFF_SIZE);
							startByte += BUFF_SIZE;
						}
					}

					if ((data.length - startByte) > 0) {
						line.write(data, startByte, data.length - startByte);
					}
					line.drain();
					line.stop();
					stopped = true;
				} catch (final LineUnavailableException e) {
					e.printStackTrace();
				}
			}).start();
			return this;
		}

		public void stop() {
			if (stopped) {
				return;
			}

			stopped = true;
			line.stop();
			line.drain();
			line.flush();
		}
	}

	public static int[][] concat(final int[][]... d) {
		int l = 0;
		final int[] lengths = new int[d.length];

		for (int i = 0; i < d.length; i++) {
			final int ll = d[i][0].length;
			final int lr = d[i][1].length;
			lengths[i] = ll > lr ? ll : lr;

			l += lengths[i];
		}

		final int[][] joined = new int[2][l];
		int currentStart = 0;

		for (int i = 0; i < d.length; i++) {
			arraycopy(d[i][0], 0, joined[0], currentStart, d[i][0].length);
			arraycopy(d[i][1], 0, joined[1], currentStart, d[i][1].length);
			currentStart += lengths[i];
		}

		return joined;
	}

	public static int[] generateBeep(final int length, final int pitch, final int loudness, final int rate) {
		final int[] data = new int[length];

		for (int i = 0; i < length; i++) {
			data[i] = (int) floor(sin(((i * Math.PI * 2) / rate) * pitch) * loudness);
		}

		return data;
	}

	public static int[][] generateData(final int length, final int pitch, final int loudness, final int rate) {
		final int[][] data = new int[2][];
		data[0] = generateBeep(length, pitch, loudness, rate);
		data[1] = data[0];

		return data;
	}

	public static Player play(final MusicData md, final int startMs) {
		try {
			return new Player(md).start(startMs);
		} catch (final LineUnavailableException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static int[] slow(final int[] d, final int s) {
		final int l = d.length;
		if (l < 2) {
			return d;
		}
		if (s < 0) {
			final int s1 = -s;
			final int newL = (((l - 1) / s1) * (s1 + 1)) + 1;
			final int[] d1 = new int[newL];

			for (int i = 0; i < ((l - 1) / s1); i++) {
				d1[i * (s1 + 1)] = d[i * s1];
				for (int j = 1; j <= s1; j++) {
					final int b = (i * s1) + j;
					final int a = b - 1;
					final int wa = j;
					final int wb = (s1 - j) + 1;
					d1[(i * (s1 + 1)) + j] = ((wa * d[a]) + (wb * d[b])) / (s1 + 1);
				}
			}
			d1[newL - 1] = d[d.length - 1];

			return d1;
		}

		final int[] d1 = new int[((l - 1) * s) + 1];

		for (int i = 0; i < (l - 1); i++) {
			for (int j = 0; j < s; j++) {
				d1[(i * s) + j] = (((s - j) * d[i]) + (j * d[i + 1])) / s;
			}
		}

		d1[(l - 1) * s] = d[l - 1];

		return d1;
	}

	public static int[][] slow(final int[][] d, final int s) {
		return new int[][] { slow(d[0], s), slow(d[1], s) };
	}

	private static int[] sum(final int[]... d) {
		int l = 0;

		for (int i = 0; i < d.length; i++) {
			if (d[i].length > l) {
				l = d[i].length;
			}
		}

		final int[] summed = new int[l];

		for (int i = 0; i < d.length; i++) {
			for (int j = 0; j < d[i].length; j++) {
				summed[j] += d[i][j];
			}
		}

		return summed;
	}

	public static int[][] sum(final int[][]... d) {
		final int[][][] separated = new int[2][d.length][];

		for (int i = 0; i < d.length; i++) {
			separated[0][i] = d[i][0];
			separated[1][i] = d[i][1];
		}

		return new int[][] { sum(separated[0]), sum(separated[1]) };
	}

	public static byte[] toBytes(int[][] data) {
		if (data.length == 1) {
			data = new int[][] { data[0], data[0] };
		}
		if (data[0].length != data[1].length) {
			throw new IllegalArgumentException("Left and right channel have different lengths");
		}

		final int l = data[0].length;
		final byte[] bytes = new byte[l * 4];

		for (int i = 0; i < l; i++) {
			final byte b0 = (byte) data[0][i];
			final byte b1 = (byte) ((data[0][i] - b0) / 256);
			final byte b2 = (byte) data[1][i];
			final byte b3 = (byte) ((data[1][i] - b2) / 256);

			bytes[i * 4] = b0;
			bytes[(i * 4) + 1] = b1;
			bytes[(i * 4) + 2] = b2;
			bytes[(i * 4) + 3] = b3;
		}

		return bytes;
	}

}
