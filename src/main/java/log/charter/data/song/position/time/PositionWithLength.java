package log.charter.data.song.position.time;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.fractional.IConstantFractionalPositionWithEnd;
import log.charter.data.song.position.fractional.IConstantFractionalPositionWithEnd.ConstantFractionalPositionWithEnd;

public class PositionWithLength extends Position implements IPositionWithLength {
	@XStreamAsAttribute
	private int length;

	public PositionWithLength(final int position) {
		super(position);
	}

	public PositionWithLength(final int position, final int length) {
		super(position);
		this.length = length;
	}

	public PositionWithLength(final PositionWithLength other) {
		super(other);
		length = other.length;
	}

	@Override
	public int length() {
		return length;
	}

	@Override
	public void length(final int value) {
		length = value;
	}

	@Override
	public IConstantPositionWithLength toPosition(final ImmutableBeatsMap beats) {
		return this;
	}

	@Override
	public IConstantFractionalPositionWithEnd toFraction(final ImmutableBeatsMap beats) {
		return new ConstantFractionalPositionWithEnd(FractionalPosition.fromTime(beats, position()),
				FractionalPosition.fromTime(beats, position() + length));
	}
}
