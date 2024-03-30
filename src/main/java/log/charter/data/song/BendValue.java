package log.charter.data.song;

import java.math.BigDecimal;

import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.fractional.IFractionalPosition;

public class BendValue implements IFractionalPosition {
	private FractionalPosition position;
	public BigDecimal bendValue;

	public BendValue() {
	}

	public BendValue(final FractionalPosition position) {
		this.position = position;
		bendValue = BigDecimal.ZERO;
	}

	public BendValue(final FractionalPosition position, final BigDecimal bendValue) {
		this.position = position;
		this.bendValue = bendValue;
	}

	public BendValue(final BendValue other) {
		position = other.position;
		bendValue = other.bendValue;
	}

	@Override
	public FractionalPosition position() {
		return position;
	}

	@Override
	public void position(final FractionalPosition newPosition) {
		position = newPosition;
	}
}
