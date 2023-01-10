package log.charter.song;

import java.math.BigDecimal;

import log.charter.io.rs.xml.song.ArrangementBendValue;
import log.charter.song.notes.Position;

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
