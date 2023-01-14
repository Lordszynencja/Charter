package log.charter.gui.handlers;

import static java.lang.System.nanoTime;
import static log.charter.song.notes.IPosition.findFirstAfter;
import static log.charter.sound.MusicData.generateSound;

import java.util.function.Supplier;

import log.charter.data.ChartData;
import log.charter.gui.CharterFrame;
import log.charter.song.notes.IPosition;
import log.charter.sound.IPlayer;
import log.charter.sound.RepeatingPlayer;
import log.charter.sound.RotatingRepeatingPlayer;
import log.charter.sound.SoundPlayer;
import log.charter.sound.SoundPlayer.Player;
import log.charter.util.CollectionUtils.ArrayList2;

public class AudioHandler {
	private static class TickPlayer {
		private final IPlayer tickPlayer;
		private final Supplier<ArrayList2<? extends IPosition>> positionsSupplier;

		public boolean on = false;
		private int nextTime = -1;

		public TickPlayer(final IPlayer tickPlayer, final Supplier<ArrayList2<? extends IPosition>> positionsSupplier) {
			this.tickPlayer = tickPlayer;
			this.positionsSupplier = positionsSupplier;
		}

		public void handleFrame(final int t) {
			if (nextTime != -1 && nextTime < t) {
				tickPlayer.play();
				nextTime = -1;
			}

			if (on && nextTime == -1) {
				final IPosition nextPosition = findFirstAfter(positionsSupplier.get(), t);
				if (nextPosition != null) {
					nextTime = nextPosition.position();
				}
			}
		}

		public void stop() {
			nextTime = -1;
		}
	}

	private ChartData data;
	private CharterFrame frame;
	private KeyboardHandler keyboardHandler;

	private TickPlayer beatTickPlayer;
	private TickPlayer noteTickPlayer;
	private Player songPlayer;

	private int songTimeOnStart = 0;
	private long playStartTime;

	public void init(final ChartData data, final CharterFrame frame, final KeyboardHandler keyboardHandler) {
		this.data = data;
		this.frame = frame;
		this.keyboardHandler = keyboardHandler;

		beatTickPlayer = new TickPlayer(new RepeatingPlayer(generateSound(4000, 0.01, 1)), //
				() -> data.songChart.beatsMap.beats);
		noteTickPlayer = new TickPlayer(new RotatingRepeatingPlayer(generateSound(1000, 0.02, 0.8), 4), //
				() -> data.getCurrentArrangementLevel().chordsAndNotes);
	}

	public void toggleClaps() {
		noteTickPlayer.on = !noteTickPlayer.on;
	}

	public void toggleMetronome() {
		beatTickPlayer.on = !beatTickPlayer.on;
	}

	public void playMusic() {
		songPlayer = SoundPlayer.play(data.music, data.time);
		songTimeOnStart = data.time;
		playStartTime = nanoTime() / 1_000_000L;
	}

	public void stopMusic() {
		if (songPlayer != null) {
			songPlayer.stop();
			songPlayer = null;
			beatTickPlayer.stop();
			noteTickPlayer.stop();
		}
	}

	public void switchMusicPlayStatus() {
		if (data.isEmpty) {
			return;
		}

		if (songPlayer != null) {
			stopMusic();
			return;
		}

		if (keyboardHandler.ctrl()) {
			data.music.setSlow(2);
		} else {
			data.music.setSlow(1);
		}

		playMusic();
	}

	public void frame() {
		if (songPlayer == null) {
			return;
		}
		if (songPlayer.isStopped()) {
			stopMusic();
		}

		final int timePassed = (int) ((nanoTime() / 1_000_000 - playStartTime) * data.music.slowMultiplier());
		final int nextTime = songTimeOnStart + timePassed;
		frame.setNextTime(nextTime);

		beatTickPlayer.handleFrame(nextTime);
		noteTickPlayer.handleFrame(nextTime);
	}
}
