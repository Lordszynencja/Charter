package log.charter.util;

import log.charter.data.managers.selection.ChordOrNote;
import log.charter.song.Chord;
import log.charter.song.Note;

public abstract class Slideable {
	public static Slideable create(final ChordOrNote chordOrNote) {
		if (chordOrNote.chord != null) {
			return new SlideableChord(chordOrNote.chord);
		}

		return new SlideableNote(chordOrNote.note);
	}

	private static class SlideableNote extends Slideable {
		private final Note note;

		public SlideableNote(final Note note) {
			this.note = note;
		}

		@Override
		public Integer fret() {
			if (note.slideTo != null) {
				return note.slideTo;
			}

			if (note.unpitchedSlideTo != null) {
				return note.unpitchedSlideTo;
			}

			return null;
		}

		@Override
		public boolean pitched() {
			return note.unpitchedSlideTo == null;
		}

		@Override
		public void set(final Integer fret, final boolean pitched) {
			if (pitched) {
				note.slideTo = fret;
				note.unpitchedSlideTo = null;
			} else {
				note.slideTo = null;
				note.unpitchedSlideTo = fret;
			}
		}
	}

	private static class SlideableChord extends Slideable {
		private final Chord chord;

		public SlideableChord(final Chord chord) {
			this.chord = chord;
		}

		@Override
		public Integer fret() {
			if (chord.slideTo != null) {
				return chord.slideTo;
			}

			if (chord.unpitchedSlideTo != null) {
				return chord.unpitchedSlideTo;
			}

			return null;
		}

		@Override
		public boolean pitched() {
			return chord.unpitchedSlideTo == null;
		}

		@Override
		public void set(final Integer fret, final boolean pitched) {
			if (pitched) {
				chord.slideTo = fret;
				chord.unpitchedSlideTo = null;
			} else {
				chord.slideTo = null;
				chord.unpitchedSlideTo = fret;
			}
		}
	}

	abstract public Integer fret();

	abstract public boolean pitched();

	abstract public void set(Integer fret, boolean pitched);
}
