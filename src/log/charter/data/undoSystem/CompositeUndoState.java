package log.charter.data.undoSystem;

import log.charter.data.ChartData;
import log.charter.util.CollectionUtils.ArrayList2;

public class CompositeUndoState implements UndoState {
	private final ArrayList2<UndoState> undoStates;

	public CompositeUndoState(final UndoState... undoStates) {
		this.undoStates = new ArrayList2<>(undoStates);
	}

	public CompositeUndoState(final ArrayList2<UndoState> undoStates) {
		this.undoStates = undoStates;
	}

	@Override
	public UndoState undo(final ChartData data) {
		return new CompositeUndoState(undoStates.map(state -> state.undo(data)));
	}
}
