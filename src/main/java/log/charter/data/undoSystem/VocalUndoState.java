package log.charter.data.undoSystem;

import log.charter.data.ChartData;
import log.charter.data.song.vocals.VocalPath;
import log.charter.services.data.ChartTimeHandler;

public class VocalUndoState extends UndoState {
	private final int vocalPathId;
	private final VocalPath vocals;

	private VocalUndoState(final ChartData data, final boolean fromUndo) {
		final VocalPath tmpVocals = data.currentVocals();
		vocalPathId = data.currentVocals;
		vocals = fromUndo ? tmpVocals : new VocalPath(tmpVocals);
	}

	public VocalUndoState(final int vocalPathId, final VocalPath vocalPath) {
		this.vocalPathId = vocalPathId;
		vocals = new VocalPath(vocalPath);
	}

	public VocalUndoState(final ChartData data) {
		this(data, false);
	}

	@Override
	public VocalUndoState undo(final ChartData data, final ChartTimeHandler chartTimeHandler) {
		final VocalUndoState redo = new VocalUndoState(data, true);

		if (data.songChart.vocalPaths.size() > vocalPathId) {
			data.songChart.vocalPaths.set(vocalPathId, vocals);
		}

		return redo;
	}
}