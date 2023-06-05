package log.charter.song.notes;

import static java.lang.Math.abs;

import java.util.List;

import log.charter.util.CollectionUtils.ArrayList2;

public interface IPosition extends Comparable<IPosition> {
	public static <T extends IPosition> Integer findClosestId(final ArrayList2<T> positions, final int position) {
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
			if (positions.get(id).position() > position) {
				right = id;
			} else {
				left = id;
			}
		}

		final int leftDistance = abs(positions.get(left).position() - position);
		final int rightDistance = abs(positions.get(right).position() - position);
		return leftDistance < rightDistance ? left : right;
	}

	public static <T extends IPosition> Integer findClosestId(final ArrayList2<T> positions, final IPosition position) {
		return findClosestId(positions, position.position());
	}

	public static <T extends IPosition> int findClosest(final ArrayList2<T> positions, final int position) {
		final Integer id = findClosestId(positions, position);
		if (id == null) {
			return position;
		}

		return positions.get(id).position();
	}

	public static <T extends IPosition> T findClosestPosition(final ArrayList2<T> positions, final int position) {
		final Integer id = findClosestId(positions, position);
		if (id == null) {
			return null;
		}

		return positions.get(id);
	}

	public static <T extends IPosition> int findFirstIdAfter(final ArrayList2<T> list, final int position) {
		if (list.isEmpty()) {
			return -1;
		}
		if (position >= list.getLast().position()) {
			return -1;
		}

		int minId = 0;
		int maxId = list.size() - 1;
		while (maxId - minId > 1) {
			final int id = (minId + maxId) / 2;
			if (list.get(id).position() <= position) {
				minId = id + 1;
			} else {
				maxId = id;
			}
		}

		return list.get(minId).position() <= position ? maxId : minId;
	}

	public static <T extends IPosition> int findFirstIdAfterEqual(final ArrayList2<T> list, final int position) {
		if (list.isEmpty()) {
			return -1;
		}
		if (position > list.getLast().position()) {
			return -1;
		}

		int minId = 0;
		int maxId = list.size() - 1;
		while (maxId - minId > 1) {
			final int id = (minId + maxId) / 2;
			if (list.get(id).position() < position) {
				minId = id + 1;
			} else {
				maxId = id;
			}
		}

		return list.get(minId).position() < position ? maxId : minId;
	}

	public static <T extends IPosition> T findFirstAfter(final ArrayList2<T> list, final int position) {
		final int id = findFirstIdAfter(list, position);
		return id < 0 ? null : list.get(id);
	}

	public static <T extends IPosition> int findLastIdBefore(final List<T> list, final int position) {
		if (list.isEmpty()) {
			return -1;
		}

		if (position <= list.get(0).position()) {
			return -1;
		}

		int minId = 0;
		int maxId = list.size() - 1;
		while (maxId - minId > 1) {
			final int id = (minId + maxId) / 2;
			if (list.get(id).position() >= position) {
				maxId = id - 1;
			} else {
				minId = id;
			}
		}

		return list.get(maxId).position() >= position ? minId : maxId;
	}

	public static <T extends IPosition> int findLastIdBeforeEqual(final List<T> list, final IPosition position) {
		return findLastIdBeforeEqual(list, position.position());
	}

	public static <T extends IPosition> int findLastIdBeforeEqual(final List<T> list, final int position) {
		if (list.isEmpty()) {
			return -1;
		}
		if (position < list.get(0).position()) {
			return -1;
		}

		int minId = 0;
		int maxId = list.size() - 1;
		while (maxId - minId > 1) {
			final int id = (minId + maxId) / 2;
			if (list.get(id).position() > position) {
				maxId = id - 1;
			} else {
				minId = id;
			}
		}

		return list.get(maxId).position() > position ? minId : maxId;
	}

	public static <T extends IPosition> T findLastBeforeEqual(final List<T> list, final int position) {
		final int id = findLastIdBeforeEqual(list, position);
		return id < 0 ? null : list.get(id);
	}

	public static <T extends IPosition> T findLastBefore(final List<T> list, final int position) {
		final int id = findLastIdBefore(list, position);
		return id < 0 ? null : list.get(id);
	}

	public static <T extends IPosition> List<T> getFromTo(final ArrayList2<T> list, final int from, final int to) {
		int fromId = findFirstIdAfterEqual(list, from);
		fromId = fromId == -1 ? 0 : fromId;
		int toId = findLastIdBeforeEqual(list, to);
		toId = toId == -1 ? list.size() : (toId + 1);
		return list.subList(fromId, toId);
	}

	int position();

	void position(int newPosition);

	@Override
	default int compareTo(final IPosition o) {
		return Integer.compare(position(), o.position());
	}
}
