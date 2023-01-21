package log.charter.data.managers.selection;

import log.charter.data.ChartData;
import log.charter.data.types.PositionType;
import log.charter.gui.handlers.MouseButtonPressReleaseHandler;
import log.charter.gui.handlers.MouseButtonPressReleaseHandler.MouseButton;
import log.charter.gui.handlers.MouseButtonPressReleaseHandler.MouseButtonPressData;
import log.charter.song.vocals.Vocal;
import log.charter.util.CollectionUtils.ArrayList2;

class VocalsSelectionManager extends SingleTypeSelectionManager<Vocal> {
	private final ChartData data;
	private final MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler;

	VocalsSelectionManager(final ChartData data, final MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler) {
		super(PositionType.VOCAL);
		this.data = data;
		this.mouseButtonPressReleaseHandler = mouseButtonPressReleaseHandler;
	}

	@Override
	protected ArrayList2<Vocal> getAvailable() {
		return PositionType.VOCAL.getPositions(data);
	}

	@Override
	protected Selection<Vocal> makeSelection(final int id, final Vocal selectable) {
		return new Selection<>(id, selectable);
	}

	@Override
	protected Selection<Vocal> getTemporarySelect() {
		final MouseButtonPressData pressData = mouseButtonPressReleaseHandler.getPressPosition(MouseButton.LEFT_BUTTON);
		if (pressData == null || pressData.highlight.vocal == null) {
			return null;
		}

		return new Selection<>(pressData.highlight.id, pressData.highlight.vocal);
	}
}
