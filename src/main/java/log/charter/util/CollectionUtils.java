package log.charter.util;

import static java.lang.Math.abs;
import static java.util.stream.Collectors.toCollection;
import static log.charter.util.Utils.nvl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import log.charter.data.song.position.IConstantFractionalPosition;
import log.charter.data.song.position.IConstantPosition;
import log.charter.util.collections.Pair;
import log.charter.util.data.Fraction;

public class CollectionUtils {
	public static interface Finder<E, P> {
		List<E> list();

		Integer findId();

		default int findId(final int defaultValue) {
			return nvl(findId(), defaultValue);
		}

		default E find() {
			final Integer id = findId();
			return id == null ? null : list().get(id);
		}
	}

	public static interface FinderGenerator {
		<E, P> Finder<E, P> generate(List<E> list, P position);
	}

	private static abstract class FinderWithComparator<C, E extends C, P extends C> implements Finder<E, P> {
		protected final List<E> list;
		protected final P position;
		protected final Comparator<C> comparator;

		private FinderWithComparator(final List<E> list, final P position, final Comparator<C> comparator) {
			this.list = list;
			this.position = position;
			this.comparator = comparator;
		}

		@Override
		public List<E> list() {
			return list;
		}
	}

	public static class FirstAfterFinder<C, E extends C, P extends C> extends FinderWithComparator<C, E, P> {
		private FirstAfterFinder(final List<E> list, final P position, final Comparator<C> comparator) {
			super(list, position, comparator);
		}

		@Override
		public Integer findId() {
			if (list.isEmpty() || comparator.compare(position, list.get(list.size() - 1)) >= 0) {
				return null;
			}

			int minId = 0;
			int maxId = list.size() - 1;
			while (maxId - minId > 1) {
				final int id = (minId + maxId) / 2;
				if (comparator.compare(position, list.get(id)) >= 0) {
					minId = id + 1;
				} else {
					maxId = id;
				}
			}

			return comparator.compare(position, list.get(minId)) >= 0 ? maxId : minId;
		}
	}

	public static <C, E extends C, P extends C> Finder<E, P> firstAfter(final List<E> list, final P position,
			final Comparator<C> comparator) {
		return new FirstAfterFinder<>(list, position, comparator);
	}

	public static <C extends Comparable<? super C>, E extends C, P extends C> Finder<E, P> firstAfter(
			final List<E> list, final P position) {
		return firstAfter(list, position, Comparable::compareTo);
	}

	public static <C extends IConstantPosition, E extends C, P extends C> Finder<E, P> firstAfter(final List<E> list,
			final P position) {
		return firstAfter(list, position, IConstantPosition::compareTo);
	}

	public static <C extends IConstantFractionalPosition, E extends C, P extends C> Finder<E, P> firstAfter(
			final List<E> list, final P position) {
		return firstAfter(list, position, IConstantFractionalPosition::compareTo);
	}

	public static class FirstAfterEqualFinder<C, E extends C, P extends C> extends FinderWithComparator<C, E, P> {
		private FirstAfterEqualFinder(final List<E> list, final P position, final Comparator<C> comparator) {
			super(list, position, comparator);
		}

		@Override
		public Integer findId() {
			if (list.isEmpty() || comparator.compare(position, list.get(list.size() - 1)) > 0) {
				return null;
			}

			int minId = 0;
			int maxId = list.size() - 1;
			while (maxId - minId > 1) {
				final int id = (minId + maxId) / 2;
				if (comparator.compare(position, list.get(id)) < 0) {
					minId = id + 1;
				} else {
					maxId = id;
				}
			}

			return comparator.compare(position, list.get(minId)) > 0 ? maxId : minId;
		}
	}

	public static <C, E extends C, P extends C> Finder<E, P> firstAfterEqual(final List<E> list, final P position,
			final Comparator<C> comparator) {
		return new FirstAfterEqualFinder<>(list, position, comparator);
	}

	public static <C extends Comparable<? super C>, E extends C, P extends C> Finder<E, P> firstAfterEqual(
			final List<E> list, final P position) {
		return firstAfterEqual(list, position, Comparable::compareTo);
	}

	public static <C extends IConstantPosition, E extends C, P extends C> Finder<E, P> firstAfterEqual(
			final List<E> list, final P position) {
		return firstAfterEqual(list, position, IConstantPosition::compareTo);
	}

	public static <C extends IConstantFractionalPosition, E extends C, P extends C> Finder<E, P> firstAfterEqual(
			final List<E> list, final P position) {
		return firstAfterEqual(list, position, IConstantFractionalPosition::compareTo);
	}

	public static class LastBeforeFinder<C, E extends C, P extends C> extends FinderWithComparator<C, E, P> {
		private LastBeforeFinder(final List<E> list, final P position, final Comparator<C> comparator) {
			super(list, position, comparator);
		}

		@Override
		public Integer findId() {
			if (list.isEmpty() || comparator.compare(position, list.get(0)) <= 0) {
				return null;
			}

			int minId = 0;
			int maxId = list.size() - 1;
			while (maxId - minId > 1) {
				final int id = (minId + maxId) / 2;
				if (comparator.compare(position, list.get(id)) <= 0) {
					maxId = id - 1;
				} else {
					minId = id;
				}
			}

			return comparator.compare(position, list.get(maxId)) <= 0 ? minId : maxId;
		}
	}

	public static <C, E extends C, P extends C> Finder<E, P> lastBefore(final List<E> list, final P position,
			final Comparator<C> comparator) {
		return new LastBeforeFinder<>(list, position, comparator);
	}

	public static <C extends IConstantPosition, E extends C, P extends C> Finder<E, P> lastBefore(final List<E> list,
			final P position) {
		return new LastBeforeFinder<>(list, position, IConstantPosition::compareTo);
	}

	public static <C extends IConstantFractionalPosition, E extends C, P extends C> Finder<E, P> lastBefore(
			final List<E> list, final P position) {
		return new LastBeforeFinder<>(list, position, IConstantFractionalPosition::compareTo);
	}

	public static class LastBeforeEqualFinder<C, E extends C, P extends C> extends FinderWithComparator<C, E, P> {
		private LastBeforeEqualFinder(final List<E> list, final P position, final Comparator<C> comparator) {
			super(list, position, comparator);
		}

		@Override
		public Integer findId() {
			if (list.isEmpty() || comparator.compare(position, list.get(0)) < 0) {
				return null;
			}

			int minId = 0;
			int maxId = list.size() - 1;
			while (maxId - minId > 1) {
				final int id = (minId + maxId) / 2;
				if (comparator.compare(position, list.get(id)) < 0) {
					maxId = id - 1;
				} else {
					minId = id;
				}
			}

			return comparator.compare(position, list.get(maxId)) < 0 ? minId : maxId;
		}
	}

	public static <C, E extends C, P extends C> Finder<E, P> lastBeforeEqual(final List<E> list, final P position,
			final Comparator<C> comparator) {
		return new LastBeforeEqualFinder<>(list, position, comparator);
	}

	public static <C extends IConstantPosition, E extends C, P extends C> Finder<E, P> lastBeforeEqual(
			final List<E> list, final P position) {
		return new LastBeforeEqualFinder<>(list, position, IConstantPosition::compareTo);
	}

	public static <C extends IConstantFractionalPosition, E extends C, P extends C> Finder<E, P> lastBeforeEqual(
			final List<E> list, final P position) {
		return new LastBeforeEqualFinder<>(list, position, IConstantFractionalPosition::compareTo);
	}

	public static class ClosestFinder<C, E extends C, P extends C, D> extends FinderWithComparator<C, E, P> {
		private final BiFunction<P, E, D> distanceCalculator;
		private final Comparator<D> distanceComparator;

		private ClosestFinder(final List<E> list, final P position, final Comparator<C> comparator,
				final BiFunction<P, E, D> distanceCalculator, final Comparator<D> distanceComparator) {
			super(list, position, comparator);

			this.distanceCalculator = distanceCalculator;
			this.distanceComparator = distanceComparator;
		}

		@Override
		public Integer findId() {
			if (list.isEmpty()) {
				return null;
			}
			if (list.size() == 1) {
				return 0;
			}

			int left = 0;
			int right = list.size() - 1;
			while (right - left > 1) {
				final int id = (left + right) / 2;
				if (comparator.compare(position, list.get(id)) < 0) {
					right = id;
				} else {
					left = id;
				}
			}

			final D leftDistance = distanceCalculator.apply(position, list.get(left));
			final D rightDistance = distanceCalculator.apply(position, list.get(right));
			return distanceComparator.compare(leftDistance, rightDistance) < 0 ? left : right;
		}
	}

	public static <C, E extends C, P extends C, D> Finder<E, P> closest(final List<E> list, final P position,
			final Comparator<C> comparator, final BiFunction<P, E, D> distanceCalculator,
			final Comparator<D> distanceComparator) {
		return new ClosestFinder<>(list, position, comparator, distanceCalculator, distanceComparator);
	}

	public static <C extends IConstantPosition, E extends C, P extends C> Finder<E, P> closest(final List<E> list,
			final P position) {
		return closest(list, position, IConstantPosition::compareTo, (a, b) -> abs(a.position() - b.position()),
				Integer::compare);
	}

	public static <C extends IConstantFractionalPosition, E extends C, P extends C> Finder<E, P> closest(
			final List<E> list, final P position) {
		return closest(list, position, IConstantFractionalPosition::compareTo,
				(a, b) -> a.fractionalPosition().add(b.fractionalPosition().negate()).absolute(),
				IConstantFractionalPosition::compareTo);
	}

	public static <C extends Fraction, E extends C, P extends C> Finder<E, P> closest(final List<E> list,
			final P position) {
		return closest(list, position, Fraction::compareTo, (a, b) -> a.add(b.negate()).absolute(),
				Fraction::compareTo);
	}

	private static <C, E extends C, T extends C> BiFunction<E, T, Integer> distance(
			final ToIntFunction<? super C> positionCalculator) {
		return (a, b) -> abs(positionCalculator.applyAsInt(a) - positionCalculator.applyAsInt(b));
	}

	public static <C, E extends C, P extends C> Finder<E, P> closest(final List<E> list, final P position,
			final Comparator<C> comparator, final ToIntFunction<C> positionCalculator) {
		return closest(list, position, comparator, distance(positionCalculator), Integer::compare);
	}

	public static <C extends Comparable<? super C>, E extends C, P extends C> Finder<E, P> closest(final List<E> list,
			final P position, final ToIntFunction<C> positionCalculator) {
		return closest(list, position, Comparable::compareTo, positionCalculator);
	}

	public static <C extends IConstantPosition, E extends C, P extends C> Finder<E, P> closest(final List<E> list,
			final P position, final ToIntFunction<C> positionCalculator) {
		return closest(list, position, IConstantPosition::compareTo, positionCalculator);
	}

	public static <C extends IConstantFractionalPosition, E extends C, P extends C> Finder<E, P> closest(
			final List<E> list, final P position, final ToIntFunction<C> positionCalculator) {
		return closest(list, position, IConstantFractionalPosition::compareTo, positionCalculator);
	}

	@SafeVarargs
	public static <K, V> Map<K, V> toMap(final Pair<K, V>... entries) {
		final Map<K, V> map = new HashMap<>();
		for (final Pair<K, V> entry : entries) {
			map.put(entry.a, entry.b);
		}

		return map;
	}

	public static <T, U, V> Map<U, V> toMap(final List<T> list, final BiConsumer<Map<U, V>, T> adder) {
		final Map<U, V> map = new HashMap<>();
		list.forEach(element -> adder.accept(map, element));
		return map;
	}

	public static <L extends Collection<E>, E> L filter(final Collection<E> items, final Predicate<E> filter,
			final Supplier<L> collectionGenerator) {
		return items.stream()//
				.filter(filter)//
				.collect(Collectors.toCollection(collectionGenerator));
	}

	public static <E> List<E> filter(final Collection<E> items, final Predicate<E> filter) {
		return filter(items, filter, ArrayList::new);
	}

	public static <C, E extends C, P extends C> List<E> getFromTo(final List<E> list, final P from, final P to,
			final Comparator<C> comparator) {
		final int fromId = firstAfterEqual(list, from, comparator).findId(0);
		final int toId = lastBeforeEqual(list, to, comparator).findId(list.size() - 1) + 1;
		if (toId <= fromId) {
			return new ArrayList<>();
		}

		return list.subList(fromId, toId);
	}

	public static <C extends IConstantPosition, E extends C, P extends C> List<E> getFromTo(final List<E> list,
			final P from, final P to) {
		return getFromTo(list, from, to, IConstantPosition::compareTo);
	}

	public static <T, U> List<U> map(final Collection<T> collection, final Function<T, U> mapper) {
		final List<U> newList = new ArrayList<>(collection.size());
		collection.stream().forEach(item -> newList.add(mapper.apply(item)));
		return newList;
	}

	public static <T, U, V, W> Map<V, W> map(final Map<T, U> map, final Function<T, V> keyMapper,
			final Function<U, W> valueMapper) {
		final Map<V, W> newMap = new HashMap<>();
		map.entrySet().stream()//
				.forEach(entry -> newMap.put(keyMapper.apply(entry.getKey()), valueMapper.apply(entry.getValue())));
		return newMap;
	}

	public static <K, V, E> List<E> map(final Map<K, V> map, final BiFunction<K, V, E> mapper) {
		return map.entrySet().stream()//
				.map(entry -> mapper.apply(entry.getKey(), entry.getValue()))//
				.collect(toCollection(ArrayList::new));
	}

	public static <T, U, V extends Collection<U>> V mapWithId(final List<T> list, final V newCollection,
			final BiFunction<Integer, T, U> mapper) {
		for (int i = 0; i < list.size(); i++) {
			newCollection.add(mapper.apply(i, list.get(i)));
		}

		return newCollection;
	}

	public static <T, U> List<U> mapWithId(final List<T> list, final BiFunction<Integer, T, U> mapper) {
		return mapWithId(list, new ArrayList<>(), mapper);
	}

	public static <T> boolean contains(final Collection<T> collection, final Predicate<T> predicate) {
		for (final T element : collection) {
			if (predicate.test(element)) {
				return true;
			}
		}

		return false;
	}
}
