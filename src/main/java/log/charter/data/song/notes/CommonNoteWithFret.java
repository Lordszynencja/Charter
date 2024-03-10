package log.charter.data.song.notes;

public class CommonNoteWithFret extends CommonNote {
	private final int fret;

	public CommonNoteWithFret(final Chord chord, final int string, final int fret) {
		super(chord, string);
		this.fret = fret;
	}

	public CommonNoteWithFret(final Note note) {
		super(note);
		fret = note.fret;
	}

	public int fret() {
		return fret;
	}
}
