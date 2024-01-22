package log.charter.data.managers.selection;

import java.util.Set;

import log.charter.data.types.PositionType;
import log.charter.data.types.PositionWithIdAndType;
import log.charter.song.notes.IPosition;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.Pair;

abstract class SingleTypeSelectionManager<T extends IPosition> implements TypeSelectionManager<T> {
	private final SelectionList<T> selectionList;

	SingleTypeSelectionManager(final PositionType type) {
		this.selectionList = new SelectionList<>(type, this::makeSelection, this::getTemporarySelect);
	}

	abstract protected ArrayList2<T> getAvailable();

	@Override
	public SelectionAccessor<T> getAccessor() {
		return selectionList.getAccessor();
	}

	@Override
	public void addSelection(final PositionWithIdAndType closestPosition, final int x, final int y, final boolean ctrl,
			final boolean shift) {
		selectionList.addSelectablesWithModifiers(getAvailable(), closestPosition.id, ctrl, shift);
	}

	@Override
	public void addAll() {
		selectionList.addAll(getAvailable());
	}

	@Override
	public void add(final int id) {
		selectionList.add(id, getAvailable().get(id));
	}

	@Override
	public void add(final ArrayList2<Integer> ids) {
		final ArrayList2<T> available = getAvailable();
		selectionList.add(ids.map(id -> new Pair<>(id, available.get(id))));
	}

	@Override
	public void addPositions(final Set<Integer> positions) {
		final ArrayList2<T> available = getAvailable();
		for (int i = 0; i < available.size(); i++) {
			final T availablePosition = available.get(i);
			if (positions.contains(availablePosition.position())) {
				selectionList.add(i, availablePosition);
			}
		}
	}

	@Override
	public void clear() {
		selectionList.clear();
	}

	abstract protected Selection<T> makeSelection(int id, T selectable);

	abstract protected Selection<T> getTemporarySelect();
}
