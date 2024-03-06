package log.charter.data.managers.selection;

import java.util.Set;

import log.charter.data.types.PositionWithIdAndType;
import log.charter.song.notes.IPosition;
import log.charter.util.CollectionUtils.ArrayList2;

public interface TypeSelectionManager<T extends IPosition> {
	SelectionAccessor<T> getAccessor();

	void addSelection(PositionWithIdAndType closestPosition, final int x, final int y, final boolean ctrl,
			final boolean shift);

	public void addAll();

	public void add(int id);

	public void add(ArrayList2<Integer> ids);

	public void addPositions(Set<Integer> positions);

	public void clear();

	public void refresh();
}
