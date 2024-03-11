package log.charter.services.audio;

import log.charter.data.song.notes.ChordOrNote;

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
