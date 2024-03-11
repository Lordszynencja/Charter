package log.charter.util.collections;

import static java.util.stream.Collectors.toCollection;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class HashMap2<T, U> extends HashMap<T, U> {
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

	public <V, W> HashMap2<V, W> map(final Function<T, V> keyMapper, final Function<U, W> valueMapper) {
		final HashMap2<V, W> newMap = new HashMap2<>();
		entrySet().stream()//
				.forEach(entry -> newMap.put(keyMapper.apply(entry.getKey()), valueMapper.apply(entry.getValue())));
		return newMap;
	}
}