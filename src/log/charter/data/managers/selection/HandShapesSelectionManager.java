package log.charter.data.managers.selection;

import log.charter.data.ChartData;
import log.charter.data.types.PositionType;
import log.charter.gui.handlers.MouseButtonPressReleaseHandler;
import log.charter.gui.handlers.MouseButtonPressReleaseHandler.MouseButton;
import log.charter.gui.handlers.MouseButtonPressReleaseHandler.MouseButtonPressData;
import log.charter.song.HandShape;
import log.charter.util.CollectionUtils.ArrayList2;

class HandShapesSelectionManager extends SingleTypeSelectionManager<HandShape> {
	private final ChartData data;
	private final MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler;

	HandShapesSelectionManager(final ChartData data,
			final MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler) {
		super();
		this.data = data;
		this.mouseButtonPressReleaseHandler = mouseButtonPressReleaseHandler;
	}

	@Override
	protected ArrayList2<HandShape> getAvailable() {
		return PositionType.HAND_SHAPE.getPositions(data);
	}

	@Override
	protected Selection<HandShape> makeSelection(final int id, final HandShape selectable) {
		return new Selection<>(id, selectable);
	}

	@Override
	protected Selection<HandShape> getTemporarySelect() {
		final MouseButtonPressData pressData = mouseButtonPressReleaseHandler.getPressPosition(MouseButton.LEFT_BUTTON);
		if (pressData == null || pressData.highlight.handShape == null) {
			return null;
		}

		return new Selection<>(pressData.highlight.id, pressData.highlight.handShape);
	}
}
