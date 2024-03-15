package log.charter.data.song.position;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import log.charter.data.song.position.time.IPositionWithLength;

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
}
