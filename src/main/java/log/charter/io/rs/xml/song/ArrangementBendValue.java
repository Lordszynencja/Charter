package log.charter.io.rs.xml.song;

import java.math.BigDecimal;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.io.rs.xml.converters.TimeConverter;
import log.charter.song.BendValue;

@XStreamAlias("bendValue")
public class ArrangementBendValue {
	private static BigDecimal maxValue = new BigDecimal(3);

	@XStreamAsAttribute
	@XStreamConverter(TimeConverter.class)
	public int time;
	@XStreamAsAttribute
	public BigDecimal step;

	public ArrangementBendValue() {
	}

	private BigDecimal getStep(final BigDecimal bendValue) {
		if (bendValue.compareTo(BigDecimal.ZERO) == 0) {
			return null;
		}
		if (bendValue.compareTo(maxValue) > 0) {
			return maxValue;
		}

		return bendValue;
	}

	public ArrangementBendValue(final BendValue bendValue, final int notePosition) {
		time = notePosition + bendValue.position();
		step = getStep(bendValue.bendValue);
	}
}
