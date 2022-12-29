package log.charter.song.notes;

public class ChordOrNote implements IPositionWithLength {
	public Chord chord;
	public Note note;

	public ChordOrNote(final Chord chord) {
		this.chord = chord;
		note = null;
	}

	public ChordOrNote(final Note note) {
		chord = null;
		this.note = note;
	}

	public ChordOrNote(final ChordOrNote other) {
		chord = other.chord == null ? null : new Chord(other.chord);
		note = other.note == null ? null : new Note(other.note);
	}

	public boolean isChord() {
		return chord != null;
	}

	public boolean isNote() {
		return note != null;
	}

	public GuitarSound asGuitarSound() {
		return isChord() ? chord : note;
	}

	@Override
	public int position() {
		return asGuitarSound().position();
	}

	@Override
	public void position(final int newPosition) {
		asGuitarSound().position(newPosition);
	}

	@Override
	public int length() {
		return asGuitarSound().length();
	}

	@Override
	public void length(final int newLength) {
		asGuitarSound().length(newLength);
	}

}
