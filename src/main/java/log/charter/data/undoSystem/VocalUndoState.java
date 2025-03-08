package log.charter.data.undoSystem;

import log.charter.data.ChartData;
import log.charter.data.song.vocals.VocalPath;
import log.charter.services.data.ChartTimeHandler;

public class VocalUndoState extends UndoState {
	private final int pathId;
	private final VocalPath vocals;

	private VocalUndoState(final ChartData data, final boolean fromUndo) {
		final VocalPath tmpVocals = data.currentVocals();
		pathId = data.currentVocals;
		vocals = fromUndo ? tmpVocals : new VocalPath(tmpVocals);
	}

	public VocalUndoState(final ChartData data) {
		this(data, false);

	}

	@Override
	public VocalUndoState undo(final ChartData data, final ChartTimeHandler chartTimeHandler) {
		final VocalUndoState redo = new VocalUndoState(data, true);

		if (data.songChart.vocalPaths.size() > pathId) {
			data.songChart.vocalPaths.set(pathId, vocals);
		}

		return redo;
	}
}