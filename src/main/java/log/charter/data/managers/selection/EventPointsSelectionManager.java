package log.charter.data.managers.selection;

import log.charter.data.ChartData;
import log.charter.data.types.PositionType;
import log.charter.data.types.PositionWithIdAndType;
import log.charter.gui.handlers.mouseAndKeyboard.MouseButtonPressReleaseHandler;
import log.charter.gui.handlers.mouseAndKeyboard.MouseButtonPressReleaseHandler.MouseButton;
import log.charter.gui.handlers.mouseAndKeyboard.MouseButtonPressReleaseHandler.MouseButtonPressData;
import log.charter.song.EventPoint;
import log.charter.util.CollectionUtils.ArrayList2;

class EventPointsSelectionManager extends SingleTypeSelectionManager<EventPoint> {
	private final ChartData data;
	private final MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler;

	EventPointsSelectionManager(final ChartData data,
			final MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler) {
		super(PositionType.EVENT_POINT);
		this.data = data;
		this.mouseButtonPressReleaseHandler = mouseButtonPressReleaseHandler;
	}

	@Override
	protected ArrayList2<EventPoint> getAvailable() {
		return PositionType.EVENT_POINT.getPositions(data);
	}

	public PositionWithIdAndType getTemporarySelected(
			final MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler) {
		final MouseButtonPressData pressData = mouseButtonPressReleaseHandler.getPressPosition(MouseButton.LEFT_BUTTON);
		if (pressData == null || pressData.highlight.eventPoint == null) {
			return null;
		}

		return pressData.highlight;
	}

	@Override
	protected Selection<EventPoint> makeSelection(final int id, final EventPoint selectable) {
		return new Selection<>(id, selectable);
	}

	@Override
	protected Selection<EventPoint> getTemporarySelect() {
		final MouseButtonPressData pressData = mouseButtonPressReleaseHandler.getPressPosition(MouseButton.LEFT_BUTTON);
		if (pressData == null || pressData.highlight.eventPoint == null) {
			return null;
		}

		return new Selection<>(pressData.highlight.id, pressData.highlight.eventPoint);
	}
}
