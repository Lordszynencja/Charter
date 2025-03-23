package log.charter.gui.components.preview3D.data;

import static java.lang.Math.min;

import java.util.List;

import log.charter.data.song.BendValue;
import log.charter.data.song.enums.HOPO;
import log.charter.data.song.enums.Harmonic;
import log.charter.data.song.enums.Mute;
import log.charter.data.song.notes.Chord;
import log.charter.data.song.notes.ChordNote;
import log.charter.data.song.notes.Note;

public class NoteDrawData {
	public final int id;
	public final double position;
	public final double endPosition;
	public final int string;
	public final int fret;
	public final boolean accent;
	public final Mute mute;
	public final HOPO hopo;
	public final Harmonic harmonic;
	public final double prebend;
	public final List<BendValue> bendValues;
	public final Integer slideTo;
	public final boolean unpitchedSlide;
	public final boolean vibrato;
	public final boolean tremolo;
	public final boolean linkPrevious;

	public final double originalPosition;
	public final double trueLength;
	public final boolean withoutHead;
	public final boolean isChordNote;

	public NoteDrawData(final int id, final double notePosition, final double noteEndPosition, final double minPosition,
			final double maxPosition, final Note note, final boolean linkPrevious) {
		this.id = id;
		originalPosition = notePosition;
		trueLength = noteEndPosition - notePosition;

		if (originalPosition < minPosition) {
			position = minPosition;
			withoutHead = true;
		} else {
			position = originalPosition;
			withoutHead = linkPrevious;
		}
		endPosition = min(maxPosition, noteEndPosition);

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

		prebend = !bendValues.isEmpty() && bendValues.get(0).position().compareTo(note.position()) == 0 //
				? bendValues.get(0).bendValue.doubleValue()//
				: 0;
	}

	public NoteDrawData(final int id, final double chordPosition, final double chordNoteEndPosition,
			final double minPosition, final double maxPosition, final Chord chord, final int string, final int fret,
			final ChordNote chordNote, final boolean linkPrevious, final boolean shouldHaveLength) {
		this.id = id;
		originalPosition = chordPosition;
		trueLength = shouldHaveLength ? chordNoteEndPosition - chordPosition : 0;

		if (originalPosition < minPosition) {
			position = minPosition;
			withoutHead = true;
		} else {
			position = originalPosition;
			withoutHead = linkPrevious;
		}
		endPosition = min(maxPosition, chordNoteEndPosition);
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

		prebend = !bendValues.isEmpty() && bendValues.get(0).position().compareTo(chord.position()) == 0 //
				? bendValues.get(0).bendValue.doubleValue()//
				: 0;
	}

	public NoteDrawData(final double truePosition, final double position, final double endPosition,
			final NoteDrawData note) {
		id = note.id;
		originalPosition = truePosition;
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
	}
}
