package log.charter.data.managers.selection;

import java.util.HashSet;
import java.util.Set;

import log.charter.song.Position;
import log.charter.util.CollectionUtils.ArrayList2;

class SelectionList<T extends Position> {
	static interface SelectionMaker<T extends Position> {
		Selection<T> make(int id, T selectable);
	}

	static interface TemporarySelectionSupplier<T extends Position> {
		Selection<T> make();
	}

	private final SelectionMaker<T> selectionMaker;
	private final TemporarySelectionSupplier<T> temporarySelectionSupplier;

	final ArrayList2<Selection<T>> selected = new ArrayList2<>();

	SelectionList(final SelectionMaker<T> selectionMaker,
			final TemporarySelectionSupplier<T> temporarySelectionSupplier) {
		this.selectionMaker = selectionMaker;
		this.temporarySelectionSupplier = temporarySelectionSupplier;
	}

	private void addSelectables(final ArrayList2<T> available, final int fromId, final int toId) {
		final Set<Integer> selectedSignatures = new HashSet<>(selected.map(selection -> selection.selectable.position));

		for (int i = fromId; i <= toId; i++) {
			final T selectable = available.get(i);
			if (!selectedSignatures.contains(selectable.position)) {
				selected.add(selectionMaker.make(i, selectable));
			}
		}
	}

	private void addSelectables(final ArrayList2<T> available, int toId) {
		int fromId = selected.isEmpty() ? toId : selected.getLast().id;
		if (fromId > toId) {
			final int tmp = fromId;
			fromId = toId;
			toId = tmp;
		}

		addSelectables(available, fromId, toId);
	}

	private void switchSelectable(final ArrayList2<T> available, final int id) {
		if (!selected.removeIf(selection -> selection.id == id)) {
			selected.add(selectionMaker.make(id, available.get(id)));
		}
	}

	private void setSelectable(final ArrayList2<T> available, final int id) {
		selected.clear();
		selected.add(selectionMaker.make(id, available.get(id)));
	}

	void addSelectablesWithModifiers(final ArrayList2<T> available, final int id, final boolean ctrl,
			final boolean shift) {
		if (ctrl) {
			switchSelectable(available, id);
			return;
		}

		if (shift) {
			addSelectables(available, id);
			return;
		}

		setSelectable(available, id);
	}

	void addSelectablesFromToPosition(final ArrayList2<T> available, final int fromPosition, final int toPosition) {
		final Set<Integer> selectedSignatures = new HashSet<>(selected.map(selection -> selection.selectable.position));

		for (int i = 0; i < available.size(); i++) {
			final T selectable = available.get(i);
			if (selectable.position >= fromPosition && selectable.position <= toPosition
					&& !selectedSignatures.contains(selectable.position)) {
				selected.add(selectionMaker.make(i, selectable));
			}
		}
	}

	public void addAll(final ArrayList2<T> available) {
		clear();

		for (int i = 0; i < available.size(); i++) {
			final T selectable = available.get(i);
			selected.add(selectionMaker.make(i, selectable));
		}
	}

	public void clear() {
		if (!selected.isEmpty()) {
			selected.clear();
		}
	}

	private ArrayList2<Selection<T>> getSelectionWithTemporary() {
		if (!selected.isEmpty()) {
			return selected;
		}

		final Selection<T> temporarySelection = temporarySelectionSupplier.make();
		if (temporarySelection == null) {
			return new ArrayList2<>();
		}

		return new ArrayList2<>(temporarySelection);
	}

	public SelectionAccessor<T> getAccessor() {
		return new SelectionAccessor<>(this::getSelectionWithTemporary);
	}
}
