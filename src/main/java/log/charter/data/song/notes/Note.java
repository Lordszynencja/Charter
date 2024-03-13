package log.charter.data.song.notes;

import static log.charter.util.Utils.mapInteger;

import java.util.stream.Collectors;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.data.song.BendValue;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.enums.BassPickingTechnique;
import log.charter.data.song.enums.HOPO;
import log.charter.data.song.enums.Harmonic;
import log.charter.data.song.enums.Mute;
import log.charter.io.rs.xml.song.ArrangementNote;
import log.charter.io.rsc.xml.converters.NoteConverter;
import log.charter.util.collections.ArrayList2;

@XStreamAlias("note")
@XStreamConverter(NoteConverter.class)
public class Note extends GuitarSound implements NoteInterface {
	public int string = 0;
	public int fret = 0;
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
				: arrangementNote.bendValues.list.stream()//
						.map(arrangementBendValue -> new BendValue(arrangementBendValue, arrangementNote.time))//
						.collect(Collectors.toCollection(ArrayList2::new));
	}

	public Note(final Note other) {
		super(other);

		string = other.string;
		fret = other.fret;
		bassPicking = other.bassPicking;
		mute = other.mute;
		hopo = other.hopo;
		harmonic = other.harmonic;
		vibrato = other.vibrato;
		tremolo = other.tremolo;
		linkNext = other.linkNext;
		slideTo = other.slideTo;
		unpitchedSlide = other.unpitchedSlide;
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

	@Override
	public BassPickingTechnique bassPicking() {
		return bassPicking;
	}

	@Override
	public void bassPicking(final BassPickingTechnique value) {
		bassPicking = value;
	}

	@Override
	public Mute mute() {
		return mute;
	}

	@Override
	public void mute(final Mute value) {
		mute = value;
	}

	@Override
	public HOPO hopo() {
		return hopo;
	}

	@Override
	public void hopo(final HOPO value) {
		hopo = value;
	}

	@Override
	public Harmonic harmonic() {
		return harmonic;
	}

	@Override
	public void harmonic(final Harmonic value) {
		harmonic = value;
	}

	@Override
	public boolean vibrato() {
		return vibrato;
	}

	@Override
	public void vibrato(final boolean value) {
		vibrato = value;
	}

	@Override
	public boolean tremolo() {
		return tremolo;
	}

	@Override
	public void tremolo(final boolean value) {
		tremolo = value;
	}

	@Override
	public boolean linkNext() {
		return linkNext;
	}

	@Override
	public void linkNext(final boolean value) {
		linkNext = value;
	}

	@Override
	public Integer slideTo() {
		return slideTo;
	}

	@Override
	public void slideTo(final Integer value) {
		slideTo = value;
	}

	@Override
	public boolean unpitchedSlide() {
		return unpitchedSlide;
	}

	@Override
	public void unpitchedSlide(final boolean value) {
		unpitchedSlide = value;
	}

	@Override
	public ArrayList2<BendValue> bendValues() {
		return bendValues;
	}

	@Override
	public void bendValues(final ArrayList2<BendValue> value) {
		bendValues = value;
	}
}
