package log.charter.gui.handlers.midiPlayer;

import log.charter.song.notes.ChordOrNote;

public class MidiChartNotePlayerNoteData {
	public final int noteId;
	public final ChordOrNote sound;
	public final int endPosition;

	public MidiChartNotePlayerNoteData(final int noteId, final ChordOrNote sound, final int endPosition) {
		this.noteId = noteId;
		this.sound = sound;
		this.endPosition = endPosition;
	}
}
