package log.charter.data.song.position;

import java.util.Optional;

public interface IPosition extends IConstantPosition, IVirtualPosition {
	void position(int newPosition);

	@Override
	default Optional<IPosition> asPosition() {
		return Optional.of(this);
	}
}
