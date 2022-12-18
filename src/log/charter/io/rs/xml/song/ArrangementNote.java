package log.charter.io.rs.xml.song;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamInclude;

import log.charter.io.rs.xml.converters.CountedListConverter.CountedList;
import log.charter.io.rs.xml.converters.TimeConverter;
import log.charter.song.Note;

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
		time = note.position;
		string = note.string;
		fret = note.fret;
		sustain = note.sustain > 0 ? note.sustain : null;
		vibrato = note.vibrato;
		accent = note.accent ? 1 : null;
		mute = note.fretHandMute ? 1 : null;
		palmMute = note.palmMute ? 1 : null;
		pluck = note.pluck ? 1 : null;
		hopo = note.hammerOn || note.pullOff ? 1 : null;
		hammerOn = note.hammerOn ? 1 : null;
		pullOff = note.pullOff ? 1 : null;
		slap = note.slap ? 1 : null;
		slideTo = note.slideTo;
		slideUnpitchTo = note.unpitchedSlideTo;
		bend = note.bend;
		tap = note.tap ? 1 : null;
		harmonic = note.harmonic ? 1 : null;
		harmonicPinch = note.harmonicPinch ? 1 : null;
		bendValues = note.bendValues.isEmpty() ? null
				: new CountedList<>(note.bendValues.map(ArrangementBendValue::new));
		linkNext = note.linkNext ? 1 : null;
	}
}
