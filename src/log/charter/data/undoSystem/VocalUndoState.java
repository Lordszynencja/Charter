package log.charter.data.undoSystem;

import log.charter.data.ChartData;
import log.charter.song.Vocals;

public class VocalUndoState implements UndoState {
	private final Vocals vocals;
	private final BeatsMapUndoState beatsMapUndoState;

	private VocalUndoState(final ChartData data, final BeatsMapUndoState beatsMapUndoState, final boolean fromUndo) {
		final Vocals tmpVocals = data.songChart.vocals;
		vocals = fromUndo ? tmpVocals : new Vocals(tmpVocals);
		this.beatsMapUndoState = beatsMapUndoState;
	}

	public VocalUndoState(final ChartData data) {
		this(data, new BeatsMapUndoState(data), false);

	}

	@Override
	public VocalUndoState undo(final ChartData data) {
		final VocalUndoState redo = new VocalUndoState(data, beatsMapUndoState.undo(data), true);

		data.songChart.vocals = vocals;

		return redo;
	}
}