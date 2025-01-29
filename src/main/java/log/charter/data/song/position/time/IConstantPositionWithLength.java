package log.charter.data.song.position.time;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.fractional.IConstantFractionalPositionWithEnd;
import log.charter.data.song.position.fractional.IConstantFractionalPositionWithEnd.ConstantFractionalPositionWithEnd;
import log.charter.data.song.position.virtual.IVirtualConstantPositionWithEnd;

public interface IConstantPositionWithLength extends IConstantPosition, IVirtualConstantPositionWithEnd {
	double length();

	@Override
	default IConstantPositionWithLength asConstantPositionWithLength() {
		return this;
	}

	@Override
	default IConstantFractionalPositionWithEnd asConstantFractionalPositionWithEnd() {
		return null;
	}

	@Override
	default IConstantPosition endPosition() {
		return new ConstantPosition(position() + length());
	}

	@Override
	IConstantPositionWithLength toPosition(ImmutableBeatsMap beats);

	@Override
	default IConstantFractionalPositionWithEnd toFraction(final ImmutableBeatsMap beats) {
		return new ConstantFractionalPositionWithEnd(FractionalPosition.fromTime(beats, position()),
				FractionalPosition.fromTime(beats, endPosition().position()));
	}
}
