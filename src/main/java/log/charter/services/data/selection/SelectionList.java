package log.charter.services.data.selection;

import static java.util.Arrays.asList;
import static log.charter.util.CollectionUtils.contains;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

	final List<Selection<T>> selected = new ArrayList<>();

	SelectionList(final PositionType type) {
		this.type = type;
		positionTypeManager = type.manager();
	}

	private void addSelectables(final int fromId, final int toId) {
		final Set<Integer> ids = new HashSet<>();

		for (int i = fromId; i <= toId; i++) {
			ids.add(i);
		}

		add(ids);
	}

	private void addSelectablesUntil(int toId) {
		int fromId = selected.isEmpty() ? toId : selected.get(selected.size() - 1).id;
		if (fromId > toId) {
			final int tmp = fromId;
			fromId = toId;
			toId = tmp;
		}

		addSelectables(fromId, toId);
	}

	private void switchSelectable(final int id) {
		if (!selected.removeIf(selection -> selection.id == id)) {
			add(id);
		}
	}

	private void setSelectable(final int id) {
		selected.clear();
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
		final List<T> items = positionTypeManager.getItems(chartData);
		clear();

		for (int i = 0; i < items.size(); i++) {
			selected.add(new Selection<>(i, items.get(i)));
		}
	}

	public void add(final int id) {
		if (!contains(selected, s -> s.id == id)) {
			selected.add(new Selection<>(id, positionTypeManager.getItems(chartData).get(id)));
		}
	}

	public void add(final Collection<Integer> ids) {
		ids.forEach(this::add);
	}

	public void addPositions(final Collection<C> positions) {
		add(positionTypeManager.getIdsForPositions(chartData, positions));
	}

	public Set<Integer> getSelectedIdsSet() {
		return selected.stream().map(s -> s.id).collect(Collectors.toSet());
	}

	public List<Integer> getSelectedIds() {
		return selected.stream().map(s -> s.id).collect(Collectors.toList());
	}

	public void clear() {
		if (!selected.isEmpty()) {
			selected.clear();
		}
	}

	private Selection<T> getTemporarySelect() {
		final MouseButtonPressData pressData = mouseButtonPressReleaseHandler.getPressPosition(MouseButton.LEFT_BUTTON);
		if (pressData == null || pressData.highlight.type != type || !pressData.highlight.existingPosition) {
			return null;
		}

		return new Selection<>(pressData.highlight.id, pressData.highlight.get());
	}

	public List<Selection<T>> getSelectionWithTemporary() {
		if (!selected.isEmpty()) {
			return selected;
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

	public void refresh() {
		final List<Integer> ids = getSelectedIds();
		clear();
		add(ids);
	}

}
