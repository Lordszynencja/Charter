package log.charter.data.undoSystem;

import log.charter.data.ChartData;
import log.charter.data.managers.ModeManager;

public class BaseUndoState extends UndoState {
	private final UndoState internalUndoState;
	private final int markerPosition;

	public BaseUndoState(final ModeManager modeManager, final ChartData data) {
		switch (modeManager.getMode()) {
		case GUITAR:
			internalUndoState = new GuitarModeUndoState(data);
			break;
		case TEMPO_MAP:
			internalUndoState = new TempoMapUndoState(data);
			break;
		case VOCALS:
			internalUndoState = new VocalUndoState(data);
			break;
		default:
			throw new RuntimeException("wrong edit mode " + modeManager.getMode());
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
