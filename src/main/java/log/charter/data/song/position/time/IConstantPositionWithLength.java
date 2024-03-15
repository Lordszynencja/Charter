package log.charter.data.song.position.time;

import log.charter.data.song.position.ConstantPosition;
import log.charter.data.song.position.fractional.IConstantFractionalPositionWithEnd;
import log.charter.data.song.position.virtual.IVirtualConstantPositionWithEnd;

public interface IConstantPositionWithLength extends IConstantPosition, IVirtualConstantPositionWithEnd {
	int length();

	@Override
	default IConstantPositionWithLength asConstantPositionWithLength() {
		return this;
	}

	@Override
	default IConstantFractionalPositionWithEnd asConstantFractionalPositionWithEnd() {
		return null;
	}

	@Override
	default IConstantPosition endPosition() {
		return new ConstantPosition(position() + length());
	}
}
