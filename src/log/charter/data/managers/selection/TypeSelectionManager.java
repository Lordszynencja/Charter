package log.charter.data.managers.selection;

import log.charter.data.PositionWithIdAndType;
import log.charter.song.Position;

public interface TypeSelectionManager<T extends Position> {
	SelectionAccessor<T> getAccessor();

	void addSelection(PositionWithIdAndType closestPosition, final int x, final int y, final boolean ctrl,
			final boolean shift);

	public void clear();
}
