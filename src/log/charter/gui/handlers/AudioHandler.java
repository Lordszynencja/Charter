package log.charter.gui.handlers;

import log.charter.data.ChartData;
import log.charter.gui.CharterFrame;
import log.charter.song.enums.Position;
import log.charter.sound.MusicData;
import log.charter.sound.RepeatingPlayer;
import log.charter.sound.RotatingRepeatingPlayer;
import log.charter.sound.SoundPlayer;
import log.charter.sound.SoundPlayer.Player;
import log.charter.util.CollectionUtils.ArrayList2;

public class AudioHandler {
	private static final RepeatingPlayer tickPlayer = new RepeatingPlayer(MusicData.generateSound(4000, 0.01, 1));
	private static final RotatingRepeatingPlayer notePlayers = new RotatingRepeatingPlayer(
			MusicData.generateSound(1000, 0.02, 0.8), 4);

	private ChartData data;
	private CharterFrame frame;
	private KeyboardHandler keyboardHandler;

	private Player songPlayer = null;

	private ArrayList2<Position> noteClapPositions;
	private boolean noteClaps;
	private ArrayList2<Position> metronomePositions;
	private boolean metronome;
	private int songTimeOnStart = 0;
	private int playStartTime;

	public void init(final ChartData data, final CharterFrame frame, final KeyboardHandler keyboardHandler) {
		this.data = data;
		this.frame = frame;
		this.keyboardHandler = keyboardHandler;
	}

	public void toggleClaps() {
		noteClaps = !noteClaps;
	}

	public void toggleMetronome() {
		metronome = !metronome;
	}

	public void playMusic() {
		songPlayer = SoundPlayer.play(data.music, data.time);
		songTimeOnStart = data.time;
		playStartTime = (int) (System.nanoTime() / 1_000_000L);
	}

	public void stopMusic() {
		if (songPlayer != null) {
			songPlayer.stop();
			songPlayer = null;
		}
	}

	private ArrayList2<Position> getPositionsAfterCurrentTime(final ArrayList2<Position> positions) {
		return positions;
	}

	public void switchMusicPlayStatus() {
		if (data.isEmpty) {
			return;
		}

		if (songPlayer != null) {
			stopMusic();
			return;
		}

		// TODO note claps
//			if (data.currentInstrument.type.isVocalsType()) {
//				nextNoteId = data.findClosestVocalForTime(data.nextT);
//				if ((nextNoteId > 0) && (nextNoteId < data.s.v.lyrics.size()) //
//						&& (data.s.v.lyrics.get(nextNoteId).pos < data.nextT)) {
//					nextNoteId++;
//				}
//				if (nextNoteId >= data.s.v.lyrics.size()) {
//					nextNoteId = -1;
//				}
//			} else {
//				nextNoteId = data.findClosestNoteForTime(data.nextT);
//				if ((nextNoteId > 0) && (nextNoteId < data.currentNotes.size()) //
//						&& (data.currentNotes.get(nextNoteId).pos < data.nextT)) {
//					nextNoteId++;
//				}
//				if (nextNoteId >= data.currentNotes.size()) {
//					nextNoteId = -1;
//				}
//			}

//			nextBeatTime = data.songChart.beatsMap.getFirstBeatAfter((int) (data.nextT - Config.delay)).position;

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

		final int timePassed = (int) ((System.nanoTime() / 1_000_000 - playStartTime) * data.music.slowMultiplier());
		final int nextTime = songTimeOnStart + timePassed;
		frame.setNextTime(nextTime);

		final int songTime = nextTime;

		// TODO clap notes
//			final List<? extends Event> notes = data.currentInstrument.type.isVocalsType() ? data.s.v.lyrics
//					: data.currentNotes;
//
//			while ((nextNoteId != -1) && (notes.get(nextNoteId).pos < soundTime)) {
//				nextNoteId++;
//				if (nextNoteId >= notes.size()) {
//					nextNoteId = -1;
//				}
//				if (claps) {
//					notePlayer.queuePlaying();
//				}
//			}

//		while ((nextBeatTime >= 0) && (nextBeatTime < soundTime)) {
//			nextBeatTime = data.songChart.beatsMap.getFirstBeatAfter((int) soundTime).position;
//			if (metronome) {
//				tickPlayer.play();
//			}
//		}
	}
}
