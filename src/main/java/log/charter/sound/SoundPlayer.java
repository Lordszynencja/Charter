package log.charter.sound;

import static java.lang.Math.min;
import static javax.sound.sampled.AudioSystem.getLine;
import static log.charter.data.config.Config.audioBufferSize;
import static log.charter.sound.data.AudioUtils.fromBytes;
import static log.charter.sound.data.AudioUtils.writeBytes;

import java.util.Arrays;
import java.util.function.DoubleSupplier;

import javax.sound.sampled.DataLine.Info;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import log.charter.data.config.Config;
import log.charter.io.Logger;
import log.charter.sound.data.AudioData;
import log.charter.sound.data.AudioUtils;

public class SoundPlayer {
	public static class Player {
		private final AudioData<?> musicData;
		private final DoubleSupplier volume;

		private final int sampleSizeInBits;
		private final int sampleSize;
		private final int frameSize;
		private final int maxBytes;
		private final SourceDataLine line;
		private int bufferSize;
		private boolean stopped;

		public long playingStartTime = -1;

		public Player(final AudioData<?> musicData) throws LineUnavailableException {
			this(musicData, null);
		}

		public Player(final AudioData<?> musicData, final DoubleSupplier volume) throws LineUnavailableException {
			this.musicData = musicData;
			this.volume = volume;

			sampleSizeInBits = musicData.format().getSampleSizeInBits();
			if (sampleSizeInBits <= 8) {
				sampleSize = 1;
			} else {
				sampleSize = 2;
			}
			frameSize = musicData.format().getFrameSize();
			maxBytes = (int) (musicData.format().getFrameRate() * frameSize * Config.audioBufferMs / 1000);
			final Info info = new Info(SourceDataLine.class, musicData.format());
			line = (SourceDataLine) getLine(info);
		}

		public boolean isStopped() {
			return stopped;
		}

		private int getStartByte(final int startTime) {
			final int startFrame = (int) (musicData.frameRate() * startTime / 1000);
			return startFrame * frameSize;
		}

		private void setUpLine() throws LineUnavailableException {
			line.open(musicData.format());
			line.start();
			bufferSize = line.getBufferSize();
		}

		private void setVolume(final byte[] buffer) {
			if (volume == null) {
				return;
			}

			final int samples = buffer.length / sampleSize;
			for (int i = 0; i < samples; i++) {
				short sample = fromBytes(buffer, i, sampleSize);
				sample = AudioUtils.clipShort(sample * volume.getAsDouble());
				writeBytes(buffer, i * sampleSize, sample, sampleSize);
			}
		}

		private int writeBuffer(final byte[] data, int startByte) {
			final byte[] buffer = Arrays.copyOfRange(data, startByte, min(data.length, startByte + audioBufferSize));
			setVolume(buffer);

			startByte += line.write(buffer, 0, buffer.length);
			return startByte;
		}

		private void waitIfNeeded() throws InterruptedException {
			if (bufferSize - line.available() > maxBytes) {
				Thread.sleep(1);
			}
		}

		private void finishPlaying() {
			line.drain();
			line.stop();
			stopped = true;
		}

		public Player start(final int startTime) {
			new Thread(() -> {
				try {
					int startByte = getStartByte(startTime);
					setUpLine();
					final byte[] data = musicData.getBytes();

					while (startByte < data.length) {
						if (stopped) {
							if (playingStartTime < 0) {
								playingStartTime = System.nanoTime();
							}

							return;
						}

						System.out.println(bufferSize - line.available());
						startByte = writeBuffer(data, startByte);

						if (playingStartTime < 0) {
							playingStartTime = System.nanoTime() + Config.delay * 1_000_000;
						}
						waitIfNeeded();
					}

					finishPlaying();
				} catch (final LineUnavailableException | InterruptedException e) {
					Logger.error("exception when playing audio", e);
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
}
