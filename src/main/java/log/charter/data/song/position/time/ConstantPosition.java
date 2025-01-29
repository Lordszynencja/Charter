package log.charter.data.song.position.time;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class ConstantPosition implements IConstantPosition {
	@XStreamAsAttribute
	private final double position;

	public ConstantPosition(final double position) {
		this.position = position;
	}

	public ConstantPosition(final ConstantPosition other) {
		position = other.position();
	}

	@Override
	public double position() {
		return position;
	}

	@Override
	public int hashCode() {
		return Double.hashCode(position);
	}

	@Override
	public boolean equals(final Object obj) {
		if (!ConstantPosition.class.isAssignableFrom(obj.getClass())) {
			return false;
		}

		return ((ConstantPosition) obj).position == position;
	}
}
