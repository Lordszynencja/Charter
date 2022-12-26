package log.charter.data.managers.selection;

import log.charter.song.enums.Position;
import log.charter.song.notes.Chord;
import log.charter.song.notes.Note;

public class ChordOrNote extends Position {
	public Chord chord;
	public Note note;

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
		return chord != null ? chord.length : note.length;
	}

	public ChordOrNote(final ChordOrNote other) {
		super(other);
		chord = other.chord == null ? null : new Chord(other.chord);
		note = other.note == null ? null : new Note(other.note);
	}

	public boolean isChord() {
		return chord != null;
	}

	public boolean isNote() {
		return note != null;
	}
}
