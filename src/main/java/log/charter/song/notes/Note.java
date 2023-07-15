package log.charter.song.notes;

import static log.charter.util.Utils.mapInteger;

import log.charter.io.rs.xml.song.ArrangementNote;
import log.charter.song.BendValue;
import log.charter.song.ChordTemplate;
import log.charter.song.enums.BassPickingTechnique;
import log.charter.song.enums.HOPO;
import log.charter.song.enums.Harmonic;
import log.charter.song.enums.Mute;
import log.charter.util.CollectionUtils.ArrayList2;

public class Note extends GuitarSound {
	public int string;
	public int fret;
	public BassPickingTechnique bassPicking = BassPickingTechnique.NONE;
	public Mute mute = Mute.NONE;
	public HOPO hopo = HOPO.NONE;
	public Harmonic harmonic = Harmonic.NONE;
	public boolean vibrato = false;
	public boolean tremolo = false;
	public boolean linkNext = false;
	public Integer slideTo = null;
	public boolean unpitchedSlide = false;
	public ArrayList2<BendValue> bendValues = new ArrayList2<>();

	public Note(final int pos, final int string, final int fret) {
		super(pos);
		this.string = string;
		this.fret = fret;
	}

	public Note(final ArrangementNote arrangementNote) {
		super(arrangementNote.time, arrangementNote.sustain == null ? 0 : arrangementNote.sustain,
				mapInteger(arrangementNote.accent), mapInteger(arrangementNote.ignore));

		string = arrangementNote.string;
		fret = arrangementNote.fret;
		bassPicking = BassPickingTechnique.fromArrangmentNote(arrangementNote);
		mute = Mute.fromArrangmentNote(arrangementNote);
		hopo = HOPO.fromArrangmentNote(arrangementNote);
		harmonic = Harmonic.fromArrangmentNote(arrangementNote);
		vibrato = mapInteger(arrangementNote.vibrato);
		tremolo = mapInteger(arrangementNote.tremolo);
		linkNext = mapInteger(arrangementNote.linkNext);
		slideTo = arrangementNote.slideTo == null ? arrangementNote.slideUnpitchTo : arrangementNote.slideTo;
		unpitchedSlide = arrangementNote.slideUnpitchTo != null;
		bendValues = arrangementNote.bendValues == null ? new ArrayList2<>()
				: arrangementNote.bendValues.list
						.map(arrangementBendValue -> new BendValue(arrangementBendValue, arrangementNote.time));
	}

	public Note(final Note other) {
		super(other);

		string = other.string;
		fret = other.fret;
		bassPicking = other.bassPicking;
		bendValues = other.bendValues.map(BendValue::new);
	}

	public Note(final Chord chord, final ChordTemplate template) {
		super(chord);

		string = template.frets.keySet().stream().min(Integer::compare).orElse(0);
		fret = template.frets.get(string);

		final ChordNote chordNote = chord.chordNotes.get(string);
		mute = chordNote.mute;
		hopo = chordNote.hopo;
		harmonic = chordNote.harmonic;
		vibrato = chordNote.vibrato;
		tremolo = chordNote.tremolo;
		linkNext = chordNote.linkNext;
		slideTo = chordNote.slideTo;
		unpitchedSlide = chordNote.unpitchedSlide;
		bendValues = chordNote.bendValues.map(BendValue::new);
	}
}
