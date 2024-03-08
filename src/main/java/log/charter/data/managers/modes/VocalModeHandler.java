package log.charter.data.managers.modes;

import static log.charter.song.notes.IPositionWithLength.changePositionsWithLengthsLength;

import log.charter.data.ChartData;
import log.charter.data.managers.selection.SelectionAccessor;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.tabs.selectionEditor.CurrentSelectionEditor;
import log.charter.gui.handlers.mouseAndKeyboard.KeyboardHandler;
import log.charter.gui.handlers.mouseAndKeyboard.MouseButtonPressReleaseHandler.MouseButtonPressReleaseData;
import log.charter.gui.panes.songEdits.VocalPane;
import log.charter.song.vocals.Vocal;

public class VocalModeHandler extends ModeHandler {
	private static final long scrollTimeoutForUndo = 1000;

	private ChartData chartData;
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
			chartData.songChart.vocals.removeNote(clickData.pressHighlight.id);
			return;
		}

		new VocalPane(clickData.pressHighlight.position(), chartData, charterFrame, selectionManager, undoSystem);
	}

	@Override
	public void changeLength(int change) {
		if (keyboardHandler.shift()) {
			change *= 4;
		}

		if (System.currentTimeMillis() - lastScrollTime > scrollTimeoutForUndo) {
			undoSystem.addUndo();
		}

		final SelectionAccessor<Vocal> selectedNotes = selectionManager.getSelectedAccessor(PositionType.VOCAL);
		changePositionsWithLengthsLength(chartData.songChart.beatsMap, selectedNotes.getSortedSelected(),
				chartData.songChart.vocals.vocals, change);

		currentSelectionEditor.selectionChanged(false);
		lastScrollTime = System.currentTimeMillis();
	}
}
