package log.charter.data.song.notes;

import static log.charter.util.CollectionUtils.map;

import java.util.ArrayList;
import java.util.List;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.BendValue;
import log.charter.data.song.enums.BassPickingTechnique;
import log.charter.data.song.enums.HOPO;
import log.charter.data.song.enums.Harmonic;
import log.charter.data.song.enums.Mute;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.fractional.IConstantFractionalPositionWithEnd;
import log.charter.data.song.position.fractional.IFractionalPositionWithEnd;

public class ChordNote implements NoteInterface, IFractionalPositionWithEnd {
	public final Chord parent;
	private FractionalPosition endPosition;
	public Mute mute = Mute.NONE;
	public HOPO hopo = HOPO.NONE;
	public Harmonic harmonic = Harmonic.NONE;
	public boolean vibrato = false;
	public boolean tremolo = false;
	public boolean linkNext = false;
	public Integer slideTo = null;
	public boolean unpitchedSlide = false;
	public List<BendValue> bendValues = new ArrayList<>();

	public ChordNote(final Chord parent) {
		this.parent = parent;
		endPosition = parent.position();
	}

	public ChordNote(final Chord parent, final FractionalPosition endPosition) {
		this.parent = parent;
		this.endPosition = endPosition;
	}

	public ChordNote(final Chord parent, final ChordNote other) {
		this.parent = parent;
		endPosition = other.endPosition;
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

	public ChordNote(final Chord parent, final Note note) {
		this.parent = parent;
		endPosition = note.endPosition();
		mute = note.mute;
		hopo = note.hopo;
		harmonic = note.harmonic;
		vibrato = note.vibrato;
		tremolo = note.tremolo;
		linkNext = note.linkNext;
		slideTo = note.slideTo;
		unpitchedSlide = note.unpitchedSlide;
		bendValues = map(note.bendValues, BendValue::new);
	}

	@Override
	public BassPickingTechnique bassPicking() {
		return BassPickingTechnique.NONE;
	}

	@Override
	public void bassPicking(final BassPickingTechnique value) {
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
	public void endPosition(final FractionalPosition newEndPosition) {
		endPosition = newEndPosition;
	}

	@Override
	public FractionalPosition endPosition() {
		return endPosition;
	}

	@Override
	public FractionalPosition position() {
		return parent.position();
	}

	@Override
	public void position(final FractionalPosition newPosition) {
		throw new UnsupportedOperationException("Can't set position for chord note, should set it on parent");
	}

	@Override
	public IConstantFractionalPositionWithEnd toFraction(final ImmutableBeatsMap beats) {
		return this;
	}

}
