package log.charter.io.rs.xml.song;

import java.math.BigDecimal;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.BendValue;
import log.charter.io.rs.xml.converters.TimeConverter;

@XStreamAlias("bendValue")
public class ArrangementBendValue {
	private static BigDecimal maxValue = new BigDecimal(3);

	private static BigDecimal getStep(final BigDecimal bendValue) {
		if (bendValue.compareTo(BigDecimal.ZERO) == 0) {
			return null;
		}
		if (bendValue.compareTo(maxValue) > 0) {
			return maxValue;
		}

		return bendValue;
	}

	@XStreamAsAttribute
	@XStreamConverter(TimeConverter.class)
	public int time;
	@XStreamAsAttribute
	public BigDecimal step;

	public ArrangementBendValue(final ImmutableBeatsMap beats, final BendValue bendValue) {
		time = bendValue.position(beats);
		step = getStep(bendValue.bendValue);
	}
}
