package log.charter.song;

import static java.lang.Math.abs;

import log.charter.util.CollectionUtils.ArrayList2;

public class Position implements Comparable<Position> {
	public static <T extends Position> Integer findClosest(final ArrayList2<T> positions, final int position) {
		if (positions.isEmpty()) {
			return null;
		}
		if (positions.size() == 1) {
			return 0;
		}

		int left = 0;
		int right = positions.size() - 1;
		while (right - left > 1) {
			final int id = (left + right) / 2;
			if (positions.get(id).position > position) {
				right = id;
			} else {
				left = id;
			}
		}

		final int leftDistance = abs(positions.get(left).position - position);
		final int rightDistance = abs(positions.get(right).position - position);
		return leftDistance < rightDistance ? left : right;
	}

	public static <T extends Position> int findFirstIdAfter(final ArrayList2<T> list, final int position) {
		if (position >= list.getLast().position) {
			return -1;
		}

		int minId = 0;
		int maxId = list.size() - 1;
		while (maxId - minId > 1) {
			final int id = (minId + maxId) / 2;
			if (list.get(id).position <= position) {
				minId = id + 1;
			} else {
				maxId = id;
			}
		}

		return list.get(minId).position <= position ? maxId : minId;
	}

	public static <T extends Position> T findFirstAfter(final ArrayList2<T> list, final int position) {
		final int id = findFirstIdAfter(list, position);
		return id < 0 ? null : list.get(id);
	}

	public static <T extends Position> int findLastIdBefore(final ArrayList2<T> list, final int position) {
		if (position <= list.get(0).position) {
			return -1;
		}

		int minId = 0;
		int maxId = list.size() - 1;
		while (maxId - minId > 1) {
			final int id = (minId + maxId) / 2;
			if (list.get(id).position >= position) {
				maxId = id - 1;
			} else {
				minId = id;
			}
		}

		return list.get(maxId).position >= position ? minId : maxId;
	}

	public static <T extends Position> T findLastBefore(final ArrayList2<T> list, final int position) {
		final int id = findLastIdBefore(list, position);
		return id < 0 ? null : list.get(id);
	}

	public int position;

	public Position(final int position) {
		this.position = position;
	}

	public Position(final Position other) {
		position = other.position;
	}

	@Override
	public int compareTo(final Position o) {
		return Integer.compare(position, o.position);
	}
}
