package log.charter.data.song.position;

public interface IFractionalPosition extends IConstantFractionalPosition, IVirtualPosition {
	void fractionalPosition(FractionalPosition newPosition);

	@Override
	default IFractionalPosition asFraction() {
		return this;
	}

	default void move(final FractionalPosition distance) {
		fractionalPosition(fractionalPosition().add(distance));
	}
}
