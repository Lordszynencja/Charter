package log.charter.data.song.position.fractional;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.time.ConstantPositionWithLength;
import log.charter.data.song.position.time.IConstantPositionWithLength;
import log.charter.data.song.position.virtual.IVirtualConstantPositionWithEnd;

public interface IConstantFractionalPositionWithEnd
		extends IConstantFractionalPosition, IVirtualConstantPositionWithEnd {
	public static class ConstantFractionalPositionWithEnd implements IConstantFractionalPositionWithEnd {

		private final FractionalPosition position;
		private final FractionalPosition endPosition;

		public ConstantFractionalPositionWithEnd(final FractionalPosition position,
				final FractionalPosition endPosition) {
			this.position = position;
			this.endPosition = endPosition;
		}

		@Override
		public FractionalPosition position() {
			return position;
		}

		@Override
		public FractionalPosition endPosition() {
			return endPosition;
		}

		@Override
		public IConstantPositionWithLength toPosition(final ImmutableBeatsMap beats) {
			final int position = this.position(beats);
			final int endPosition = this.endPosition.position(beats);
			return new ConstantPositionWithLength(position, endPosition - position);
		}

		@Override
		public IConstantFractionalPositionWithEnd toFraction(final ImmutableBeatsMap beats) {
			return this;
		}
	}

	@Override
	public FractionalPosition endPosition();

	default FractionalPosition length() {
		return position().distance(endPosition());
	}

	default int length(final ImmutableBeatsMap beats) {
		return endPosition().position(beats) - position(beats);
	}

	@Override
	default IConstantPositionWithLength asConstantPositionWithLength() {
		return null;
	}

	@Override
	default IConstantFractionalPositionWithEnd asConstantFractionalPositionWithEnd() {
		return this;
	}

	@Override
	default IConstantPositionWithLength toPosition(final ImmutableBeatsMap beats) {
		final int position = position().position(beats);
		final int endPosition = endPosition().position(beats);
		return new ConstantPositionWithLength(position, endPosition - position);
	}

	@Override
	IConstantFractionalPositionWithEnd toFraction(ImmutableBeatsMap beats);
}
