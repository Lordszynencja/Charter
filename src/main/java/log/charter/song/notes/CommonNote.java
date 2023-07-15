package log.charter.song.notes;

import log.charter.song.BendValue;
import log.charter.song.enums.HOPO;
import log.charter.song.enums.Harmonic;
import log.charter.song.enums.Mute;
import log.charter.util.CollectionUtils.ArrayList2;

public interface CommonNote {
	static class CommonChordNoteNote implements CommonNote {
		private final Chord chord;
		private final int string;
		private final ChordNote note;

		public CommonChordNoteNote(final Chord chord, final int string, final ChordNote note) {
			this.chord = chord;
			this.string = string;
			this.note = note;
		}

		@Override
		public int position() {
			return chord.position();
		}

		@Override
		public int length() {
			return note.length;
		}

		@Override
		public void length(final int length) {
			note.length = length;
		}

		@Override
		public int string() {
			return string;
		}

		@Override
		public Mute mute() {
			return note.mute;
		}

		@Override
		public void mute(final Mute mute) {
			note.mute = mute;
		}

		@Override
		public HOPO hopo() {
			return note.hopo;
		}

		@Override
		public void hopo(final HOPO hopo) {
			note.hopo = hopo;
		}

		@Override
		public Harmonic harmonic() {
			return note.harmonic;
		}

		@Override
		public void harmonic(final Harmonic harmonic) {
			note.harmonic = harmonic;
		}

		@Override
		public boolean vibrato() {
			return note.vibrato;
		}

		@Override
		public void vibrato(final boolean vibrato) {
			note.vibrato = vibrato;
		}

		@Override
		public boolean tremolo() {
			return note.tremolo;
		}

		@Override
		public void tremolo(final boolean tremolo) {
			note.tremolo = tremolo;
		}

		@Override
		public boolean linkNext() {
			return note.linkNext;
		}

		@Override
		public void linkNext(final boolean linkNext) {
			note.linkNext = linkNext;
		}

		@Override
		public boolean unpitchedSlide() {
			return note.unpitchedSlide;
		}

		@Override
		public void unpitchedSlide(final boolean unpitchedSlide) {
			note.unpitchedSlide = unpitchedSlide;
		}

		@Override
		public ArrayList2<BendValue> bendValues() {
			return note.bendValues;
		}

		@Override
		public void bendValues(final ArrayList2<BendValue> bendValues) {
			note.bendValues = bendValues;
		}

		@Override
		public boolean passOtherNotes() {
			return chord.passOtherNotes;
		}
	}

	static class CommonNoteNote implements CommonNote {
		private final Note note;

		public CommonNoteNote(final Note note) {
			this.note = note;
		}

		@Override
		public int position() {
			return note.position();
		}

		@Override
		public int length() {
			return note.length();
		}

		@Override
		public void length(final int length) {
			note.length(length);
		}

		@Override
		public int string() {
			return note.string;
		}

		@Override
		public Mute mute() {
			return note.mute;
		}

		@Override
		public void mute(final Mute mute) {
			note.mute = mute;
		}

		@Override
		public HOPO hopo() {
			return note.hopo;
		}

		@Override
		public void hopo(final HOPO hopo) {
			note.hopo = hopo;
		}

		@Override
		public Harmonic harmonic() {
			return note.harmonic;
		}

		@Override
		public void harmonic(final Harmonic harmonic) {
			note.harmonic = harmonic;
		}

		@Override
		public boolean vibrato() {
			return note.vibrato;
		}

		@Override
		public void vibrato(final boolean vibrato) {
			note.vibrato = vibrato;
		}

		@Override
		public boolean tremolo() {
			return note.tremolo;
		}

		@Override
		public void tremolo(final boolean tremolo) {
			note.tremolo = tremolo;
		}

		@Override
		public boolean linkNext() {
			return note.linkNext;
		}

		@Override
		public void linkNext(final boolean linkNext) {
			note.linkNext = linkNext;
		}

		@Override
		public boolean unpitchedSlide() {
			return note.unpitchedSlide;
		}

		@Override
		public void unpitchedSlide(final boolean unpitchedSlide) {
			note.unpitchedSlide = unpitchedSlide;
		}

		@Override
		public ArrayList2<BendValue> bendValues() {
			return note.bendValues;
		}

		@Override
		public void bendValues(final ArrayList2<BendValue> bendValues) {
			note.bendValues = bendValues;
		}

		@Override
		public boolean passOtherNotes() {
			return note.passOtherNotes;
		}

	}

	static CommonNote create(final Note note) {
		return new CommonNoteNote(note);
	}

	static CommonNote create(final Chord chord, final int string, final ChordNote chordNote) {
		return new CommonChordNoteNote(chord, string, chordNote);
	}

	public int position();

	public int length();

	public void length(int length);

	public default int endPosition() {
		return position() + length();
	}

	public default void endPosition(final int endPosition) {
		length(endPosition - position());
	}

	public int string();

	public Mute mute();

	public void mute(Mute mute);

	public HOPO hopo();

	public void hopo(HOPO hopo);

	public Harmonic harmonic();

	public void harmonic(Harmonic harmonic);

	public boolean vibrato();

	public void vibrato(boolean vibrato);

	public boolean tremolo();

	public void tremolo(boolean tremolo);

	public boolean linkNext();

	public void linkNext(boolean linkNext);

	public boolean unpitchedSlide();

	public void unpitchedSlide(boolean unpitchedSlide);

	public ArrayList2<BendValue> bendValues();

	public void bendValues(ArrayList2<BendValue> bendValues);

	public boolean passOtherNotes();
}
