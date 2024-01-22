package log.charter.data.managers.modes;

import log.charter.data.ChartData;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.handlers.MouseButtonPressReleaseHandler.MouseButtonPressReleaseData;
import log.charter.gui.panes.TempoBeatPane;

public class TempoMapModeHandler extends ModeHandler {
	private ChartData data;
	private CharterFrame frame;
	SelectionManager selectionManager;
	private UndoSystem undoSystem;

	public void init(final ChartData data, final CharterFrame frame, final SelectionManager selectionManager,
			final UndoSystem undoSystem) {
		this.data = data;
		this.frame = frame;
		this.selectionManager = selectionManager;
		this.undoSystem = undoSystem;
	}

	@Override
	public void handleEnd() {
		frame.setNextTime(data.songChart.beatsMap.beats.getLast().position());
	}

	@Override
	public void handleHome() {
		frame.setNextTime(data.songChart.beatsMap.beats.get(0).position());
	}

	@Override
	public void rightClick(final MouseButtonPressReleaseData clickData) {
		if (clickData.isXDrag() || clickData.pressHighlight.beat == null) {
			return;
		}

		new TempoBeatPane(data, frame, undoSystem, clickData.pressHighlight.beat);
	}

	@Override
	public void changeLength(final int change) {
	}
}
