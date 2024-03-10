package log.charter.services.data.selection;

import java.util.Set;

import log.charter.data.ChartData;
import log.charter.data.song.notes.IPosition;
import log.charter.data.types.PositionType;
import log.charter.data.types.PositionWithIdAndType;
import log.charter.services.mouseAndKeyboard.MouseButtonPressReleaseHandler;
import log.charter.services.mouseAndKeyboard.MouseButtonPressReleaseHandler.MouseButton;
import log.charter.services.mouseAndKeyboard.MouseButtonPressReleaseHandler.MouseButtonPressData;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.Pair;

public class TypeSelectionManager<T extends IPosition> {
	private final ChartData chartData;
	private final MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler;

	private final SelectionList<T> selectionList;

	public TypeSelectionManager(final PositionType type, final ChartData chartData,
			final MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler) {
		this.chartData = chartData;
		this.mouseButtonPressReleaseHandler = mouseButtonPressReleaseHandler;

		this.selectionList = new SelectionList<>(type, this::getTemporarySelect);
	}

	private ArrayList2<T> getAvailable() {
		return selectionList.type.getPositions(chartData);
	}

	public SelectionAccessor<T> getAccessor() {
		return selectionList.getAccessor();
	}

	private Selection<T> getTemporarySelect() {
		final MouseButtonPressData pressData = mouseButtonPressReleaseHandler.getPressPosition(MouseButton.LEFT_BUTTON);
		if (pressData == null || pressData.highlight.type != selectionList.type
				|| !pressData.highlight.existingPosition) {
			return null;
		}

		return new Selection<>(pressData.highlight.id, pressData.highlight.get());
	}

	public void addSelection(final PositionWithIdAndType closestPosition, final int x, final int y, final boolean ctrl,
			final boolean shift) {
		selectionList.addSelectablesWithModifiers(getAvailable(), closestPosition.id, ctrl, shift);
	}

	public void addAll() {
		selectionList.addAll(getAvailable());
	}

	public void add(final int id) {
		selectionList.add(id, getAvailable().get(id));
	}

	public void add(final ArrayList2<Integer> ids) {
		final ArrayList2<T> available = getAvailable();
		selectionList.add(ids.map(id -> new Pair<>(id, available.get(id))));
	}

	public void addPositions(final Set<Integer> positions) {
		final ArrayList2<T> available = getAvailable();
		for (int i = 0; i < available.size(); i++) {
			final T availablePosition = available.get(i);
			if (positions.contains(availablePosition.position())) {
				selectionList.add(i, availablePosition);
			}
		}
	}

	public void clear() {
		selectionList.clear();
	}

	public void refresh() {
		final ArrayList2<Integer> ids = selectionList.selected.map(selection -> selection.id);
		clear();
		add(ids);
	}
}
