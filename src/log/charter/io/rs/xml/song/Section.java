package log.charter.io.rs.xml.song;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.io.rs.xml.converters.TimeConverter;

@XStreamAlias("section")
public class Section {
	@XStreamAsAttribute
	public String name;
	@XStreamAsAttribute
	public int number;
	@XStreamAsAttribute
	@XStreamConverter(TimeConverter.class)
	public int startTime;
}
