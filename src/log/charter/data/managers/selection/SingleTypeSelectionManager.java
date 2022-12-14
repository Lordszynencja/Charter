package log.charter.data.managers.selection;

import log.charter.data.PositionWithIdAndType;
import log.charter.data.managers.selection.SelectionList.SelectionMaker;
import log.charter.song.Position;
import log.charter.util.CollectionUtils.ArrayList2;

abstract class SingleTypeSelectionManager<T extends Position> implements TypeSelectionManager<T> {
	private final SelectionList<T> selectionList;

	SingleTypeSelectionManager(final SelectionMaker<T> selectionMaker) {
		this.selectionList = new SelectionList<>(selectionMaker);
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
	public void clear() {
		selectionList.clear();
	}
}
