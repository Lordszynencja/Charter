package log.charter.util;

public interface Positionable extends Comparable<Positionable> {

	int position();

	@Override
	default int compareTo(final Positionable o) {
		return Integer.compare(position(), o.position());
	}
}
