package log.charter.song;

public class Position implements Comparable<Position> {
	public int position;

	public Position(final int pos) {
		position = pos;
	}

	@Override
	public int compareTo(final Position o) {
		return Integer.compare(position, o.position);
	}
}
