package log.charter.data.undoSystem;

import log.charter.data.ChartData;
import log.charter.song.vocals.Vocals;

public class VocalUndoState implements UndoState {
	private final Vocals vocals;

	private VocalUndoState(final ChartData data, final boolean fromUndo) {
		final Vocals tmpVocals = data.songChart.vocals;
		vocals = fromUndo ? tmpVocals : new Vocals(tmpVocals);
	}

	public VocalUndoState(final ChartData data) {
		this(data, false);

	}

	@Override
	public VocalUndoState undo(final ChartData data) {
		final VocalUndoState redo = new VocalUndoState(data, true);

		data.songChart.vocals = vocals;

		return redo;
	}
}