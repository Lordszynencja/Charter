package log.charter.data.song.position;

import java.util.Optional;

public interface IFractionalPosition extends IConstantFractionalPosition, IVirtualPosition {
	void fractionalPosition(FractionalPosition newPosition);

	@Override
	default Optional<IFractionalPosition> asFraction() {
		return Optional.of(this);
	}

	default void move(final FractionalPosition fraction) {
		fractionalPosition(fractionalPosition().add(fraction));
	}
}
