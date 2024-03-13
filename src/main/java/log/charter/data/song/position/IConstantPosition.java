package log.charter.data.song.position;

import static java.lang.Math.abs;

import java.util.Optional;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;

public interface IConstantPosition extends IVirtualConstantPosition {
	int position();

	@Override
	default Optional<IConstantPosition> asConstantPosition() {
		return Optional.of(this);
	}

	@Override
	default IConstantPosition positionAsPosition(final ImmutableBeatsMap beats) {
		return this;
	}

	@Override
	default IConstantFractionalPosition positionAsFraction(final ImmutableBeatsMap beats) {
		return FractionalPosition.fromTime(beats, position(), false);
	}

	default IConstantPosition distance(final IConstantPosition other) {
		return new ConstantPosition(abs(other.position() - position()));
	}

	default int compareTo(final IConstantPosition other) {
		return Integer.compare(position(), other.position());
	}
}
