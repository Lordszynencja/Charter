package log.charter.data.undoSystem;

import log.charter.data.ChartData;

public interface UndoState {
	public UndoState undo(final ChartData data);
}
