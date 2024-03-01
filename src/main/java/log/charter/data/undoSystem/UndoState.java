package log.charter.data.undoSystem;

import static log.charter.data.undoSystem.UndoSystem.nextId;

import log.charter.data.ChartData;
import log.charter.gui.handlers.data.ChartTimeHandler;

public abstract class UndoState {
	public abstract UndoState undo(final ChartData data, ChartTimeHandler chartTimeHandler);

	public final int id = nextId++;
}
