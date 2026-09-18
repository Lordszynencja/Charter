package log.charter.data.undoSystem;

import log.charter.data.ChartData;
import log.charter.data.song.vocals.VocalPath;
import log.charter.gui.components.tabs.TextTab;
import log.charter.services.data.ChartTimeHandler;

public class VocalUndoState extends UndoState {
	private final int vocalPathId;
	private final VocalPath vocals;
	private final String text;

	private VocalUndoState(final ChartData data, final String text, final boolean fromUndo) {
		final VocalPath tmpVocals = data.currentVocals();
		vocalPathId = data.currentVocals;
		vocals = fromUndo ? tmpVocals : new VocalPath(tmpVocals);
		this.text = text;
	}

	public VocalUndoState(final int vocalPathId, final VocalPath vocalPath, final String text) {
		this.vocalPathId = vocalPathId;
		vocals = new VocalPath(vocalPath);
		this.text = text;
	}

	public VocalUndoState(final ChartData data, final String text) {
		this(data, text, false);
	}

	@Override
	public VocalUndoState undo(final ChartData data, final ChartTimeHandler chartTimeHandler, final TextTab textTab) {
		final VocalUndoState redo = new VocalUndoState(data, textTab.getText(), true);

		if (data.songChart.vocalPaths.size() > vocalPathId) {
			data.songChart.vocalPaths.set(vocalPathId, vocals);
		}

		textTab.setText(text);

		return redo;
	}
}