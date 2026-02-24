package log.charter.data.undoSystem;

import java.util.List;

import log.charter.data.ChartData;
import log.charter.data.song.Showlight;
import log.charter.services.data.ChartTimeHandler;
import log.charter.util.CollectionUtils;

public class ShowlightsUndoState extends UndoState {
	private final List<Showlight> showlights;

	private ShowlightsUndoState(final ChartData data, final boolean fromUndo) {
		final List<Showlight> tmpShowlights = data.showlights();
		showlights = fromUndo ? tmpShowlights : CollectionUtils.map(tmpShowlights, Showlight::new);
	}

	public ShowlightsUndoState(final int vocalPathId, final List<Showlight> showlights) {
		this.showlights = CollectionUtils.map(showlights, Showlight::new);
	}

	public ShowlightsUndoState(final ChartData data) {
		this(data, false);
	}

	@Override
	public ShowlightsUndoState undo(final ChartData data, final ChartTimeHandler chartTimeHandler) {
		final ShowlightsUndoState redo = new ShowlightsUndoState(data, true);

		data.songChart.showlights = showlights;

		return redo;
	}
}