package log.charter.gui.chartPanelDrawers.instruments.guitar;

import static java.lang.Math.max;
import static log.charter.util.ScalingUtils.timeToXLength;

import java.util.Map.Entry;

import log.charter.song.BendValue;
import log.charter.song.ChordTemplate;
import log.charter.song.enums.HOPO;
import log.charter.song.enums.Harmonic;
import log.charter.song.enums.Mute;
import log.charter.song.notes.Chord;
import log.charter.song.notes.ChordNote;
import log.charter.song.notes.Note;
import log.charter.util.CollectionUtils.ArrayList2;

public class EditorNoteDrawingData {
	public static EditorNoteDrawingData fromNote(final int x, final Note note, final boolean selected,
			final boolean lastWasLinkNext) {
		return new EditorNoteDrawingData(x, timeToXLength(note.length()), note, selected, lastWasLinkNext);
	}

	public static EditorNoteDrawingData fromChordNote(final int x, final Chord chord, final ChordTemplate chordTemplate,
			final int string, final ChordNote chordNote, final boolean selected, final boolean lastWasLinkNext,
			final boolean ctrl) {
		final int fret = chordTemplate.frets.get(string);
		final Integer finger = chordTemplate.fingers.get(string);
		final String fretDescription = fret
				+ (ctrl && finger != null ? "(" + (finger == 0 ? "T" : finger.toString()) + ")" : "");

		return new EditorNoteDrawingData(x, timeToXLength(chordNote.length), string, fret, fretDescription, chord,
				chordNote, selected, lastWasLinkNext);
	}

	public static ArrayList2<EditorNoteDrawingData> fromChord(final Chord chord, final ChordTemplate chordTemplate,
			final int x, final boolean selected, final boolean lastWasLinkNext, final boolean ctrl) {
		final ArrayList2<EditorNoteDrawingData> notes = new ArrayList2<>();

		for (final Entry<Integer, ChordNote> chordNoteEntry : chord.chordNotes.entrySet()) {
			notes.add(fromChordNote(x, chord, chordTemplate, chordNoteEntry.getKey(), chordNoteEntry.getValue(),
					selected, lastWasLinkNext, ctrl));
		}

		return notes;
	}

	public int position;
	public final int x;
	public final int length;

	public final int string;
	public final int fretNumber;
	public final String fret;
	public final boolean accent;
	public final Mute mute;
	public final HOPO hopo;
	public final Harmonic harmonic;
	public final ArrayList2<BendValue> bendValues;
	public final Integer slideTo;
	public final boolean unpitchedSlide;
	public final boolean vibrato;
	public final boolean tremolo;

	public final boolean selected;
	public final boolean linkPrevious;
	public final double prebend;

	private EditorNoteDrawingData(final int x, final int length, final Note note, final boolean selected,
			final boolean lastWasLinkNext) {
		this(note.position(), x, length, //
				note.string, note.fret, note.fret + "", //
				note.accent, note.mute, note.hopo, note.harmonic, note.bendValues, note.slideTo, note.unpitchedSlide,
				note.vibrato, note.tremolo, //
				selected, lastWasLinkNext);
	}

	private EditorNoteDrawingData(final int x, final int length, final int string, final int fret,
			final String fretDescription, final Chord chord, final ChordNote chordNote, final boolean selected,
			final boolean lastWasLinkNext) {
		this(chord.position(), x, length, //
				string, fret, fretDescription, //
				chord.accent, chordNote.mute, chordNote.hopo, chordNote.harmonic, chordNote.bendValues,
				chordNote.slideTo, chordNote.unpitchedSlide, chordNote.vibrato, chordNote.tremolo, //
				selected, lastWasLinkNext);
	}

	private EditorNoteDrawingData(final int position, final int x, final int length, //
			final int string, final int fretNumber, final String fret, //
			final boolean accent, final Mute mute, final HOPO hopo, final Harmonic harmonic,
			final ArrayList2<BendValue> bendValues, final Integer slideTo, final boolean unpitchedSlide,
			final boolean vibrato, final boolean tremolo, final boolean selected, final boolean lastWasLinkNext) {
		this.position = position;
		this.x = x;
		this.length = lastWasLinkNext ? max(5, length) : length;

		this.string = string;
		this.fretNumber = fretNumber;
		this.fret = fret;
		this.accent = accent;
		this.mute = mute;
		this.hopo = hopo;
		this.harmonic = harmonic;
		this.bendValues = bendValues == null ? new ArrayList2<>() : bendValues;
		this.slideTo = slideTo;
		this.unpitchedSlide = unpitchedSlide;
		this.vibrato = vibrato;
		this.tremolo = tremolo;

		this.selected = selected;
		linkPrevious = lastWasLinkNext;

		prebend = !this.bendValues.isEmpty() && this.bendValues.get(0).position() == 0
				? this.bendValues.get(0).bendValue.doubleValue()
				: 0;
	}
}