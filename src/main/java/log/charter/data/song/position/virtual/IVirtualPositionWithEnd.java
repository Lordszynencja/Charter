package log.charter.data.song.position.virtual;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.position.fractional.IFractionalPositionWithEnd;
import log.charter.data.song.position.time.IPositionWithLength;

public interface IVirtualPositionWithEnd extends IVirtualPosition, IVirtualConstantPositionWithEnd {

	IPositionWithLength asPositionWithLength();

	IFractionalPositionWithEnd asFractionalPositionWithEnd();

	@Override
	default boolean isPosition() {
		return asPositionWithLength() != null;
	}

	@Override
	default boolean isFraction() {
		return asFractionalPositionWithEnd() != null;
	}

	default void endPosition(final ImmutableBeatsMap beats, final IVirtualConstantPosition newPosition) {
		if (isPosition()) {
			asPositionWithLength().endPosition(newPosition.toPosition(beats).position());
		}
		if (isFraction()) {
			asFractionalPositionWithEnd().endPosition(newPosition.toFraction(beats).position());
		}
	}
}
