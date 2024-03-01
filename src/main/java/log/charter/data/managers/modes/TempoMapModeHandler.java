package log.charter.data.managers.modes;

import log.charter.data.ChartData;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.handlers.data.ChartTimeHandler;
import log.charter.gui.handlers.mouseAndKeyboard.MouseButtonPressReleaseHandler.MouseButtonPressReleaseData;
import log.charter.gui.panes.songEdits.TempoBeatPane;

public class TempoMapModeHandler extends ModeHandler {
	private ChartTimeHandler chartTimeHandler;
	private ChartData data;
	private CharterFrame frame;
	private UndoSystem undoSystem;

	public void init(final ChartTimeHandler chartTimeHandler, final ChartData data, final CharterFrame frame,
			final UndoSystem undoSystem) {
		this.chartTimeHandler = chartTimeHandler;
		this.data = data;
		this.frame = frame;
		this.undoSystem = undoSystem;
	}

	@Override
	public void rightClick(final MouseButtonPressReleaseData clickData) {
		if (clickData.isXDrag() || clickData.pressHighlight.beat == null) {
			return;
		}

		new TempoBeatPane(data, frame, undoSystem, chartTimeHandler.audioLength(), clickData.pressHighlight.beat);
	}

	@Override
	public void changeLength(final int change) {
	}
}
