package log.charter.data.managers.modes;

import static log.charter.song.notes.IPositionWithLength.changePositionsWithLengthsLength;

import log.charter.data.ChartData;
import log.charter.data.managers.selection.SelectionAccessor;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.handlers.mouseAndKeyboard.KeyboardHandler;
import log.charter.gui.handlers.mouseAndKeyboard.MouseButtonPressReleaseHandler.MouseButtonPressReleaseData;
import log.charter.gui.panes.songEdits.VocalPane;
import log.charter.song.vocals.Vocal;

public class VocalModeHandler extends ModeHandler {
	private static final long scrollTimeoutForUndo = 1000;

	private ChartData data;
	private CharterFrame frame;
	private KeyboardHandler keyboardHandler;
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	private long lastScrollTime = -scrollTimeoutForUndo;

	public void init(final ChartData data, final CharterFrame frame, final KeyboardHandler keyboardHandler,
			final SelectionManager selectionManager, final UndoSystem undoSystem) {
		this.data = data;
		this.frame = frame;
		this.keyboardHandler = keyboardHandler;
		this.selectionManager = selectionManager;
		this.undoSystem = undoSystem;
	}

	@Override
	public void rightClick(final MouseButtonPressReleaseData clickData) {
		if (clickData.pressHighlight.type != PositionType.VOCAL) {
			return;
		}

		if (clickData.pressHighlight.vocal != null) {
			undoSystem.addUndo();

			selectionManager.clear();
			data.songChart.vocals.removeNote(clickData.pressHighlight.id);
			return;
		}

		new VocalPane(clickData.pressHighlight.position(), data, frame, selectionManager, undoSystem);
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
		changePositionsWithLengthsLength(data.songChart.beatsMap, selectedNotes.getSortedSelected(),
				data.songChart.vocals.vocals, change);

		frame.selectionChanged(false);
		lastScrollTime = System.currentTimeMillis();
	}
}
