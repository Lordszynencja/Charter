package log.charter.gui.components.preview3D.data;

import log.charter.song.BendValue;
import log.charter.song.enums.HOPO;
import log.charter.song.enums.Harmonic;
import log.charter.song.enums.Mute;
import log.charter.song.notes.Chord;
import log.charter.song.notes.ChordNote;
import log.charter.song.notes.Note;
import log.charter.util.CollectionUtils.ArrayList2;

public class Preview3DNoteData {
	public final int position;
	public final int length;
	public final int string;
	public final int fret;
	public final boolean accent;
	public final Mute mute;
	public final HOPO hopo;
	public final Harmonic harmonic;
	public final double prebend;
	public final ArrayList2<BendValue> bendValues;
	public final Integer slideTo;
	public final boolean unpitchedSlide;
	public final boolean vibrato;
	public final boolean tremolo;
	public final boolean linkPrevious;
	public final boolean isChordNote;

	public Preview3DNoteData(final Note note, final boolean linkPrevious) {
		position = note.position();
		length = note.length();
		string = note.string;
		fret = note.fret;
		accent = note.accent;
		mute = note.mute;
		hopo = note.hopo;
		harmonic = note.harmonic;
		bendValues = note.bendValues;
		slideTo = note.slideTo;
		unpitchedSlide = note.unpitchedSlide;
		vibrato = note.vibrato;
		tremolo = note.tremolo;
		this.linkPrevious = linkPrevious;
		isChordNote = false;

		prebend = !bendValues.isEmpty() && bendValues.get(0).position() == 0 //
				? bendValues.get(0).bendValue.doubleValue()//
				: 0;
	}

	public Preview3DNoteData(final Chord chord, final int string, final int fret, final ChordNote chordNote,
			final boolean linkPrevious, final boolean shouldHaveLength) {
		position = chord.position();
		length = shouldHaveLength ? chordNote.length : 0;
		this.string = string;
		this.fret = fret;
		accent = chord.accent;
		mute = chordNote.mute;
		hopo = chordNote.hopo;
		harmonic = chordNote.harmonic;
		bendValues = chordNote.bendValues;
		slideTo = chordNote.slideTo;
		unpitchedSlide = chordNote.unpitchedSlide;
		vibrato = chordNote.vibrato;
		tremolo = chordNote.tremolo;
		this.linkPrevious = linkPrevious;
		isChordNote = !chord.splitIntoNotes;

		prebend = !bendValues.isEmpty() && bendValues.get(0).position() == 0 //
				? bendValues.get(0).bendValue.doubleValue()//
				: 0;
	}
}
