package log.charter.services.data.selection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import log.charter.data.song.position.IVirtualConstantPosition;
import log.charter.data.types.PositionType;

class NoneSelectionAccessor<T extends IVirtualConstantPosition> implements ISelectionAccessor<T> {
	@Override
	public PositionType type() {
		return PositionType.NONE;
	}

	@Override
	public List<Selection<T>> getSortedSelected() {
		return new ArrayList<>();
	}

	@Override
	public Set<Selection<T>> getSelectedSet() {
		return new HashSet<>();
	}

	@Override
	public Set<Integer> getSelectedIds(final PositionType forType) {
		return new HashSet<>();
	}

	@Override
	public boolean isSelected() {
		return false;
	}
}
