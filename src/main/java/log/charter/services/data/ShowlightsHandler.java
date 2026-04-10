package log.charter.services.data;

import java.util.List;

import log.charter.data.ChartData;
import log.charter.data.song.Showlight;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.time.ConstantPosition;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.panes.songEdits.ShowlightPane;
import log.charter.services.data.selection.SelectionManager;
import log.charter.util.CollectionUtils;

public class ShowlightsHandler {
	private ChartData chartData;
	private CharterFrame charterFrame;
	private ChartTimeHandler chartTimeHandler;
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	public void insertShowlight() {
		selectionManager.clear();
		final FractionalPosition position = chartData.beats()
				.getPositionFromGridClosestTo(new ConstantPosition(chartTimeHandler.time()));

		Integer id = CollectionUtils.lastBeforeEqual(chartData.showlights(), position).findId();
		Showlight showlight = id == null ? null : chartData.showlights().get(id);
		if (showlight != null && showlight.position().equals(position)) {
			new ShowlightPane(chartData, charterFrame, undoSystem, showlight);
			return;
		}

		undoSystem.addUndo();

		showlight = new Showlight(position.toFraction(chartData.beats()).position());
		final List<Showlight> showlights = chartData.showlights();
		if (id == null) {
			id = 0;
		} else {
			id++;
		}
		showlights.add(id, showlight);
		selectionManager.addSelection(PositionType.SHOWLIGHT, id);

		new ShowlightPane(chartData, charterFrame, undoSystem, showlight, () -> {
			undoSystem.undo();
			undoSystem.removeRedo();
		});
	}
}
