package log.charter.data.song.position.time;

import log.charter.data.song.position.fractional.IFractionalPosition;
import log.charter.data.song.position.virtual.IVirtualPosition;

public interface IPosition extends IConstantPosition, IVirtualPosition {
	void position(double newPosition);

	@Override
	default IPosition asPosition() {
		return this;
	}

	@Override
	default IFractionalPosition asFraction() {
		return null;
	}
}
