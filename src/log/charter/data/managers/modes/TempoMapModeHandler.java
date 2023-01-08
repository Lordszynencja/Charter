package log.charter.data.managers.modes;

import log.charter.data.ChartData;
import log.charter.data.managers.HighlightManager;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.handlers.MouseButtonPressReleaseHandler.MouseButtonPressReleaseData;

public class TempoMapModeHandler extends ModeHandler {
	private ChartData data;
	private CharterFrame frame;
	private HighlightManager highlightManager;
	SelectionManager selectionManager;
	private UndoSystem undoSystem;

	public void init(final ChartData data, final CharterFrame frame, final HighlightManager highlightManager,
			final SelectionManager selectionManager, final UndoSystem undoSystem) {
		this.data = data;
		this.frame = frame;
		this.highlightManager = highlightManager;
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
	public void snapNotes() {
	}

	@Override
	public void rightClick(final MouseButtonPressReleaseData clickData) {
	}

	@Override
	public void changeLength(final int change) {
	}
}
