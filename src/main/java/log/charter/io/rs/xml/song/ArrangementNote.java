package log.charter.io.rs.xml.song;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.math.BigDecimal;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamInclude;

import log.charter.io.rs.xml.converters.CountedListConverter.CountedList;
import log.charter.util.collections.ArrayList2;
import log.charter.data.song.BendValue;
import log.charter.data.song.enums.BassPickingTechnique;
import log.charter.data.song.enums.HOPO;
import log.charter.data.song.enums.Harmonic;
import log.charter.data.song.enums.Mute;
import log.charter.data.song.notes.Note;
import log.charter.io.rs.xml.converters.TimeConverter;

@XStreamAlias("note")
@XStreamInclude(ArrangementBendValue.class)
public class ArrangementNote {
	@XStreamAsAttribute
	@XStreamConverter(TimeConverter.class)
	public int time;
	@XStreamAsAttribute
	public int string;
	@XStreamAsAttribute
	public int fret;
	@XStreamAsAttribute
	@XStreamConverter(TimeConverter.class)
	public Integer sustain;
	@XStreamAsAttribute
	public Integer vibrato;
	@XStreamAsAttribute
	public Integer tremolo;
	@XStreamAsAttribute
	public Integer accent;
	@XStreamAsAttribute
	public Integer mute;
	@XStreamAsAttribute
	public Integer palmMute;
	@XStreamAsAttribute
	public Integer pluck;
	@XStreamAsAttribute
	public Integer hopo;
	@XStreamAsAttribute
	public Integer hammerOn;
	@XStreamAsAttribute
	public Integer pullOff;
	@XStreamAsAttribute
	public Integer slap;
	@XStreamAsAttribute
	public Integer slideTo;
	@XStreamAsAttribute
	public Integer slideUnpitchTo;
	@XStreamAsAttribute
	public Integer bend;
	@XStreamAsAttribute
	public Integer tap;
	@XStreamAsAttribute
	public Integer harmonic;
	@XStreamAsAttribute
	public Integer harmonicPinch;
	@XStreamAsAttribute
	public Integer linkNext;
	@XStreamAsAttribute
	public Integer ignore;

	public CountedList<ArrangementBendValue> bendValues;

	public ArrangementNote() {
	}

	private Integer bend(final ArrayList2<BendValue> bendValues) {
		if (bendValues.isEmpty()) {
			return null;
		}

		int bend = 0;
		for (final BendValue bendValue : bendValues) {
			if (bendValue.bendValue.compareTo(BigDecimal.ZERO) == 0) {
				continue;
			}
			bend = max(1, min(3, max(bend, bendValue.bendValue.intValue())));
		}

		return bend;
	}

	private CountedList<ArrangementBendValue> bendValues(final ArrayList2<BendValue> bendValues) {
		if (bendValues.isEmpty()) {
			return null;
		}

		return new CountedList<>(bendValues.map(bendValue -> new ArrangementBendValue(bendValue, time)));
	}

	public ArrangementNote(final Note note) {
		time = note.position();
		string = note.string;
		fret = note.fret;
		sustain = note.length() > 0 ? note.length() : null;
		vibrato = note.vibrato ? 1 : null;
		tremolo = note.tremolo ? 1 : null;
		accent = note.accent ? 1 : null;

		bend = bend(note.bendValues);
		bendValues = bendValues(note.bendValues);

		linkNext = note.linkNext ? 1 : null;
		ignore = note.ignore || note.fret > 22 ? 1 : null;

		setUpMute(note);
		setUpHOPO(note);
		setUpBassPickingTechniques(note);
		setUpHarmonic(note);
		setUpSlide(note);
	}

	public ArrangementNote(final ArrangementChordNote chordNote) {
		time = chordNote.time;
		string = chordNote.string;
		fret = chordNote.fret;
		sustain = chordNote.sustain;
		vibrato = chordNote.vibrato;
		tremolo = chordNote.tremolo;
		accent = chordNote.accent;
		mute = chordNote.mute;
		palmMute = chordNote.palmMute;
		pluck = chordNote.pluck;
		hopo = chordNote.hopo;
		hammerOn = chordNote.hammerOn;
		pullOff = chordNote.pullOff;
		slap = chordNote.slap;
		slideTo = chordNote.slideTo;
		slideUnpitchTo = chordNote.slideUnpitchTo;
		bend = chordNote.bend;
		tap = chordNote.tap;
		harmonic = chordNote.harmonic;
		harmonicPinch = chordNote.harmonicPinch;
		linkNext = chordNote.linkNext;
		ignore = chordNote.ignore;
		bendValues = chordNote.bendValues;
	}

	private void setUpMute(final Note note) {
		if (note.mute == Mute.FULL) {
			mute = 1;
		} else if (note.mute == Mute.PALM) {
			palmMute = 1;
		}
	}

	private void setUpHOPO(final Note note) {
		if (note.hopo == HOPO.HAMMER_ON) {
			hopo = 1;
			hammerOn = 1;
		} else if (note.hopo == HOPO.PULL_OFF) {
			hopo = 1;
			pullOff = 1;
		} else if (note.hopo == HOPO.TAP) {
			tap = 1;
		}
	}

	private void setUpBassPickingTechniques(final Note note) {
		if (note.bassPicking == BassPickingTechnique.POP) {
			pluck = 1;
		} else if (note.bassPicking == BassPickingTechnique.SLAP) {
			slap = 1;
		}
	}

	private void setUpHarmonic(final Note note) {
		if (note.harmonic == Harmonic.NORMAL) {
			harmonic = 1;
		} else if (note.harmonic == Harmonic.PINCH) {
			harmonicPinch = 1;
		}
	}

	private void setUpSlide(final Note note) {
		if (note.slideTo == null) {
			return;
		}

		if (note.unpitchedSlide) {
			slideUnpitchTo = note.slideTo;
		} else {
			slideTo = note.slideTo;
		}
	}
}
