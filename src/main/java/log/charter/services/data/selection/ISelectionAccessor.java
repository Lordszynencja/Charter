package log.charter.services.data.selection;

import java.util.List;
import java.util.Set;

import log.charter.data.song.position.virtual.IVirtualConstantPosition;
import log.charter.data.types.PositionType;

public interface ISelectionAccessor<T extends IVirtualConstantPosition> {

	public PositionType type();

	public List<Selection<T>> getSelected();

	public List<Integer> getSelectedIds(final PositionType forType);

	public Set<Integer> getSelectedIdsSet(final PositionType forType);

	public List<T> getSelectedElements();

	public boolean isSelected();
}
