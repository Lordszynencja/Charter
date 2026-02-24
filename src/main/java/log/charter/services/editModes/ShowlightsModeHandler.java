package log.charter.services.editModes;

import java.util.List;

import log.charter.data.ChartData;
import log.charter.data.song.Showlight;
import log.charter.data.song.position.fractional.IConstantFractionalPosition;
import log.charter.data.types.PositionType;
import log.charter.data.types.PositionWithIdAndType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.panes.songEdits.ShowlightPane;
import log.charter.services.data.selection.SelectionManager;
import log.charter.services.mouseAndKeyboard.MouseButtonPressReleaseHandler.MouseButtonPressReleaseData;

public class ShowlightsModeHandler implements ModeHandler {
	private ChartData chartData;
	private CharterFrame charterFrame;
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	@Override
	public void rightClick(final MouseButtonPressReleaseData clickData) {
		if (clickData.pressHighlight.type != PositionType.SHOWLIGHT) {
			return;
		}

		final PositionWithIdAndType position = clickData.pressHighlight;
		selectionManager.clear();

		if (position.showlight != null) {
			new ShowlightPane(chartData, charterFrame, undoSystem, position.showlight, () -> {});
			return;
		}

		undoSystem.addUndo();

		final Showlight showlight = new Showlight(position.toFraction(chartData.beats()).position());
		final List<Showlight> showlights = chartData.showlights();
		showlights.add(showlight);
		showlights.sort(IConstantFractionalPosition::compareTo);

		new ShowlightPane(chartData, charterFrame, undoSystem, showlight, () -> {
			undoSystem.undo();
			undoSystem.removeRedo();
		});
	}

	@Override
	public void changeLength(final int change) {
	}

	@Override
	public void handleNumber(final int number) {
	}

	@Override
	public void clearNumbers() {
	}
}
