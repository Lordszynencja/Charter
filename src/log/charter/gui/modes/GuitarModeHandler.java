package log.charter.gui.modes;

import static java.lang.Math.min;

import java.util.List;

import log.charter.song.Chord;
import log.charter.song.Level;
import log.charter.song.Note;

public class GuitarModeHandler extends ModeHandler {
	@Override
	public void handleHome() {
		if (!chartKeyboardHandler.ctrl()) {
			frame.setNextTime(0);
			return;
		}

		final Level currentLevel = data.getCurrentArrangementLevel();
		final List<Chord> chords = currentLevel.chords;
		final List<Note> notes = currentLevel.notes;

		if (chords.isEmpty()) {
			if (notes.isEmpty()) {
				frame.setNextTime(0);
				return;
			}

			frame.setNextTime(notes.get(0).position);
			return;
		}

		if (notes.isEmpty()) {
			frame.setNextTime(chords.get(0).position);
			return;
		}

		frame.setNextTime(min(chords.get(0).position, notes.get(0).position));
	}
}
