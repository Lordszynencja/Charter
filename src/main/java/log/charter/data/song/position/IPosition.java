package log.charter.data.song.position;

public interface IPosition extends IConstantPosition, IVirtualPosition {
	void position(int newPosition);

	@Override
	default IPosition asPosition() {
		return this;
	}
}
