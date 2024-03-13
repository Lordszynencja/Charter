package log.charter.util.collections;

import static java.util.stream.Collectors.toCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import log.charter.util.CollectionUtils;

public class ArrayList2<T> extends ArrayList<T> {
	private static final long serialVersionUID = 1L;

	public ArrayList2() {
		super();
	}

	public ArrayList2(final int initialCapacity) {
		super(initialCapacity);
	}

	public ArrayList2(final List<T> list) {
		super(list);
	}

	@SafeVarargs
	public ArrayList2(final T... elements) {
		super();
		for (final T element : elements) {
			add(element);
		}
	}

	public T getLast() {
		return isEmpty() ? null : get(size() - 1);
	}

	public <U> ArrayList2<U> map(final Function<T, U> mapper) {
		return stream()//
				.map(mapper)//
				.collect(toCollection(ArrayList2::new));
	}

	public <U> ArrayList2<U> mapWithId(final BiFunction<Integer, T, U> mapper) {
		return CollectionUtils.mapWithId(this, new ArrayList2<>(size()), mapper);
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