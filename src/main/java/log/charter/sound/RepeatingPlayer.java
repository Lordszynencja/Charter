package log.charter.sound;

import static javax.sound.sampled.AudioSystem.getLine;

import javax.sound.sampled.DataLine.Info;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import log.charter.io.Logger;

public class RepeatingPlayer implements IPlayer {
	private static final int BUFF_SIZE = 1024 * 128;

	private final MusicData musicData;
	private final SourceDataLine line;

	private boolean playAgain = false;
	private boolean stopped = false;

	public RepeatingPlayer(final MusicData musicData) {
		this.musicData = musicData;
		final Info info = new Info(SourceDataLine.class, musicData.outFormat);
		SourceDataLine sourceDataLine;
		try {
			sourceDataLine = (SourceDataLine) getLine(info);
			sourceDataLine.open(musicData.outFormat);
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
		final byte[] data = musicData.getData();
		int startByte = 0;

		while ((data.length - startByte) > BUFF_SIZE) {
			line.write(data, startByte, BUFF_SIZE);
			startByte += BUFF_SIZE;
		}

		if ((data.length - startByte) > 0) {
			line.write(data, startByte, data.length - startByte);
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
