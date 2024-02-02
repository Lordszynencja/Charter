package log.charter.song.notes;

import java.util.List;

import log.charter.util.CollectionUtils.ArrayList2;

public interface IConstantPositionWithLength extends IConstantPosition {
	public static <T extends IConstantPositionWithLength> int findFirstIdAfterEqual(final ArrayList2<T> list,
			final int position) {
		if (list.isEmpty()) {
			return -1;
		}
		if (position > list.getLast().endPosition()) {
			return -1;
		}

		int minId = 0;
		int maxId = list.size() - 1;
		while (maxId - minId > 1) {
			final int id = (minId + maxId) / 2;
			if (list.get(id).endPosition() < position) {
				minId = id + 1;
			} else {
				maxId = id;
			}
		}

		return list.get(minId).endPosition() < position ? maxId : minId;
	}

	public static <T extends IConstantPositionWithLength> int findLastIdBefore(final List<T> list, final int position) {
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
			if (list.get(id).endPosition() >= position) {
				maxId = id - 1;
			} else {
				minId = id;
			}
		}

		return list.get(maxId).endPosition() >= position ? minId : maxId;
	}

	int length();

	default int endPosition() {
		return position() + length();
	}
}
