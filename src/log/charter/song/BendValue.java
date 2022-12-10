package log.charter.song;

import java.math.BigDecimal;

import log.charter.io.rs.xml.song.ArrangementBendValue;

public class BendValue extends Position {
	public BigDecimal bendValue;

	public BendValue(final int pos, final BigDecimal bendValue) {
		super(pos);
		this.bendValue = bendValue;
	}

	public BendValue(final ArrangementBendValue arrangementBendValue) {
		super(arrangementBendValue.time);
		bendValue = arrangementBendValue.step;
	}
}
