package log.charter.data.managers.selection;

import log.charter.data.types.PositionWithIdAndType;
import log.charter.song.enums.Position;
import log.charter.util.CollectionUtils.ArrayList2;

abstract class SingleTypeSelectionManager<T extends Position> implements TypeSelectionManager<T> {
	private final SelectionList<T> selectionList;

	SingleTypeSelectionManager() {
		this.selectionList = new SelectionList<>(this::makeSelection, this::getTemporarySelect);
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
	public void clear() {
		selectionList.clear();
	}

	abstract protected Selection<T> makeSelection(int id, T selectable);

	abstract protected Selection<T> getTemporarySelect();
}
