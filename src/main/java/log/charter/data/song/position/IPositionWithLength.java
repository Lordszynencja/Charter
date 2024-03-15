package log.charter.data.song.position;

public interface IPositionWithLength extends IPosition, IConstantPositionWithLength, IVirtualPositionWithEnd {
	void length(int newLength);

	@Override
	default IPositionWithLength asPositionWithLength() {
		return this;
	}

	@Override
	default IConstantPosition endPosition() {
		return new ConstantPosition(position() + length());
	}

	default void endPosition(final int newEndPosition) {
		length(newEndPosition - position());
	}
}
