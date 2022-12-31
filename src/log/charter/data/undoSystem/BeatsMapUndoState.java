package log.charter.data.undoSystem;

import log.charter.data.ChartData;
import log.charter.song.BeatsMap;

public class BeatsMapUndoState implements UndoState {
	public final BeatsMap beatsMap;

	private BeatsMapUndoState(final BeatsMap beatsMap) {
		this.beatsMap = beatsMap;
	}

	public BeatsMapUndoState(final ChartData data) {
		this(new BeatsMap(data.songChart.beatsMap));
	}

	@Override
	public BeatsMapUndoState undo(final ChartData data) {
		final BeatsMapUndoState redo = new BeatsMapUndoState(data.songChart.beatsMap);

		data.songChart.beatsMap = beatsMap;

		return redo;
	}

}
