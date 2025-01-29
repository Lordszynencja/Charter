package log.charter.data.song.position.time;

import static java.lang.Math.abs;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.fractional.IConstantFractionalPosition;
import log.charter.data.song.position.virtual.IVirtualConstantPosition;

public interface IConstantPosition extends IVirtualConstantPosition {
	double position();

	@Override
	default IConstantPosition asConstantPosition() {
		return this;
	}

	@Override
	default IConstantFractionalPosition asConstantFraction() {
		return null;
	}

	@Override
	default IConstantPosition toPosition(final ImmutableBeatsMap beats) {
		return this;
	}

	@Override
	default IConstantFractionalPosition toFraction(final ImmutableBeatsMap beats) {
		return FractionalPosition.fromTime(beats, position());
	}

	default IConstantPosition distance(final IConstantPosition other) {
		return new ConstantPosition(abs(other.position() - position()));
	}

	default int compareTo(final IConstantPosition other) {
		return Double.compare(position(), other.position());
	}
}
