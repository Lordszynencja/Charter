package log.charter.data.song.notes;

public class CommonNoteWithFret extends CommonNote {
	private final int fret;
	private final Integer finger;

	public CommonNoteWithFret(final Chord chord, final int string, final int fret, final Integer finger) {
		super(chord, string);
		this.fret = fret;
		this.finger = finger;
	}

	public CommonNoteWithFret(final Note note) {
		super(note);
		fret = note.fret;
		finger = null;
	}

	public int fret() {
		return fret;
	}

	@Override
	public Integer finger() {
		return finger;
	}
}
