package log.charter.data.undoSystem;

import static log.charter.data.undoSystem.UndoSystem.nextId;

import log.charter.data.ChartData;

public abstract class UndoState {
	public abstract UndoState undo(final ChartData data);

	public final int id = nextId++;
}
