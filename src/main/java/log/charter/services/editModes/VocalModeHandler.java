package log.charter.services.editModes;

import log.charter.data.ChartData;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.fractional.IConstantFractionalPositionWithEnd.ConstantFractionalPositionWithEnd;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.tabs.selectionEditor.CurrentSelectionEditor;
import log.charter.gui.panes.songEdits.VocalPane;
import log.charter.services.data.ChartItemsHandler;
import log.charter.services.data.selection.SelectionManager;
import log.charter.services.mouseAndKeyboard.KeyboardHandler;
import log.charter.services.mouseAndKeyboard.MouseButtonPressReleaseHandler.MouseButtonPressReleaseData;

public class VocalModeHandler implements ModeHandler {
	private static final long scrollTimeoutForUndo = 1000;

	private ChartData chartData;
	private ChartItemsHandler chartItemsHandler;
	private CharterFrame charterFrame;
	private CurrentSelectionEditor currentSelectionEditor;
	private KeyboardHandler keyboardHandler;
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	private long lastScrollTime = -scrollTimeoutForUndo;

	@Override
	public void rightClick(final MouseButtonPressReleaseData clickData) {
		if (clickData.pressHighlight.type != PositionType.VOCAL) {
			return;
		}

		if (clickData.pressHighlight.vocal != null) {
			undoSystem.addUndo();

			selectionManager.clear();
			chartData.currentVocals().removeNote(clickData.pressHighlight.id);
			return;
		}

		final FractionalPosition position = clickData.pressHighlight.toFraction(chartData.beats()).position();
		final FractionalPosition endPosition = chartData.beats().getMinEndPositionAfter(position)
				.toFraction(chartData.beats()).position();
		new VocalPane(new ConstantFractionalPositionWithEnd(position, endPosition), chartData, charterFrame,
				selectionManager, undoSystem);
	}

	@Override
	public void changeLength(int change) {
		if (keyboardHandler.shift()) {
			change *= 4;
		}

		if (System.currentTimeMillis() - lastScrollTime > scrollTimeoutForUndo) {
			undoSystem.addUndo();
		}

		chartItemsHandler.changePositionsWithLengthsByGrid(selectionManager.getSelectedElements(PositionType.VOCAL),
				chartData.currentVocals().vocals, change);

		currentSelectionEditor.selectionChanged(false);
		lastScrollTime = System.currentTimeMillis();
	}

	@Override
	public void handleNumber(final int number) {
	}

	@Override
	public void clearNumbers() {
	}
}
