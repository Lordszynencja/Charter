package log.charter.data.undoSystem;

import log.charter.data.ChartData;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.modes.EditMode;

public class BaseUndoState implements UndoState {
	private final UndoState internalUndoState;
	private final int markerPosition;

	public BaseUndoState(final ModeManager modeManager, final ChartData data) {
		if (modeManager.editMode == EditMode.GUITAR) {
			internalUndoState = new GuitarUndoState(data);
		} else if (modeManager.editMode == EditMode.VOCALS) {
			internalUndoState = new VocalUndoState(data);
		} else {
			internalUndoState = null;
		}

		markerPosition = data.time;
	}

	private BaseUndoState(final int markerPosition, final UndoState internalUndoState) {
		this.internalUndoState = internalUndoState;
		this.markerPosition = markerPosition;
	}

	@Override
	public UndoState undo(final ChartData data) {
		final BaseUndoState redo = new BaseUndoState(markerPosition, internalUndoState.undo(data));

		data.time = markerPosition;
		data.setNextTime(markerPosition);

		return redo;
	}
}
