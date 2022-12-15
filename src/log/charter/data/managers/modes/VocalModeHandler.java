package log.charter.data.managers.modes;

import java.util.function.Function;

import log.charter.data.ChartData;
import log.charter.data.managers.selection.Selection;
import log.charter.data.managers.selection.SelectionAccessor;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.handlers.KeyboardHandler;
import log.charter.gui.handlers.MouseButtonPressReleaseHandler.MouseButtonPressReleaseData;
import log.charter.gui.panes.LyricPane;
import log.charter.song.Position;
import log.charter.song.Vocal;
import log.charter.util.CollectionUtils.ArrayList2;

public class VocalModeHandler extends ModeHandler {
	private ChartData data;
	private CharterFrame frame;
	private KeyboardHandler keyboardHandler;
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	public void init(final ChartData data, final CharterFrame frame, final KeyboardHandler keyboardHandler,
			final SelectionManager selectionManager, final UndoSystem undoSystem) {
		this.data = data;
		this.frame = frame;
		this.keyboardHandler = keyboardHandler;
		this.selectionManager = selectionManager;
		this.undoSystem = undoSystem;
	}

	private int getTimeValue(final int defaultValue,
			final Function<ArrayList2<? extends Position>, Integer> positionFromListGetter) {
		if (!keyboardHandler.ctrl()) {
			return defaultValue;
		}

		final ArrayList2<Vocal> vocals = data.songChart.vocals.vocals;
		return vocals.isEmpty() ? defaultValue : positionFromListGetter.apply(vocals);
	}

	@Override
	public void handleEnd() {
		frame.setNextTime(getTimeValue(data.music.msLength(), list -> list.getLast().position));
	}

	@Override
	public void handleHome() {
		frame.setNextTime(getTimeValue(0, list -> list.get(0).position));
	}

	@Override
	public void handleLyricsEdit() {
		final SelectionAccessor<Vocal> selectedVocalsAccessor = selectionManager
				.getSelectedAccessor(PositionType.VOCAL);
		if (!selectedVocalsAccessor.isSelected()) {
			return;
		}

		final Selection<Vocal> selection = selectedVocalsAccessor.getSortedSelected().get(0);

		new LyricPane(selection.id, selection.selectable, frame, data, selectionManager, undoSystem);
	}

	@Override
	public void snapNotes() {
		// TODO Auto-generated method stub

	}

	@Override
	public void rightClick(final MouseButtonPressReleaseData clickData) {
		if (clickData.pressHighlight.vocal != null) {
			data.songChart.vocals.removeNote(clickData.pressHighlight.id);
			return;
		}

		new LyricPane(clickData.pressHighlight.position, frame, data, selectionManager, undoSystem);
	}
}
