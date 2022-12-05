package log.charter.io.rs.xml.vocals;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.io.rs.xml.converters.TimeConverter;

@XStreamAlias("vocal")
public class Vocal {
	@XStreamAsAttribute
	@XStreamConverter(TimeConverter.class)
	public Integer time;

	@XStreamAsAttribute
	public Integer note;

	@XStreamAsAttribute
	@XStreamConverter(TimeConverter.class)
	public Integer length;

	@XStreamAsAttribute
	public String lyric;
}
