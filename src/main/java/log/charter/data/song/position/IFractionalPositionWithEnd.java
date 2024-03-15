package log.charter.data.song.position;

public interface IFractionalPositionWithEnd
		extends IConstantFractionalPositionWithEnd, IFractionalPosition, IVirtualPositionWithEnd {
	public void endPosition(FractionalPosition newEndPosition);

	@Override
	default IFractionalPositionWithEnd asFractionalPositionWithEnd() {
		return this;
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
