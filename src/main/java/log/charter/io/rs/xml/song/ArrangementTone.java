package log.charter.io.rs.xml.song;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.io.rs.xml.converters.TimeConverter;
import log.charter.song.ToneChange;

@XStreamAlias("tone")
public class ArrangementTone {
	@XStreamAsAttribute
	@XStreamConverter(TimeConverter.class)
	public int time;
	@XStreamAsAttribute
	public String name;

	public ArrangementTone(final ToneChange toneChange) {
		time = toneChange.position();
		name = toneChange.toneName;
	}
}
