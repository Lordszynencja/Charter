package log.charter.data.song;

import java.math.BigDecimal;

import log.charter.data.song.position.Position;
import log.charter.io.rs.xml.song.ArrangementBendValue;

public class BendValue extends Position {
	public BigDecimal bendValue;

	public BendValue(final int pos, final BigDecimal bendValue) {
		super(pos);
		this.bendValue = bendValue;
	}

	public BendValue(final ArrangementBendValue arrangementBendValue, final int noteTime) {
		super(arrangementBendValue.time - noteTime);
		bendValue = arrangementBendValue.step == null ? BigDecimal.ZERO : arrangementBendValue.step;
	}

	public BendValue(final BendValue other) {
		super(other);
		bendValue = other.bendValue;
	}
}
