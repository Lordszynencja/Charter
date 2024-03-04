package log.charter.song.notes;

public interface CommonNoteWithFret extends CommonNote {
	static class CommonChordNoteNoteWithFret extends CommonChordNoteNote implements CommonNoteWithFret {
		private final int fret;

		public CommonChordNoteNoteWithFret(final Chord chord, final int string, final int fret) {
			super(chord, string);
			this.fret = fret;
		}

		@Override
		public int fret() {
			return fret;
		}
	}

	static class CommonNoteNoteWithFret extends CommonNoteNote implements CommonNoteWithFret {
		public CommonNoteNoteWithFret(final Note note) {
			super(note);
		}

		@Override
		public int fret() {
			return fret;
		}

	}

	public static CommonNoteWithFret create(final Note note) {
		return new CommonNoteNoteWithFret(note);
	}

	public static CommonNoteWithFret create(final Chord chord, final int string, final int fret) {
		return new CommonChordNoteNoteWithFret(chord, string, fret);
	}

	public int fret();
}
