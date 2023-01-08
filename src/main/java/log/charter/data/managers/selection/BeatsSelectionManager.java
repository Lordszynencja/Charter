package log.charter.data.managers.selection;

import log.charter.data.ChartData;
import log.charter.data.types.PositionType;
import log.charter.gui.handlers.MouseButtonPressReleaseHandler;
import log.charter.gui.handlers.MouseButtonPressReleaseHandler.MouseButton;
import log.charter.gui.handlers.MouseButtonPressReleaseHandler.MouseButtonPressData;
import log.charter.song.Beat;
import log.charter.util.CollectionUtils.ArrayList2;

class BeatsSelectionManager extends SingleTypeSelectionManager<Beat> {
	private final ChartData data;
	private final MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler;

	BeatsSelectionManager(final ChartData data, final MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler) {
		super();
		this.data = data;
		this.mouseButtonPressReleaseHandler = mouseButtonPressReleaseHandler;
	}

	@Override
	protected ArrayList2<Beat> getAvailable() {
		return PositionType.BEAT.getPositions(data);
	}

	@Override
	protected Selection<Beat> makeSelection(final int id, final Beat selectable) {
		return new Selection<>(id, selectable);
	}

	@Override
	protected Selection<Beat> getTemporarySelect() {
		final MouseButtonPressData pressData = mouseButtonPressReleaseHandler.getPressPosition(MouseButton.LEFT_BUTTON);
		if (pressData == null || pressData.highlight.beat == null) {
			return null;
		}

		return new Selection<>(pressData.highlight.id, pressData.highlight.beat);
	}
}
