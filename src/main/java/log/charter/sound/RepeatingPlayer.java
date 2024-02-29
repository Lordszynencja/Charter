package log.charter.sound;

import static java.lang.Math.min;
import static javax.sound.sampled.AudioSystem.getLine;
import static log.charter.data.config.Config.audioBufferSize;

import java.util.Arrays;
import java.util.function.Supplier;

import javax.sound.sampled.DataLine.Info;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import log.charter.io.Logger;
import log.charter.sound.data.MusicData;

public class RepeatingPlayer implements IPlayer {
	private final Supplier<MusicData<?>> musicDataSupplier;
	private final SourceDataLine line;

	private boolean playAgain = false;
	private boolean stopped = false;

	public RepeatingPlayer(final Supplier<MusicData<?>> musicDataSupplier) {
		this.musicDataSupplier = musicDataSupplier;

		final MusicData<?> musicData = musicDataSupplier.get();
		final Info info = new Info(SourceDataLine.class, musicData.format());
		SourceDataLine sourceDataLine;
		try {
			sourceDataLine = (SourceDataLine) getLine(info);
			sourceDataLine.open(musicData.format());
		} catch (final LineUnavailableException e) {
			Logger.error("Couldn't open line for repeating player", e);
			sourceDataLine = null;
		}
		line = sourceDataLine;
		line.start();

		new Thread(() -> {
			try {
				while (!stopped) {
					if (playAgain) {
						playSound();
					}
					Thread.sleep(1);
				}
			} catch (final InterruptedException e) {
				Logger.error("Sound thread interrupted", e);
			}

		}).start();
	}

	private void playSound() {
		line.flush();
		final byte[] data = musicDataSupplier.get().getBytes();
		int startByte = 0;

		while (startByte < data.length) {
			if (stopped) {
				return;
			}

			final byte[] buffer = Arrays.copyOfRange(data, startByte, min(data.length, startByte + audioBufferSize));
			startByte += line.write(buffer, 0, buffer.length);
		}

		playAgain = false;
	}

	@Override
	public void play() {
		playAgain = true;
	}

	public void stop() {
		stopped = true;
	}

}
