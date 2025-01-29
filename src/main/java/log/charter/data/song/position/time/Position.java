package log.charter.data.song.position.time;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class Position implements IPosition {
	@XStreamAsAttribute
	private double position;

	public Position(final double position) {
		this.position = position;
	}

	public Position(final Position other) {
		position = other.position;
	}

	public Position(final IPosition other) {
		position = other.position();
	}

	@Override
	public double position() {
		return position;
	}

	@Override
	public void position(final double newPosition) {
		position = newPosition;
	}

	@Override
	public int hashCode() {
		return Double.hashCode(position);
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
