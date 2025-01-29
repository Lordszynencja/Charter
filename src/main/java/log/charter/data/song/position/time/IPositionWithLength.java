package log.charter.data.song.position.time;

import log.charter.data.song.position.fractional.IFractionalPositionWithEnd;
import log.charter.data.song.position.virtual.IVirtualPositionWithEnd;

public interface IPositionWithLength extends IPosition, IConstantPositionWithLength, IVirtualPositionWithEnd {
	void length(double newLength);

	@Override
	default IPositionWithLength asPositionWithLength() {
		return this;
	}

	@Override
	default IFractionalPositionWithEnd asFractionalPositionWithEnd() {
		return null;
	}

	@Override
	default IConstantPosition endPosition() {
		return new ConstantPosition(position() + length());
	}

	default void endPosition(final double newEndPosition) {
		length(newEndPosition - position());
	}
}
