package log.charter.data.song.position.fractional;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.time.ConstantPosition;
import log.charter.data.song.position.time.IConstantPosition;
import log.charter.data.song.position.virtual.IVirtualConstantPosition;

public interface IConstantFractionalPosition extends IVirtualConstantPosition {
	public FractionalPosition position();

	@Override
	default IConstantPosition asConstantPosition() {
		return null;
	}

	@Override
	default IConstantFractionalPosition asConstantFraction() {
		return this;
	}

	default int position(final ImmutableBeatsMap beats) {
		return position().getPosition(beats);
	}

	@Override
	default IConstantPosition toPosition(final ImmutableBeatsMap beats) {
		return new ConstantPosition(position(beats));
	}

	@Override
	default IConstantFractionalPosition toFraction(final ImmutableBeatsMap beats) {
		return this;
	}

	default FractionalPosition distance(final IConstantFractionalPosition other) {
		return movementTo(other).absolute();
	}

	default FractionalPosition movementTo(final IConstantFractionalPosition other) {
		return position().negate().add(other.position());
	}

	default int compareTo(final IConstantFractionalPosition other) {
		return position().compareTo(other.position());
	}
}
