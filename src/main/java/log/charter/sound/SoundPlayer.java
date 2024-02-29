package log.charter.sound;

import static java.lang.Math.floor;
import static java.lang.Math.min;
import static javax.sound.sampled.AudioSystem.getLine;
import static log.charter.data.config.Config.audioBufferSize;

import java.util.Arrays;

import javax.sound.sampled.DataLine.Info;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import log.charter.sound.data.MusicData;

public class SoundPlayer {
	public static class Player {
		private final MusicData<?> musicData;
		private final SourceDataLine line;
		private boolean stopped;
		public long startTime = -1;

		private Player(final MusicData<?> musicData) throws LineUnavailableException {
			this.musicData = musicData;
			final Info info = new Info(SourceDataLine.class, musicData.format());
			line = (SourceDataLine) getLine(info);
		}

		public boolean isStopped() {
			return stopped;
		}

		private Player start(final int startMs) {
			new Thread(() -> {
				try {
					int startByte = (int) floor(((musicData.frameRate() * startMs)) / 250);
					startByte -= startByte % 4;

					if (stopped) {
						return;
					}

					line.open(musicData.format());
					line.start();
					final byte[] data = musicData.getBytes();

					while (startByte < data.length) {
						if (stopped) {
							startTime = System.nanoTime();
							return;
						}

						final byte[] buffer = Arrays.copyOfRange(data, startByte,
								min(data.length, startByte + audioBufferSize));
						if (startTime < 0) {
							startTime = System.nanoTime();
						}
						startByte += line.write(buffer, 0, buffer.length);
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

	public static Player play(final MusicData<?> musicData, final int startMs) {
		try {
			return new Player(musicData).start(startMs);
		} catch (final LineUnavailableException e) {
			e.printStackTrace();
		}
		return null;
	}

}
