package log.charter.io.rs.xml.showlights;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.io.rs.xml.converters.TimeConverter;

@XStreamAlias("showlight")
public class RsXmlShowlight {
	@XStreamAsAttribute
	@XStreamConverter(TimeConverter.class)
	public Integer time;
	@XStreamAsAttribute
	public int note;

	public RsXmlShowlight(final Integer time, final int note) {
		this.time = time;
		this.note = note;
	}
}
