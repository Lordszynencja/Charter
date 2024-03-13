package log.charter.data.song.position;

import java.util.Optional;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;

public interface IConstantFractionalPosition extends IVirtualConstantPosition {
	@Override
	default Optional<IConstantFractionalPosition> asConstantFraction() {
		return Optional.of(this);
	}

	public FractionalPosition fractionalPosition();

	default int position(final ImmutableBeatsMap beats) {
		return fractionalPosition().getPosition(beats);
	}

	@Override
	default IConstantPosition positionAsPosition(final ImmutableBeatsMap beats) {
		return new ConstantPosition(position(beats));
	}

	@Override
	default IConstantFractionalPosition positionAsFraction(final ImmutableBeatsMap beats) {
		return this;
	}

	default IConstantFractionalPosition distance(final IConstantFractionalPosition other) {
		return fractionalPosition().add(other.fractionalPosition().negate()).absolute();
	}

	default int compareTo(final IConstantFractionalPosition other) {
		return fractionalPosition().compareTo(other.fractionalPosition());
	}
}
