package log.charter.data.song.position.fractional;

import log.charter.data.song.position.FractionalPosition;
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
		public FractionalPosition fractionalPosition() {
			return position;
		}

		@Override
		public FractionalPosition endPosition() {
			return endPosition;
		}

	}

	@Override
	public FractionalPosition endPosition();

	@Override
	default IConstantPositionWithLength asConstantPositionWithLength() {
		return null;
	}

	@Override
	default IConstantFractionalPositionWithEnd asConstantFractionalPositionWithEnd() {
		return this;
	}
}
