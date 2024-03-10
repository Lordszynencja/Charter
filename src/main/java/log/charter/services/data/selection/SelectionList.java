package log.charter.services.data.selection;

import java.util.HashSet;
import java.util.Set;

import log.charter.data.types.PositionType;
import log.charter.song.notes.IPosition;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.Pair;

class SelectionList<T extends IPosition> {
	static interface SelectionMaker<T extends IPosition> {
		Selection<T> make(int id, T selectable);
	}

	static interface TemporarySelectionSupplier<T extends IPosition> {
		Selection<T> make();
	}

	public final PositionType type;
	private final TemporarySelectionSupplier<T> temporarySelectionSupplier;

	final ArrayList2<Selection<T>> selected = new ArrayList2<>();

	SelectionList(final PositionType type, final TemporarySelectionSupplier<T> temporarySelectionSupplier) {
		this.type = type;
		this.temporarySelectionSupplier = temporarySelectionSupplier;
	}

	private void addSelectables(final ArrayList2<T> available, final int fromId, final int toId) {
		final Set<Integer> selectedSignatures = new HashSet<>(
				selected.map(selection -> selection.selectable.position()));

		for (int i = fromId; i <= toId; i++) {
			final T selectable = available.get(i);
			if (!selectedSignatures.contains(selectable.position())) {
				selected.add(new Selection<>(i, selectable));
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
			selected.add(new Selection<>(id, available.get(id)));
		}
	}

	private void setSelectable(final ArrayList2<T> available, final int id) {
		selected.clear();
		selected.add(new Selection<>(id, available.get(id)));
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

	public void addAll(final ArrayList2<T> available) {
		clear();

		for (int i = 0; i < available.size(); i++) {
			final T selectable = available.get(i);
			selected.add(new Selection<>(i, selectable));
		}
	}

	public void add(final int id, final T selectable) {
		selected.add(new Selection<>(id, selectable));
	}

	public void add(final ArrayList2<Pair<Integer, T>> toAdd) {
		toAdd.stream()//
				.map(pair -> new Selection<>(pair.a, pair.b))//
				.forEach(selected::add);
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
		return new SelectionAccessor<>(type, this::getSelectionWithTemporary);
	}
}
