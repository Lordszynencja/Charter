package log.charter.data.song.position.virtual;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.position.fractional.IConstantFractionalPositionWithEnd;
import log.charter.data.song.position.time.IConstantPositionWithLength;

public interface IVirtualConstantPositionWithEnd extends IVirtualConstantPosition {
	IVirtualConstantPosition endPosition();

	IConstantPositionWithLength asConstantPositionWithLength();

	IConstantFractionalPositionWithEnd asConstantFractionalPositionWithEnd();

	@Override
	default boolean isPosition() {
		return asConstantPositionWithLength() != null;
	}

	@Override
	default boolean isFraction() {
		return asConstantFractionalPositionWithEnd() != null;
	}

	@Override
	IConstantPositionWithLength toPosition(ImmutableBeatsMap beats);

	@Override
	IConstantFractionalPositionWithEnd toFraction(ImmutableBeatsMap beats);
}
