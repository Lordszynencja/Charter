package log.charter.gui.chartPanelDrawers.instruments.guitar;

import static java.lang.Math.max;
import static java.util.stream.Collectors.minBy;

import java.util.Map.Entry;

import log.charter.song.BendValue;
import log.charter.song.ChordTemplate;
import log.charter.song.enums.HOPO;
import log.charter.song.enums.Harmonic;
import log.charter.song.enums.Mute;
import log.charter.song.notes.Chord;
import log.charter.song.notes.GuitarSound;
import log.charter.song.notes.Note;
import log.charter.util.CollectionUtils.ArrayList2;

class NoteData {
	public static ArrayList2<NoteData> fromChord(final Chord chord, final ChordTemplate chordTemplate, final int x,
			final int length, final boolean selected, final boolean lastWasLinkNext, final boolean ctrl) {
		final ArrayList2<NoteData> notes = new ArrayList2<>();
		Integer slideDistance;
		try {
			slideDistance = chord.slideTo == null ? null//
					: chord.slideTo - chordTemplate.frets.values().stream().collect(minBy(Integer::compare)).get();
		} catch (final Exception e) {
			slideDistance = null;
		}

		for (final Entry<Integer, Integer> chordFret : chordTemplate.frets.entrySet()) {
			final int string = chordFret.getKey();
			final int fret = chordFret.getValue();
			final Integer finger = chordTemplate.fingers.get(string);
			final String fretDescription = fret
					+ (ctrl && finger != null ? "(" + (finger == 0 ? "T" : finger.toString()) + ")" : "");
			final Integer slideTo = slideDistance == null ? null : (fret + slideDistance);

			notes.add(new NoteData(x, length, string, fret, fretDescription, chord, chord.bendValues.get(string),
					slideTo, null, selected, lastWasLinkNext));
		}

		return notes;
	}

	public final int position;
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
	public final Integer vibrato;
	public final boolean tremolo;

	public final boolean selected;
	public final boolean linkPrevious;

	public NoteData(final int x, final int length, final Note note, final boolean selected,
			final boolean lastWasLinkNext) {
		this(x, length, note.string, note.fret, note.fret + "", note, note.bendValues, note.slideTo, note.vibrato,
				selected, lastWasLinkNext);
	}

	public NoteData(final int x, final int length, final int string, final int fret, final String fretDescription,
			final GuitarSound sound, final ArrayList2<BendValue> bendValues, final Integer slideTo,
			final Integer vibrato, final boolean selected, final boolean lastWasLinkNext) {
		this(sound.position(), x, length, string, fret, fretDescription, sound.accent, sound.mute, sound.hopo,
				sound.harmonic, bendValues, slideTo, sound.unpitchedSlide, vibrato, sound.tremolo, selected,
				lastWasLinkNext);
	}

	private NoteData(final int position, final int x, final int length, final int string, final int fretNumber,
			final String fret, final boolean accent, final Mute mute, final HOPO hopo, final Harmonic harmonic,
			final ArrayList2<BendValue> bendValues, final Integer slideTo, final boolean unpitchedSlide,
			final Integer vibrato, final boolean tremolo, final boolean selected, final boolean lastWasLinkNext) {
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
		this.bendValues = bendValues;
		this.slideTo = slideTo;
		this.unpitchedSlide = unpitchedSlide;
		this.vibrato = vibrato;
		this.tremolo = tremolo;

		this.selected = selected;
		linkPrevious = lastWasLinkNext;
	}
}