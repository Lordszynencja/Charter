package log.charter.io.rs.xml.song;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.io.rs.xml.converters.TimeConverter;

@XStreamAlias("tone")
public class ArrangementTone {
	@XStreamAsAttribute
	@XStreamConverter(TimeConverter.class)
	public int time;
	@XStreamAsAttribute
	public String name;

	public ArrangementTone(final int time, final String name) {
		this.time = time;
		this.name = name;
	}
}
