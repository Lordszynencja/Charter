package log.charter.util.collections;

import static java.util.stream.Collectors.toCollection;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class HashSet2<T> extends HashSet<T> {
	private static final long serialVersionUID = 1L;

	public HashSet2() {
		super();
	}

	public HashSet2(final int initialCapacity) {
		super(initialCapacity);
	}

	public HashSet2(final Collection<T> collection) {
		super(collection);
	}

	public <U> HashSet2<U> map(final Function<T, U> mapper) {
		return stream()//
				.map(mapper)//
				.collect(toCollection(HashSet2::new));
	}

	public <U, V> HashMap2<U, V> toMap(final Function<T, Pair<U, V>> mapper) {
		return stream()//
				.map(mapper)//
				.collect(Collectors.toMap(pair -> pair.a, pair -> pair.b, (a, b) -> a, HashMap2::new));
	}

	public boolean contains(final Predicate<T> predicate) {
		for (final T element : this) {
			if (predicate.test(element)) {
				return true;
			}
		}

		return false;
	}
}