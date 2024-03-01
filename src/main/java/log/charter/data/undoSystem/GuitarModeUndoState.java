package log.charter.data.undoSystem;

import log.charter.data.ChartData;
import log.charter.gui.handlers.data.ChartTimeHandler;

public class GuitarModeUndoState extends UndoState {
	private final GuitarUndoState guitarUndoState;

	GuitarModeUndoState(final GuitarUndoState guitarUndoState) {
		this.guitarUndoState = guitarUndoState;
	}

	public GuitarModeUndoState(final ChartData data) {
		this(new GuitarUndoState(data));
	}

	@Override
	public GuitarModeUndoState undo(final ChartData data, final ChartTimeHandler chartTimeHandler) {
		final GuitarModeUndoState redo = new GuitarModeUndoState(guitarUndoState.undo(data, chartTimeHandler));
		return redo;
	}
}
