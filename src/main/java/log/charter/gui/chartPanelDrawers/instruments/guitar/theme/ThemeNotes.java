package log.charter.gui.chartPanelDrawers.instruments.guitar.theme;

import java.util.Optional;

import log.charter.data.song.ChordTemplate;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.gui.chartPanelDrawers.data.EditorNoteDrawingData;
import log.charter.util.data.Position2D;

public interface ThemeNotes {
	void addNote(final EditorNoteDrawingData note);

	void addSoundHighlight(int x, int length, Optional<ChordOrNote> originalSound, Optional<ChordTemplate> template,
			int string, boolean drawOriginalStrings);

	void addNoteAdditionLine(final Position2D from, final Position2D to);

	void addChordName(int x, String chordName);

}
