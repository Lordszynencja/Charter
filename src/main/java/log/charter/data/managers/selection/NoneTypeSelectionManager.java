package log.charter.data.managers.selection;

import java.util.Set;

import log.charter.data.types.PositionType;
import log.charter.data.types.PositionWithIdAndType;
import log.charter.song.notes.IPosition;
import log.charter.util.CollectionUtils.ArrayList2;

public class NoneTypeSelectionManager implements TypeSelectionManager<IPosition> {

	@Override
	public SelectionAccessor<IPosition> getAccessor() {
		return new SelectionAccessor<>(PositionType.NONE, () -> new ArrayList2<>());
	}

	@Override
	public void addSelection(final PositionWithIdAndType closestPosition, final int x, final int y, final boolean ctrl,
			final boolean shift) {
	}

	@Override
	public void addAll() {
	}

	@Override
	public void clear() {
	}

	@Override
	public void add(final int id) {
	}

	@Override
	public void add(final ArrayList2<Integer> ids) {
	}

	@Override
	public void addPositions(final Set<Integer> positions) {
	}

}
