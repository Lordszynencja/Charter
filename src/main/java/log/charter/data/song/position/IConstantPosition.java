package log.charter.data.song.position;

import static java.lang.Math.abs;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;

public interface IConstantPosition extends IVirtualConstantPosition {
	int position();

	@Override
	default IConstantPosition asConstantPosition() {
		return this;
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
		return Integer.compare(position(), other.position());
	}
}
