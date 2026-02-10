package log.charter.util.collections;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ExpiringValuesList<T> {
	public static final long second = 1_000_000_000;

	private final List<Pair<Long, T>> values = new LinkedList<>();
	private final long lifetime;

	/**
	 * @param lifetime in nanoseconds
	 */
	public ExpiringValuesList(final long lifetime) {
		this.lifetime = lifetime;
	}

	public synchronized void addValue(final T value) {
		values.add(new Pair<>(System.nanoTime(), value));
	}

	public synchronized List<T> getValues() {
		final List<T> list = new ArrayList<>();
		final Iterator<Pair<Long, T>> iterator = values.iterator();
		final long t = System.nanoTime();
		while (iterator.hasNext()) {
			final Pair<Long, T> value = iterator.next();
			if (value.a + lifetime < t) {
				iterator.remove();
			} else {
				list.add(value.b);
				break;
			}
		}
		iterator.forEachRemaining(value -> list.add(value.b));

		return list;
	}
}
