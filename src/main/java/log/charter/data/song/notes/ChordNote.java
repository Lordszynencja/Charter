package log.charter.data.song.notes;

import log.charter.data.song.BendValue;
import log.charter.data.song.enums.BassPickingTechnique;
import log.charter.data.song.enums.HOPO;
import log.charter.data.song.enums.Harmonic;
import log.charter.data.song.enums.Mute;
import log.charter.util.CollectionUtils.ArrayList2;

public class ChordNote implements NoteInterface {
	public int length;
	public Mute mute = Mute.NONE;
	public HOPO hopo = HOPO.NONE;
	public Harmonic harmonic = Harmonic.NONE;
	public boolean vibrato = false;
	public boolean tremolo = false;
	public boolean linkNext = false;
	public Integer slideTo = null;
	public boolean unpitchedSlide = false;
	public ArrayList2<BendValue> bendValues = new ArrayList2<>();

	public ChordNote() {
	}

	public ChordNote(final ChordNote other) {
		length = other.length;
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

	public ChordNote(final Note note) {
		length = note.length();
		mute = note.mute;
		hopo = note.hopo;
		harmonic = note.harmonic;
		vibrato = note.vibrato;
		tremolo = note.tremolo;
		linkNext = note.linkNext;
		slideTo = note.slideTo;
		unpitchedSlide = note.unpitchedSlide;
		bendValues = note.bendValues.map(BendValue::new);
	}

	@Override
	public int length() {
		return length;
	}

	@Override
	public void length(final int value) {
		length = value;
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
	public ArrayList2<BendValue> bendValues() {
		return bendValues;
	}

	@Override
	public void bendValues(final ArrayList2<BendValue> value) {
		bendValues = value;
	}
}
