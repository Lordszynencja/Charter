package log.charter.data.song.position.time;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.fractional.IConstantFractionalPositionWithEnd;
import log.charter.data.song.position.fractional.IConstantFractionalPositionWithEnd.ConstantFractionalPositionWithEnd;

public class ConstantPositionWithLength extends ConstantPosition implements IConstantPositionWithLength {
	@XStreamAsAttribute
	private final int length;

	public ConstantPositionWithLength(final int position) {
		super(position);
		length = 0;
	}

	public ConstantPositionWithLength(final int position, final int length) {
		super(position);
		this.length = length;
	}

	public ConstantPositionWithLength(final ConstantPositionWithLength other) {
		super(other);
		length = other.length;
	}

	@Override
	public int length() {
		return length;
	}

	@Override
	public IConstantPositionWithLength toPosition(final ImmutableBeatsMap beats) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public IConstantFractionalPositionWithEnd toFraction(final ImmutableBeatsMap beats) {
		return new ConstantFractionalPositionWithEnd(FractionalPosition.fromTime(beats, position()),
				FractionalPosition.fromTime(beats, position() + length));
	}
}
