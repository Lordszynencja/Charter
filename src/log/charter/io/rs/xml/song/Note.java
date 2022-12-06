package log.charter.io.rs.xml.song;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamInclude;

import log.charter.io.rs.xml.converters.TimeConverter;
import log.charter.io.rs.xml.converters.CountedListConverter.CountedList;

@XStreamAlias("note")
@XStreamInclude(BendValue.class)
public class Note {
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
	public CountedList<BendValue> bendValues;
}
