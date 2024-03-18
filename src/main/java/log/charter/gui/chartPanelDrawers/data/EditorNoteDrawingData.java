package log.charter.gui.chartPanelDrawers.data;

import static java.lang.Math.max;
import static log.charter.util.CollectionUtils.map;
import static log.charter.util.ScalingUtils.positionToX;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.enums.BassPickingTechnique;
import log.charter.data.song.enums.HOPO;
import log.charter.data.song.enums.Harmonic;
import log.charter.data.song.enums.Mute;
import log.charter.data.song.notes.Chord;
import log.charter.data.song.notes.ChordNote;
import log.charter.data.song.notes.Note;

public class EditorNoteDrawingData {
	public static class EditorBendValueDrawingData {
		public final int x;
		public final BigDecimal bendValue;

		public EditorBendValueDrawingData(final int x, final BigDecimal bendValue) {
			this.x = x;
			this.bendValue = bendValue;
		}
	}

	public static EditorNoteDrawingData fromNote(final ImmutableBeatsMap beats, final int time, final Note note,
			final boolean selected, final boolean highlighted, final boolean lastWasLinkNext,
			final boolean wrongLinkNext) {
		final int x = positionToX(note.position(beats), time);
		final int length = positionToX(note.endPosition(beats), time) - x;
		final List<EditorBendValueDrawingData> bends = map(note.bendValues,
				b -> new EditorBendValueDrawingData(positionToX(b.position(beats), time), b.bendValue));

		return new EditorNoteDrawingData(x, length, //
				note.string, note.fret, note.fret + "", //
				note.accent, note.mute, note.hopo, note.harmonic, note.bassPicking, //
				bends, note.slideTo, note.unpitchedSlide, note.vibrato, note.tremolo, //
				selected, highlighted, lastWasLinkNext, wrongLinkNext);
	}

	public static EditorNoteDrawingData fromChordNote(final ImmutableBeatsMap beats, final int time, final Chord chord,
			final ChordTemplate chordTemplate, final int x, final int string, final ChordNote chordNote,
			final boolean selected, final boolean highlighted, final boolean lastWasLinkNext,
			final boolean wrongLinkNext, final boolean ctrl) {
		final int length = positionToX(chordNote.endPosition(beats), time) - x;
		final int fret = chordTemplate.frets.get(string);
		final Integer finger = chordTemplate.fingers.get(string);
		final String fretDescription = fret
				+ (ctrl && finger != null ? "(" + (finger == 0 ? "T" : finger.toString()) + ")" : "");
		final List<EditorBendValueDrawingData> bends = map(chordNote.bendValues,
				b -> new EditorBendValueDrawingData(positionToX(b.position(beats), time), b.bendValue));

		return new EditorNoteDrawingData(x, length, //
				string, fret, fretDescription, //
				chord.accent, chordNote.mute, chordNote.hopo, chordNote.harmonic, BassPickingTechnique.NONE, //
				bends, chordNote.slideTo, chordNote.unpitchedSlide, chordNote.vibrato, chordNote.tremolo, //
				selected, highlighted, lastWasLinkNext, wrongLinkNext);
	}

	public static List<EditorNoteDrawingData> fromChord(final ImmutableBeatsMap beats, final int time,
			final Chord chord, final ChordTemplate chordTemplate, final int x, final boolean selected,
			final int highlightedString, final boolean lastWasLinkNext, final boolean wrongLinkNext,
			final boolean ctrl) {
		final List<EditorNoteDrawingData> notes = new ArrayList<>();

		for (final Entry<Integer, ChordNote> chordNoteEntry : chord.chordNotes.entrySet()) {
			final int string = chordNoteEntry.getKey();
			notes.add(fromChordNote(beats, time, chord, chordTemplate, x, string, chordNoteEntry.getValue(), selected,
					highlightedString == string, lastWasLinkNext, wrongLinkNext, ctrl));
		}

		return notes;
	}

	public final int x;
	public final int length;

	public final int string;
	public final int fretNumber;
	public final String fret;
	public final boolean accent;
	public final Mute mute;
	public final HOPO hopo;
	public final Harmonic harmonic;
	public final BassPickingTechnique bassPickingTech;
	public final List<EditorBendValueDrawingData> bendValues;
	public final Integer slideTo;
	public final boolean unpitchedSlide;
	public final boolean vibrato;
	public final boolean tremolo;

	public final boolean selected;
	public final boolean highlighted;
	public final boolean linkPrevious;
	public final boolean wrongLink;
	public final double prebend;

	private EditorNoteDrawingData(final int x, final int length, //
			final int string, final int fretNumber, final String fret, //
			final boolean accent, final Mute mute, final HOPO hopo, final Harmonic harmonic,
			final BassPickingTechnique bassPickingTech, final List<EditorBendValueDrawingData> bendValues,
			final Integer slideTo, final boolean unpitchedSlide, final boolean vibrato, final boolean tremolo,
			final boolean selected, final boolean highlighted, final boolean lastWasLinkNext, final boolean wrongLink) {
		this.x = x;
		this.length = lastWasLinkNext ? max(5, length) : length;

		this.string = string;
		this.fretNumber = fretNumber;
		this.fret = fret;
		this.accent = accent;
		this.mute = mute;
		this.hopo = hopo;
		this.harmonic = harmonic;
		this.bassPickingTech = bassPickingTech;
		this.bendValues = bendValues == null ? new ArrayList<>() : bendValues;
		this.slideTo = slideTo;
		this.unpitchedSlide = unpitchedSlide;
		this.vibrato = vibrato;
		this.tremolo = tremolo;

		this.selected = selected;
		this.highlighted = highlighted;
		linkPrevious = lastWasLinkNext;
		this.wrongLink = wrongLink;

		if (this.bendValues.isEmpty()) {
			prebend = 0;
		} else {
			final EditorBendValueDrawingData firstBend = this.bendValues.get(0);
			prebend = firstBend.x - x == 0 ? firstBend.bendValue.doubleValue() : 0;
		}
	}
}