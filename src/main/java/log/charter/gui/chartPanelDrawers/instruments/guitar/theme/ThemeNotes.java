package log.charter.gui.chartPanelDrawers.instruments.guitar.theme;

import java.util.Optional;

import log.charter.data.song.ChordTemplate;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.gui.chartPanelDrawers.data.EditorNoteDrawingData;
import log.charter.util.Position2D;

public interface ThemeNotes {
	void addNote(final EditorNoteDrawingData note);

	void addSoundHighlight(int x, Optional<ChordOrNote> originalSound, Optional<ChordTemplate> template, int string);

	void addNoteAdditionLine(final Position2D from, final Position2D to);

	void addChordName(int x, String chordName);

}
