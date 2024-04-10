package log.charter.sound.system;

import static java.lang.Math.min;
import static log.charter.data.config.Config.audioBufferSize;
import static log.charter.sound.data.AudioUtils.fromBytes;
import static log.charter.sound.data.AudioUtils.writeBytes;

import java.util.Arrays;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;

import javax.sound.sampled.AudioFormat;

import log.charter.data.config.Config;
import log.charter.io.Logger;
import log.charter.sound.data.AudioData;
import log.charter.sound.data.AudioUtils;
import log.charter.sound.system.SoundSystem.ISoundSystem.ISoundLine;

public class SoundSystem {
	private static int playerId = 0;

	public interface ISoundSystem {
		public interface ISoundLine {
			public int write(byte[] bytes);

			void close();

			void stop();

			boolean wantsMoreData();
		}

		public static class EmptySoundLine implements ISoundLine {
			@Override
			public int write(final byte[] bytes) {
				return bytes.length;
			}

			@Override
			public void close() {
			}

			@Override
			public void stop() {
			}

			@Override
			public boolean wantsMoreData() {
				return true;
			}

		}

		ISoundLine getNewLine(AudioFormat format);
	}

	private static ISoundSystem currentSoundSystem = new StandardSoundSystem();

	public static void setCurrentSoundSystem() {
		currentSoundSystem = switch (Config.audioSystemType) {
			case ASIO -> new ASIOSoundSystem(Config.audioSystemName, Config.leftOutChannelId, Config.rightOutChannelId);
			default -> new StandardSoundSystem();
		};
	}

	public static ISoundSystem getCurrentSoundSystem() {
		return currentSoundSystem;
	}

	public static class Player {
		private final AudioData<?> musicData;
		private final DoubleSupplier volume;
		private final IntSupplier speed;

		private final int sampleSizeInBits;
		private final int sampleSize;
		private final int frameSize;
		private final ISoundLine line;
		private boolean stopped;

		public long playingStartTime = -1;

		private Player(final AudioData<?> musicData, final DoubleSupplier volume, final IntSupplier speed) {
			this.musicData = musicData;
			this.volume = volume;
			this.speed = speed;

			sampleSizeInBits = musicData.format().getSampleSizeInBits();
			if (sampleSizeInBits <= 8) {
				sampleSize = 1;
			} else {
				sampleSize = 2;
			}
			frameSize = musicData.format().getFrameSize();

			final ISoundSystem soundSystem = getCurrentSoundSystem();
			line = soundSystem.getNewLine(musicData.format());
		}

		public boolean isStopped() {
			return stopped;
		}

		private int getStartByte(final double startTime) {
			final int startFrame = (int) (musicData.frameRate() * startTime / 1000);
			return startFrame * frameSize;
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
		
		private byte[] stretch(final byte[] buffer) {
			System.out.println(this.speed.getAsInt());
			return buffer;
		}

		private int writeBuffer(final byte[] data, int startByte) {
			final byte[] buffer = Arrays.copyOfRange(data, startByte, min(data.length, startByte + audioBufferSize));
			
			setVolume(buffer);
			
			final byte[] stretchedBuffer = stretch(buffer);

			startByte += line.write(buffer);
			return startByte;
		}

		private void waitIfNeeded() throws InterruptedException {
			while (!line.wantsMoreData()) {
				Thread.sleep(1);
			}
		}

		private void finishPlaying() {
			line.close();
			stopped = true;
		}

		private void playSound(int startByte) throws InterruptedException {
			final byte[] data = musicData.getBytes();

			while (startByte < data.length) {
				if (stopped) {
					if (playingStartTime < 0) {
						playingStartTime = System.nanoTime();
					}

					return;
				}

				startByte = writeBuffer(data, startByte);

				if (playingStartTime < 0) {
					playingStartTime = System.nanoTime() + Config.delay * 1_000_000;
				}
				waitIfNeeded();
			}

			finishPlaying();
		}

		private Player start(final double startTime) {
			final Thread thread = new Thread(() -> {
				try {
					playSound(getStartByte(startTime));
				} catch (final InterruptedException e) {
					Logger.error("playing audio interrupted", e);
					line.stop();
				}
			});
			thread.setName("Sound thread " + playerId++);
			thread.setPriority(Thread.MAX_PRIORITY);
			thread.start();

			return this;
		}

		public void stop() {
			if (stopped) {
				return;
			}

			stopped = true;
			line.stop();
		}
	}

	public static Player play(final AudioData<?> audioData, final DoubleSupplier volumeSupplier, final IntSupplier speed) {
		return play(audioData, volumeSupplier, speed, 0);
	}

	public static Player play(final AudioData<?> audioData, final DoubleSupplier volumeSupplier, final IntSupplier speed,
			final double startTime) {
		return new Player(audioData, volumeSupplier, speed).start(startTime);
	}
}