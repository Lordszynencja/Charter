package log.charter.services.data.selection;

import java.util.function.Supplier;

import log.charter.data.song.position.IPosition;
import log.charter.data.types.PositionType;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashSet2;

public class SelectionAccessor<T extends IPosition> {
	public final PositionType type;
	private final Supplier<ArrayList2<Selection<T>>> selectedSupplier;

	SelectionAccessor(final PositionType type, final Supplier<ArrayList2<Selection<T>>> selectedSupplier) {
		this.type = type;
		this.selectedSupplier = selectedSupplier;
	}

	private ArrayList2<Selection<T>> getSortedCopy(final ArrayList2<Selection<T>> list) {
		final ArrayList2<Selection<T>> copy = new ArrayList2<>(list);
		copy.sort((selection0, selection1) -> Integer.compare(selection0.selectable.position(),
				selection1.selectable.position()));

		return copy;
	}

	public ArrayList2<Selection<T>> getSortedSelected() {
		return getSortedCopy(selectedSupplier.get());
	}

	public HashSet2<Selection<T>> getSelectedSet() {
		return new HashSet2<>(selectedSupplier.get());
	}

	public boolean isSelected() {
		return !selectedSupplier.get().isEmpty();
	}
}
