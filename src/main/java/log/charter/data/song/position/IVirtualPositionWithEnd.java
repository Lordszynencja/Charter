package log.charter.data.song.position;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;

public interface IVirtualPositionWithEnd extends IVirtualPosition {
	default IPositionWithLength asPositionWithLength() {
		return null;
	}

	default IFractionalPositionWithEnd asFractionalPositionWithEnd() {
		return null;
	}

	IVirtualConstantPosition endPosition();

	default void endPosition(final ImmutableBeatsMap beats, final IVirtualConstantPosition newPosition) {
		if (isPosition()) {
			asPositionWithLength().endPosition(newPosition.toPosition(beats).position());
		}
		if (isFraction()) {
			asFractionalPositionWithEnd().endPosition(newPosition.toFraction(beats).fractionalPosition());
		}
	}
}
