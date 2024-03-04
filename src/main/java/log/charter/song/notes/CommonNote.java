package log.charter.song.notes;

public interface CommonNote extends NoteInterface {
	static class CommonChordNoteNote extends ChordNote implements CommonNote {
		private final Chord chord;
		private final int string;

		public CommonChordNoteNote(final Chord chord, final int string) {
			super(chord.chordNotes.get(string));
			this.chord = chord;
			this.string = string;
		}

		@Override
		public int position() {
			return chord.position();
		}

		@Override
		public int endPosition() {
			return position() + length();
		}

		@Override
		public void endPosition(final int endPosition) {
			length(endPosition - position());
		}

		@Override
		public int string() {
			return string;
		}

		@Override
		public boolean passOtherNotes() {
			return chord.passOtherNotes;
		}
	}

	static class CommonNoteNote extends Note implements CommonNote {
		public CommonNoteNote(final Note note) {
			super(note);
		}

		@Override
		public int string() {
			return string;
		}

		@Override
		public boolean passOtherNotes() {
			return passOtherNotes;
		}

		@Override
		public int endPosition() {
			return position() + length();
		}

		@Override
		public void endPosition(final int endPosition) {
			length(endPosition - position());
		}
	}

	public static CommonNote create(final Note note) {
		return new CommonNoteNote(note);
	}

	public static CommonNote create(final Chord chord, final int string) {
		return new CommonChordNoteNote(chord, string);
	}

	public int position();

	public int endPosition();

	public void endPosition(final int endPosition);

	public int string();

	boolean passOtherNotes();
}
