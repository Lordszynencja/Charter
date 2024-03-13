package log.charter.services.data.selection;

import java.util.List;
import java.util.Set;

import log.charter.data.song.position.IVirtualConstantPosition;
import log.charter.data.types.PositionType;

public interface ISelectionAccessor<T extends IVirtualConstantPosition> {

	public PositionType type();

	public List<Selection<T>> getSortedSelected();

	public Set<Selection<T>> getSelectedSet();

	public Set<Integer> getSelectedIds(final PositionType forType);

	public boolean isSelected();
}
