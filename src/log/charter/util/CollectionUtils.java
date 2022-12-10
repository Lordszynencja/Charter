package log.charter.util;

import static java.util.stream.Collectors.toCollection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CollectionUtils {
	public static class Pair<A, B> {
		public A a;
		public B b;

		public Pair(final A a, final B b) {
			this.a = a;
			this.b = b;
		}
	}

	public static class ArrayList2<T> extends ArrayList<T> {
		private static final long serialVersionUID = 1L;

		public ArrayList2() {
			super();
		}

		public ArrayList2(final List<T> list) {
			super(list);
		}

		public T getLast() {
			return isEmpty() ? null : get(size() - 1);
		}

		public <U> ArrayList2<U> map(final Function<T, U> mapper) {
			return stream()//
					.map(mapper)//
					.collect(toCollection(ArrayList2::new));
		}

		public <U, V> HashMap2<U, V> toMap(final Function<T, Pair<U, V>> mapper) {
			return stream()//
					.map(mapper)//
					.collect(Collectors.toMap(pair -> pair.a, pair -> pair.b, (a, b) -> a, HashMap2::new));
		}
	}

	public static class HashMap2<T, U> extends HashMap<T, U> {
		private static final long serialVersionUID = 1L;

		public HashMap2() {
			super();
		}

		public HashMap2(final Map<T, U> other) {
			super(other);
		}

		public <V> ArrayList2<V> map(final BiFunction<T, U, V> mapper) {
			return entrySet().stream()//
					.map(entry -> mapper.apply(entry.getKey(), entry.getValue()))//
					.collect(toCollection(ArrayList2::new));
		}
	}

	public static int[] arrayOf(final int... values) {
		return values;
	}
}
