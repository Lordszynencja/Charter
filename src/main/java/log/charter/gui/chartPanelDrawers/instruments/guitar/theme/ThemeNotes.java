package log.charter.gui.chartPanelDrawers.instruments.guitar.theme;

import log.charter.gui.chartPanelDrawers.data.EditorNoteDrawingData;
import log.charter.song.ChordTemplate;
import log.charter.song.notes.ChordOrNote;
import log.charter.util.Position2D;

public interface ThemeNotes {
	void addNote(final EditorNoteDrawingData note);

	void addSoundHighlight(int x, ChordOrNote originalSound, ChordTemplate template, int string);

	void addNoteAdditionLine(final Position2D from, final Position2D to);

	void addChordName(int x, String chordName);

}
