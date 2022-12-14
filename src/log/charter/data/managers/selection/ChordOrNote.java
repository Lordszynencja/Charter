package log.charter.data.managers.selection;

import log.charter.song.Chord;
import log.charter.song.Note;
import log.charter.song.Position;

public class ChordOrNote extends Position {
	public final Chord chord;
	public final Note note;

	public ChordOrNote(final Chord chord) {
		super(chord);
		this.chord = chord;
		note = null;
	}

	public ChordOrNote(final Note note) {
		super(note);
		chord = null;
		this.note = note;
	}

	public int length() {
		return chord != null ? chord.length() : note.sustain;
	}
}
