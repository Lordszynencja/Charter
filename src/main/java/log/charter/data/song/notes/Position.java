package log.charter.data.song.notes;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class Position implements IPosition {
	@XStreamAsAttribute
	private int position;

	public Position(final int position) {
		this.position = position;
	}

	public Position(final Position other) {
		position = other.position;
	}

	public Position(final IPosition other) {
		position = other.position();
	}

	@Override
	public int position() {
		return position;
	}

	@Override
	public void position(final int newPosition) {
		position = newPosition;
	}

	@Override
	public int hashCode() {
		return Integer.hashCode(position);
	}

	@Override
	public boolean equals(final Object obj) {
		if (!Position.class.isAssignableFrom(obj.getClass())) {
			return false;
		}

		final Position otherPosition = (Position) obj;

		return otherPosition.position == position;
	}
}
