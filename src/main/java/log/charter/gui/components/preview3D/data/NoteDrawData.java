package log.charter.gui.components.preview3D.data;

import static java.lang.Math.min;

import log.charter.song.BendValue;
import log.charter.song.enums.HOPO;
import log.charter.song.enums.Harmonic;
import log.charter.song.enums.Mute;
import log.charter.song.notes.Chord;
import log.charter.song.notes.ChordNote;
import log.charter.song.notes.Note;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.IntRange;

public class NoteDrawData {
	public final int position;
	public final int endPosition;
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

	public final int truePosition;
	public final int trueLength;
	public final boolean withoutHead;
	public final boolean isChordNote;
	public final IntRange frets;

	public NoteDrawData(final int minPosition, final int maxPosition, final Note note, final boolean linkPrevious,
			final IntRange frets) {
		truePosition = note.position();
		trueLength = note.length();

		if (truePosition < minPosition) {
			position = minPosition;
			withoutHead = true;
		} else {
			position = truePosition;
			withoutHead = linkPrevious;
		}
		endPosition = min(maxPosition, note.endPosition());
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
		this.frets = frets;

		prebend = !bendValues.isEmpty() && bendValues.get(0).position() == 0 //
				? bendValues.get(0).bendValue.doubleValue()//
				: 0;
	}

	public NoteDrawData(final int minPosition, final int maxPosition, final Chord chord, final int string,
			final int fret, final ChordNote chordNote, final boolean linkPrevious, final boolean shouldHaveLength,
			final IntRange frets) {
		truePosition = chord.position();
		trueLength = shouldHaveLength ? chordNote.length : 0;

		if (truePosition < minPosition) {
			position = minPosition;
			withoutHead = true;
		} else {
			position = truePosition;
			withoutHead = linkPrevious;
		}
		endPosition = min(maxPosition, chord.endPosition());
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
		this.frets = frets;

		prebend = !bendValues.isEmpty() && bendValues.get(0).position() == 0 //
				? bendValues.get(0).bendValue.doubleValue()//
				: 0;
	}

	public NoteDrawData(final int truePosition, final int position, final int endPosition, final NoteDrawData note) {
		this.truePosition = truePosition;
		trueLength = note.trueLength;

		this.position = position;
		this.endPosition = endPosition;

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

		withoutHead = note.linkPrevious || position > truePosition;
		linkPrevious = note.linkPrevious;
		isChordNote = note.isChordNote;
		prebend = note.prebend;
		frets = note.frets;
	}
}
