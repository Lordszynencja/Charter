package log.charter.util;

import log.charter.song.notes.IPosition;
import log.charter.util.CollectionUtils.ArrayList2;

public class SeekableList<T extends IPosition> {
	private final ArrayList2<T> list;
	private int position = 0;

	public SeekableList(final ArrayList2<T> list) {
		if (list == null) {
			throw new NullPointerException("list can't be null");
		}
		this.list = list;
	}

	public boolean hasPosition() {
		return position >= 0 && position < list.size();
	}

	public T getCurrent() {
		return hasPosition() ? list.get(position) : null;
	}

	public void next() {
		position++;
	}

	public void seekNextGreaterEqual(final int seekPosition) {
		while (hasPosition() && list.get(position).position() < seekPosition) {
			position++;
		}
	}

	public void seekPreviousLesserThan(final int seekPosition) {
		while (hasPosition() && list.get(position).position() >= seekPosition) {
			position--;
		}
	}
}
