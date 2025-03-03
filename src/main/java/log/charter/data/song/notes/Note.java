package log.charter.data.song.notes;

import static log.charter.util.CollectionUtils.map;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.BendValue;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.enums.BassPickingTechnique;
import log.charter.data.song.enums.HOPO;
import log.charter.data.song.enums.Harmonic;
import log.charter.data.song.enums.Mute;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.fractional.IConstantFractionalPositionWithEnd;
import log.charter.data.song.position.fractional.IFractionalPositionWithEnd;
import log.charter.io.rsc.xml.converters.NoteConverter;

@XStreamAlias("note")
@XStreamConverter(NoteConverter.class)
public class Note extends GuitarSound implements IFractionalPositionWithEnd, NoteInterface {
	private FractionalPosition endPosition;
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
	public List<BendValue> bendValues = new ArrayList<>();

	public Note() {
	}

	public Note(final FractionalPosition position, final FractionalPosition endPosition) {
		super(position);
		this.endPosition = endPosition;
	}

	public Note(final FractionalPosition position, final int string, final int fret) {
		super(position);
		endPosition = position;
		this.string = string;
		this.fret = fret;
	}

	public Note(final Note other) {
		super(other);

		endPosition = other.endPosition;
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
		bendValues = map(other.bendValues, BendValue::new);
	}

	public Note(final Chord chord, final ChordTemplate template) {
		super(chord);

		string = template.frets.keySet().stream().min(Integer::compare).orElse(0);
		fret = template.frets.get(string);

		final ChordNote chordNote = chord.chordNotes.get(string);
		endPosition = chordNote.endPosition();
		mute = chordNote.mute;
		hopo = chordNote.hopo;
		harmonic = chordNote.harmonic;
		vibrato = chordNote.vibrato;
		tremolo = chordNote.tremolo;
		linkNext = chordNote.linkNext;
		slideTo = chordNote.slideTo;
		unpitchedSlide = chordNote.unpitchedSlide;
		bendValues = map(chordNote.bendValues, BendValue::new);
	}

	@Override
	public FractionalPosition endPosition() {
		return endPosition;
	}

	@Override
	public void endPosition(final FractionalPosition newEndPosition) {
		endPosition = newEndPosition;
	}

	@Override
	public void move(final FractionalPosition distance) {
		IFractionalPositionWithEnd.super.move(distance);
		bendValues.forEach(p -> p.move(distance));
	}

	@Override
	public void move(final int beats) {
		move(new FractionalPosition(beats));
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
	public List<BendValue> bendValues() {
		return bendValues;
	}

	@Override
	public void bendValues(final List<BendValue> value) {
		bendValues = value;
	}

	@Override
	public IConstantFractionalPositionWithEnd toFraction(final ImmutableBeatsMap beats) {
		return this;
	}

}
