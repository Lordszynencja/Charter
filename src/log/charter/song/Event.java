package log.charter.song;

public class Event extends Position {

	/**
	 * length in ms
	 */
	private double length = 1;

	public Event(final double pos) {
		super(pos);
	}

	public Event(final double pos, final double length) {
		super(pos);
		setLength(length);
	}

	public Event(final Event e) {
		super(e);
		length = e.length;
	}

	public double getLength() {
		return length;
	}

	public void setLength(final double l) {
		length = l < 1 ? 1 : l;
	}

	@Override
	public String toString() {
		return "Event{pos: " + pos + ", length: " + length + "}";
	}
}
