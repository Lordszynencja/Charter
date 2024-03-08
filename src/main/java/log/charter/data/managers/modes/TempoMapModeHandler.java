package log.charter.data.managers.modes;

import log.charter.data.ChartData;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.handlers.data.ChartTimeHandler;
import log.charter.gui.handlers.mouseAndKeyboard.MouseButtonPressReleaseHandler.MouseButtonPressReleaseData;
import log.charter.gui.panes.songEdits.TempoBeatPane;

public class TempoMapModeHandler extends ModeHandler {
	private ChartData chartData;
	private CharterFrame charterFrame;
	private ChartTimeHandler chartTimeHandler;
	private UndoSystem undoSystem;

	@Override
	public void rightClick(final MouseButtonPressReleaseData clickData) {
		if (clickData.isXDrag() || clickData.pressHighlight.beat == null) {
			return;
		}

		new TempoBeatPane(chartData, charterFrame, undoSystem, chartTimeHandler.maxTime(),
				clickData.pressHighlight.beat);
	}

	@Override
	public void changeLength(final int change) {
	}
}
