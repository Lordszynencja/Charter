package log.charter.data.song.position;

public interface IConstantPositionWithLength extends IConstantPosition {
	int length();

	default IConstantPosition asEndPosition() {
		return new Position(position() + length());
	}

	default int endPosition() {
		return position() + length();
	}
}
