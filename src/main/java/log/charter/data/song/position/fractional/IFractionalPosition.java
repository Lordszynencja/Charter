package log.charter.data.song.position.fractional;

import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.time.IPosition;
import log.charter.data.song.position.virtual.IVirtualPosition;

public interface IFractionalPosition extends IConstantFractionalPosition, IVirtualPosition {
	void position(FractionalPosition newPosition);

	@Override
	default IPosition asPosition() {
		return null;
	}

	@Override
	default IFractionalPosition asFraction() {
		return this;
	}

	default void move(final FractionalPosition distance) {
		position(position().add(distance));
	}

}
