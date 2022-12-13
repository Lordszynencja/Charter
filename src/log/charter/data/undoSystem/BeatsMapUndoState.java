package log.charter.data.undoSystem;

import log.charter.data.ChartData;
import log.charter.song.BeatsMap;

public class BeatsMapUndoState implements UndoState {
	private final BeatsMap beatsMap;

	private BeatsMapUndoState(final ChartData data, final boolean fromUndo) {
		final BeatsMap tmpBeatsMap = data.songChart.beatsMap;
		beatsMap = fromUndo ? tmpBeatsMap : new BeatsMap(tmpBeatsMap);
	}

	public BeatsMapUndoState(final ChartData data) {
		this(data, false);
	}

	@Override
	public BeatsMapUndoState undo(final ChartData data) {
		final BeatsMapUndoState redo = new BeatsMapUndoState(data, true);

		data.songChart.beatsMap = beatsMap;

		return redo;
	}

}
