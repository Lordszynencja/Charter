package log.charter.data.managers.selection;

import log.charter.data.ChartData;
import log.charter.data.types.PositionType;
import log.charter.data.types.PositionWithIdAndType;
import log.charter.gui.handlers.mouseAndKeyboard.MouseButtonPressReleaseHandler;
import log.charter.gui.handlers.mouseAndKeyboard.MouseButtonPressReleaseHandler.MouseButton;
import log.charter.gui.handlers.mouseAndKeyboard.MouseButtonPressReleaseHandler.MouseButtonPressData;
import log.charter.song.Anchor;
import log.charter.util.CollectionUtils.ArrayList2;

class AnchorsSelectionManager extends SingleTypeSelectionManager<Anchor> {
	private final ChartData data;
	private final MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler;

	AnchorsSelectionManager(final ChartData data, final MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler) {
		super(PositionType.ANCHOR);
		this.data = data;
		this.mouseButtonPressReleaseHandler = mouseButtonPressReleaseHandler;
	}

	@Override
	protected ArrayList2<Anchor> getAvailable() {
		return PositionType.ANCHOR.getPositions(data);
	}

	public PositionWithIdAndType getTemporarySelected(
			final MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler) {
		final MouseButtonPressData pressData = mouseButtonPressReleaseHandler.getPressPosition(MouseButton.LEFT_BUTTON);
		if (pressData == null || pressData.highlight.anchor == null) {
			return null;
		}

		return pressData.highlight;
	}

	@Override
	protected Selection<Anchor> makeSelection(final int id, final Anchor selectable) {
		return new Selection<>(id, selectable);
	}

	@Override
	protected Selection<Anchor> getTemporarySelect() {
		final MouseButtonPressData pressData = mouseButtonPressReleaseHandler.getPressPosition(MouseButton.LEFT_BUTTON);
		if (pressData == null || pressData.highlight.anchor == null) {
			return null;
		}

		return new Selection<>(pressData.highlight.id, pressData.highlight.anchor);
	}
}
