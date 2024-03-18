package log.charter.services.audio;

import log.charter.data.song.notes.ChordOrNote;

public class MidiChartNotePlayerNoteData {
	public final int noteId;
	public final ChordOrNote sound;
	public final int position;
	public final int endPosition;

	public MidiChartNotePlayerNoteData(final int noteId, final ChordOrNote sound, final int position,
			final int endPosition) {
		this.noteId = noteId;
		this.sound = sound;
		this.position = position;
		this.endPosition = endPosition;
	}
}
