package log.charter.data.managers.selection;

import log.charter.data.ChartData;
import log.charter.data.types.PositionType;
import log.charter.gui.handlers.MouseButtonPressReleaseHandler;
import log.charter.gui.handlers.MouseButtonPressReleaseHandler.MouseButton;
import log.charter.gui.handlers.MouseButtonPressReleaseHandler.MouseButtonPressData;
import log.charter.song.notes.ChordOrNote;
import log.charter.util.CollectionUtils.ArrayList2;

class ChordsNotesSelectionManager extends SingleTypeSelectionManager<ChordOrNote> {
	private final ChartData data;
	private final MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler;

	ChordsNotesSelectionManager(final ChartData data,
			final MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler) {
		super(PositionType.GUITAR_NOTE);
		this.data = data;
		this.mouseButtonPressReleaseHandler = mouseButtonPressReleaseHandler;
	}

	@Override
	protected ArrayList2<ChordOrNote> getAvailable() {
		return PositionType.GUITAR_NOTE.getPositions(data);
	}

	@Override
	protected Selection<ChordOrNote> makeSelection(final int id, final ChordOrNote selectable) {
		return new Selection<>(id, selectable);
	}

	@Override
	protected Selection<ChordOrNote> getTemporarySelect() {
		final MouseButtonPressData pressData = mouseButtonPressReleaseHandler.getPressPosition(MouseButton.LEFT_BUTTON);
		if (pressData == null || pressData.highlight.chordOrNote == null) {
			return null;
		}

		return new Selection<>(pressData.highlight.id, pressData.highlight.chordOrNote);
	}
}
