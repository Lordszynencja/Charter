package log.charter.io.rs.xml.song;

import java.math.BigDecimal;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.io.rs.xml.converters.TimeConverter;
import log.charter.song.BendValue;

@XStreamAlias("bendValue")
public class ArrangementBendValue {
	@XStreamAsAttribute
	@XStreamConverter(TimeConverter.class)
	public int time;
	@XStreamAsAttribute
	public BigDecimal step;

	public ArrangementBendValue() {
	}

	public ArrangementBendValue(final BendValue bendValue) {
		time = bendValue.position();
		step = bendValue.bendValue;
	}
}
