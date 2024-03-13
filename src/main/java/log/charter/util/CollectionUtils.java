package log.charter.util;

import static java.lang.Math.abs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

public class CollectionUtils {
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

	public static <E extends Comparable<E>, T extends E, D extends Comparable<D>> int findClosestId(
			final List<T> positions, final E position, final BiFunction<T, E, D> distanceCalculator) {
		if (positions.isEmpty()) {
			return -1;
		}
		if (positions.size() == 1) {
			return 0;
		}

		int left = 0;
		int right = positions.size() - 1;
		while (right - left > 1) {
			final int id = (left + right) / 2;
			if (position.compareTo(positions.get(id)) < 0) {
				right = id;
			} else {
				left = id;
			}
		}

		final D leftDistance = distanceCalculator.apply(positions.get(left), position);
		final D rightDistance = distanceCalculator.apply(positions.get(right), position);
		return leftDistance.compareTo(rightDistance) < 0 ? left : right;
	}

	public static <E extends Comparable<E>, T extends E, D extends Comparable<D>> int findClosestId(
			final List<T> positions, final E position, final ToIntFunction<E> positionCalculator) {
		return findClosestId(positions, position,
				(a, b) -> abs(positionCalculator.applyAsInt(a) - positionCalculator.applyAsInt(b)));
	}

	public static <E extends Comparable<E>, T extends E, D extends Comparable<D>> T findClosest(final List<T> positions,
			final E position, final BiFunction<T, E, D> distanceCalculator) {
		final int id = findClosestId(positions, position, distanceCalculator);
		return id == -1 ? null : positions.get(id);
	}

	public static <E extends Comparable<E>, T extends E> int findFirstIdAfter(final List<T> list, final E position) {
		if (list.isEmpty() || position.compareTo(list.get(list.size() - 1)) >= 0) {
			return -1;
		}

		int minId = 0;
		int maxId = list.size() - 1;
		while (maxId - minId > 1) {
			final int id = (minId + maxId) / 2;
			if (position.compareTo(list.get(id)) >= 0) {
				minId = id + 1;
			} else {
				maxId = id;
			}
		}

		return position.compareTo(list.get(minId)) >= 0 ? maxId : minId;
	}

	public static <E extends Comparable<E>, T extends E> T findFirstAfter(final List<T> list, final E position) {
		final int id = findFirstIdAfter(list, position);
		return id == -1 ? null : list.get(id);
	}

	public static <E extends Comparable<E>, T extends E> int findFirstIdAfterEqual(final List<T> list,
			final E position) {
		if (list.isEmpty() || position.compareTo(list.get(list.size() - 1)) > 0) {
			return -1;
		}

		int minId = 0;
		int maxId = list.size() - 1;
		while (maxId - minId > 1) {
			final int id = (minId + maxId) / 2;
			if (position.compareTo(list.get(id)) < 0) {
				minId = id + 1;
			} else {
				maxId = id;
			}
		}

		return position.compareTo(list.get(minId)) > 0 ? maxId : minId;
	}

	public static <E extends Comparable<E>, T extends E> T findFirstAfterEqual(final List<T> list, final E position) {
		final int id = findFirstIdAfterEqual(list, position);
		return id == -1 ? null : list.get(id);
	}

	public static <E extends Comparable<E>, T extends E> int findLastIdBefore(final List<T> list, final E position) {
		if (list.isEmpty() || position.compareTo(list.get(0)) <= 0) {
			return -1;
		}

		int minId = 0;
		int maxId = list.size() - 1;
		while (maxId - minId > 1) {
			final int id = (minId + maxId) / 2;
			if (position.compareTo(list.get(id)) <= 0) {
				maxId = id - 1;
			} else {
				minId = id;
			}
		}

		return position.compareTo(list.get(maxId)) <= 0 ? minId : maxId;
	}

	public static <E extends Comparable<E>, T extends E> T findLastBefore(final List<T> list, final E position) {
		final int id = findLastIdBefore(list, position);
		return id == -1 ? null : list.get(id);
	}

	public static <E extends Comparable<E>, T extends E> int findLastIdBeforeEqual(final List<T> list,
			final E position) {
		if (list.isEmpty() || position.compareTo(list.get(0)) < 0) {
			return -1;
		}

		int minId = 0;
		int maxId = list.size() - 1;
		while (maxId - minId > 1) {
			final int id = (minId + maxId) / 2;
			if (position.compareTo(list.get(id)) < 0) {
				maxId = id - 1;
			} else {
				minId = id;
			}
		}

		return position.compareTo(list.get(maxId)) < 0 ? minId : maxId;
	}

	public static <E extends Comparable<E>, T extends E> T findLastBeforeEquals(final List<T> list, final E position) {
		final Integer id = findLastIdBeforeEqual(list, position);
		return id == -1 ? null : list.get(id);
	}

	public static <E extends Comparable<E>, T extends E> List<T> getFromTo(final List<T> list, final E from,
			final E to) {
		Integer fromId = findFirstIdAfterEqual(list, from);
		fromId = fromId == null ? 0 : fromId;
		Integer toId = findLastIdBeforeEqual(list, to);
		toId = toId == null ? list.size() : (toId + 1);
		if (toId < fromId) {
			return new ArrayList<>();
		}

		return list.subList(fromId, toId);
	}
}
