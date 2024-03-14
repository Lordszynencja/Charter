package log.charter.services.data.selection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import log.charter.data.song.position.IVirtualConstantPosition;
import log.charter.data.types.PositionType;

class SelectionAccessor<T extends IVirtualConstantPosition> implements ISelectionAccessor<T> {
	private final SelectionList<?, ?, T> selectionList;
	private final Comparator<IVirtualConstantPosition> comparator;

	SelectionAccessor(final SelectionList<?, ?, T> selectionList) {
		this.selectionList = selectionList;
		comparator = selectionList.type.manager().comparator();
	}

	@Override
	public PositionType type() {
		return selectionList.type;
	}

	private List<Selection<T>> getSortedCopy(final Collection<Selection<T>> list) {
		final List<Selection<T>> copy = new ArrayList<>(list);
		copy.sort((a, b) -> comparator.compare(a.selectable, b.selectable));
		return copy;
	}

	@Override
	public List<Selection<T>> getSortedSelected() {
		return getSortedCopy(selectionList.getSelectionWithTemporary());
	}

	@Override
	public Set<Selection<T>> getSelectedSet() {
		return new HashSet<>(selectionList.getSelectionWithTemporary());
	}

	@Override
	public Set<Integer> getSelectedIds(final PositionType forType) {
		if (selectionList.type != forType) {
			return new HashSet<>();
		}

		return selectionList.getSelectedIds();
	}

	@Override
	public boolean isSelected() {
		return !selectionList.getSelectionWithTemporary().isEmpty();
	}
}
