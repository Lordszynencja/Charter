package log.charter.song.notes;

import static log.charter.util.Utils.mapInteger;

import log.charter.io.rs.xml.song.ArrangementNote;
import log.charter.song.BendValue;
import log.charter.song.Position;
import log.charter.song.enums.BassPickingTechnique;
import log.charter.song.enums.HOPO;
import log.charter.song.enums.Harmonic;
import log.charter.song.enums.Mute;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.Slideable;

public class Note extends Position implements Slideable {
	public int string;
	public int fret;
	public int length;
	public Integer vibrato;
	public boolean accent;
	public Mute mute = Mute.NONE;
	public HOPO hopo = HOPO.NONE;
	public BassPickingTechnique bassPicking = BassPickingTechnique.NONE;
	public Integer slideTo;
	public boolean unpitchedSlide;
	public Integer bend;
	public Harmonic harmonic = Harmonic.NONE;
	public ArrayList2<BendValue> bendValues;
	public boolean linkNext;

	public Note(final int pos, final int string, final int fret) {
		super(pos);
		this.string = string;
		this.fret = fret;
	}

	public Note(final ArrangementNote arrangementNote) {
		super(arrangementNote.time);
		string = arrangementNote.string;
		fret = arrangementNote.fret;
		length = arrangementNote.sustain == null ? 0 : arrangementNote.sustain;
		vibrato = arrangementNote.vibrato;
		accent = mapInteger(arrangementNote.accent);
		mute = Mute.fromArrangmentNote(arrangementNote);
		hopo = HOPO.fromArrangmentNote(arrangementNote);
		bassPicking = BassPickingTechnique.fromArrangmentNote(arrangementNote);
		if (arrangementNote.slideTo != null) {
			slideTo = arrangementNote.slideTo;
		} else if (arrangementNote.slideUnpitchTo != null) {
			slideTo = arrangementNote.slideUnpitchTo;
			unpitchedSlide = true;
		}
		bend = arrangementNote.bend;
		harmonic = Harmonic.fromArrangmentNote(arrangementNote);
		bendValues = arrangementNote.bendValues == null ? new ArrayList2<>()
				: arrangementNote.bendValues.list.map(BendValue::new);
		linkNext = mapInteger(arrangementNote.linkNext);
	}

	public Note(final Note other) {
		super(other);
		string = other.string;
		fret = other.fret;
		length = other.length;
		vibrato = other.vibrato;
		accent = other.accent;
		mute = other.mute;
		hopo = other.hopo;
		bassPicking = other.bassPicking;
		slideTo = other.slideTo;
		unpitchedSlide = other.unpitchedSlide;
		bend = other.bend;
		harmonic = other.harmonic;
		bendValues = other.bendValues.map(BendValue::new);
		linkNext = other.linkNext;
	}

	@Override
	public Integer slideTo() {
		return slideTo;
	}

	@Override
	public boolean unpitched() {
		return unpitchedSlide;
	}

	@Override
	public void setSlide(final Integer slideTo, final boolean unpitched) {
		this.slideTo = slideTo;
		unpitchedSlide = unpitched;
	}
}
