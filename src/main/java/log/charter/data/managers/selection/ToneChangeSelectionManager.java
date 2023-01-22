package log.charter.data.managers.selection;

import log.charter.data.ChartData;
import log.charter.data.types.PositionType;
import log.charter.data.types.PositionWithIdAndType;
import log.charter.gui.handlers.MouseButtonPressReleaseHandler;
import log.charter.gui.handlers.MouseButtonPressReleaseHandler.MouseButton;
import log.charter.gui.handlers.MouseButtonPressReleaseHandler.MouseButtonPressData;
import log.charter.song.ToneChange;
import log.charter.util.CollectionUtils.ArrayList2;

class ToneChangeSelectionManager extends SingleTypeSelectionManager<ToneChange> {
	private final ChartData data;
	private final MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler;

	ToneChangeSelectionManager(final ChartData data,
			final MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler) {
		super(PositionType.TONE_CHANGE);
		this.data = data;
		this.mouseButtonPressReleaseHandler = mouseButtonPressReleaseHandler;
	}

	@Override
	protected ArrayList2<ToneChange> getAvailable() {
		return PositionType.TONE_CHANGE.getPositions(data);
	}

	public PositionWithIdAndType getTemporarySelected(
			final MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler) {
		final MouseButtonPressData pressData = mouseButtonPressReleaseHandler.getPressPosition(MouseButton.LEFT_BUTTON);
		if (pressData == null || pressData.highlight.toneChange == null) {
			return null;
		}

		return pressData.highlight;
	}

	@Override
	protected Selection<ToneChange> makeSelection(final int id, final ToneChange selectable) {
		return new Selection<>(id, selectable);
	}

	@Override
	protected Selection<ToneChange> getTemporarySelect() {
		final MouseButtonPressData pressData = mouseButtonPressReleaseHandler.getPressPosition(MouseButton.LEFT_BUTTON);
		if (pressData == null || pressData.highlight.toneChange == null) {
			return null;
		}

		return new Selection<>(pressData.highlight.id, pressData.highlight.toneChange);
	}
}
