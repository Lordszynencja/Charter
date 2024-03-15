package log.charter.data.song.position.fractional;

import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.time.IConstantPositionWithLength;
import log.charter.data.song.position.virtual.IVirtualConstantPositionWithEnd;

public interface IConstantFractionalPositionWithEnd
		extends IConstantFractionalPosition, IVirtualConstantPositionWithEnd {
	@Override
	public FractionalPosition endPosition();

	@Override
	default IConstantPositionWithLength asConstantPositionWithLength() {
		return null;
	}

	@Override
	default IConstantFractionalPositionWithEnd asConstantFractionalPositionWithEnd() {
		return this;
	}
}
