package log.charter.data.undoSystem;

import log.charter.data.ChartData;
import log.charter.data.song.position.FractionalPosition;
import log.charter.services.data.ChartTimeHandler;
import log.charter.services.editModes.ModeManager;

public class BaseUndoState extends UndoState {
	private final UndoState internalUndoState;
	private final FractionalPosition markerPosition;

	public BaseUndoState(final ChartTimeHandler chartTimeHandler, final ModeManager modeManager, final ChartData data) {
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

		markerPosition = chartTimeHandler.timeFractional();
	}

	private BaseUndoState(final FractionalPosition markerPosition, final UndoState internalUndoState) {
		this.internalUndoState = internalUndoState;
		this.markerPosition = markerPosition;
	}

	@Override
	public UndoState undo(final ChartData data, final ChartTimeHandler chartTimeHandler) {
		final BaseUndoState redo = new BaseUndoState(markerPosition, internalUndoState.undo(data, chartTimeHandler));

		chartTimeHandler.nextTime(markerPosition);

		return redo;
	}
}
