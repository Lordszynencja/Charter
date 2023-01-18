package log.charter.data.undoSystem;

import log.charter.data.ChartData;

public class GuitarModeUndoState extends UndoState {
	private final BeatsMapUndoState beatsMapUndoState;
	private final GuitarUndoState guitarUndoState;

	GuitarModeUndoState(final GuitarUndoState guitarUndoState, final BeatsMapUndoState beatsMapUndoState) {
		this.beatsMapUndoState = beatsMapUndoState;
		this.guitarUndoState = guitarUndoState;
	}

	public GuitarModeUndoState(final ChartData data) {
		this(new GuitarUndoState(data), new BeatsMapUndoState(data));
	}

	@Override
	public GuitarModeUndoState undo(final ChartData data) {
		final GuitarModeUndoState redo = new GuitarModeUndoState(guitarUndoState.undo(data),
				beatsMapUndoState.undo(data));
		return redo;
	}
}
