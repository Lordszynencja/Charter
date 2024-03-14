package log.charter.services.data.selection;

import static java.util.Arrays.asList;
import static log.charter.util.CollectionUtils.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import log.charter.data.ChartData;
import log.charter.data.song.position.IVirtualConstantPosition;
import log.charter.data.types.PositionType;
import log.charter.data.types.PositionType.PositionTypeManager;
import log.charter.services.mouseAndKeyboard.MouseButtonPressReleaseHandler;
import log.charter.services.mouseAndKeyboard.MouseButtonPressReleaseHandler.MouseButton;
import log.charter.services.mouseAndKeyboard.MouseButtonPressReleaseHandler.MouseButtonPressData;

class SelectionList<C extends IVirtualConstantPosition, P extends C, T extends P> {
	static interface SelectionMaker<T extends IVirtualConstantPosition> {
		Selection<T> make(int id, T selectable);
	}

	static interface TemporarySelectionSupplier<T extends IVirtualConstantPosition> {
		Selection<T> make();
	}

	public final PositionType type;
	private final PositionTypeManager<C, P, T> positionTypeManager;

	private ChartData chartData;
	private MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler;

	private Integer lastId = null;
	private final SortedSet<Integer> selectedIds = new TreeSet<>();

	SelectionList(final PositionType type) {
		this.type = type;
		positionTypeManager = type.manager();
	}

	private void addSelectablesUntil(final int toId) {
		final int fromId = lastId == null ? toId : lastId;
		if (fromId > toId) {
			for (int i = fromId; i >= toId; i--) {
				add(i);
			}
		} else {
			for (int i = fromId; i <= toId; i++) {
				add(i);
			}
		}
	}

	private void switchSelectable(final int id) {
		if (!selectedIds.remove(id)) {
			add(id);
		}
	}

	private void setSelectable(final int id) {
		selectedIds.clear();
		add(id);
	}

	public void addSelectablesWithModifiers(final int id, final boolean ctrl, final boolean shift) {
		if (ctrl) {
			switchSelectable(id);
			return;
		}
		if (shift) {
			addSelectablesUntil(id);
			return;
		}

		setSelectable(id);
	}

	public void addAll() {
		final int size = positionTypeManager.getItems(chartData).size();
		clear();

		for (int i = 0; i < size; i++) {
			add(i);
		}
	}

	public void add(final int id) {
		if (!selectedIds.contains(id)) {
			selectedIds.add(id);
		}
		lastId = id;
	}

	public void add(final Collection<Integer> ids) {
		ids.forEach(this::add);
	}

	public void addPositions(final Collection<C> positions) {
		add(positionTypeManager.getIdsForPositions(chartData, positions));
	}

	public Set<Integer> getSelectedIds() {
		return new TreeSet<>(selectedIds);
	}

	public void clear() {
		selectedIds.clear();
		lastId = null;
	}

	private Selection<T> getTemporarySelect() {
		final MouseButtonPressData pressData = mouseButtonPressReleaseHandler.getPressPosition(MouseButton.LEFT_BUTTON);
		if (pressData == null || pressData.highlight.type != type || !pressData.highlight.existingPosition) {
			return null;
		}

		return new Selection<>(pressData.highlight.id, pressData.highlight.get());
	}

	public Collection<Selection<T>> getSelectionWithTemporary() {
		if (!selectedIds.isEmpty()) {
			final List<T> items = positionTypeManager.getItems(chartData);
			return map(selectedIds, id -> new Selection<>(id, items.get(id)));
		}

		final Selection<T> temporarySelection = getTemporarySelect();
		if (temporarySelection == null) {
			return new ArrayList<>();
		}

		return asList(temporarySelection);
	}

	public SelectionAccessor<T> getAccessor() {
		return new SelectionAccessor<>(this);
	}

}
