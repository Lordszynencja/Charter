package log.charter.io.rs.xml.song;

import java.util.stream.Collectors;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamInclude;

import log.charter.io.rs.xml.converters.CountedListConverter.CountedList;
import log.charter.io.rs.xml.converters.TimeConverter;
import log.charter.song.enums.BassPickingTechnique;
import log.charter.song.enums.HOPO;
import log.charter.song.enums.Harmonic;
import log.charter.song.enums.Mute;
import log.charter.song.notes.Note;

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
	public CountedList<ArrangementBendValue> bendValues;
	@XStreamAsAttribute
	public Integer linkNext;

	public ArrangementNote() {
	}

	public ArrangementNote(final Note note) {
		time = note.position();
		string = note.string;
		fret = note.fret;
		sustain = note.length() > 0 ? note.length() : null;
		vibrato = note.vibrato;
		accent = note.accent ? 1 : null;

		bend = note.bendValues.isEmpty() ? null
				: note.bendValues.stream()
						.map(bendValue -> bendValue.bendValue == null ? 0 : bendValue.bendValue.intValue())
						.collect(Collectors.maxBy(Integer::compare)).orElse(null);
		bendValues = note.bendValues.isEmpty() ? null
				: new CountedList<>(note.bendValues.map(ArrangementBendValue::new));

		linkNext = note.linkNext ? 1 : null;

		setUpMute(note);
		setUpHOPO(note);
		setUpBassPickingTechniques(note);
		setUpHarmonic(note);
		setUpSlide(note);
	}

	private void setUpMute(final Note note) {
		if (note.mute == Mute.STRING) {
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
