package log.charter.data.song.position;

public interface IConstantPositionWithLength extends IConstantPosition {
	int length();

	default IConstantPosition endPosition() {
		return new ConstantPosition(position() + length());
	}
}
