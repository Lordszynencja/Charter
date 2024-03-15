package log.charter.data.song.position.fractional;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.time.IPositionWithLength;
import log.charter.data.song.position.virtual.IVirtualPositionWithEnd;

public interface IFractionalPositionWithEnd
		extends IConstantFractionalPositionWithEnd, IFractionalPosition, IVirtualPositionWithEnd {
	public void endPosition(FractionalPosition newEndPosition);

	@Override
	default IPositionWithLength asPositionWithLength() {
		return null;
	}

	@Override
	default IFractionalPositionWithEnd asFractionalPositionWithEnd() {
		return this;
	}

	default int endPosition(final ImmutableBeatsMap beats) {
		return endPosition().position(beats);
	}

	@Override
	default void move(final FractionalPosition distance) {
		IFractionalPosition.super.move(distance);
		endPosition(endPosition().add(distance));
	}

	default void changeLength(final FractionalPosition change) {
		endPosition(endPosition().add(change));
	}

}
