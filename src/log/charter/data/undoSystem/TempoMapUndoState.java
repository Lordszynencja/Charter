package log.charter.data.undoSystem;

import log.charter.data.ChartData;
import log.charter.song.ArrangementChart;
import log.charter.util.CollectionUtils.ArrayList2;

public class TempoMapUndoState implements UndoState {
	private final BeatsMapUndoState beatsMapUndoState;

	private final ArrayList2<GuitarUndoState> guitarUndoStates;
	private final VocalUndoState vocalUndoState;

	private TempoMapUndoState(final BeatsMapUndoState beatsMapUndoState,
			final ArrayList2<GuitarUndoState> guitarUndoStates, final VocalUndoState vocalUndoState) {
		this.beatsMapUndoState = beatsMapUndoState;
		this.guitarUndoStates = guitarUndoStates;
		this.vocalUndoState = vocalUndoState;
	}

	public TempoMapUndoState(final ChartData data) {
		beatsMapUndoState = new BeatsMapUndoState(data);

		guitarUndoStates = new ArrayList2<>();
		for (int arrangementId = 0; arrangementId < data.songChart.arrangements.size(); arrangementId++) {
			final ArrangementChart arrangement = data.songChart.arrangements.get(arrangementId);
			for (final Integer levelId : arrangement.levels.keySet()) {
				guitarUndoStates.add(new GuitarUndoState(data, arrangementId, levelId));
			}
		}

		vocalUndoState = new VocalUndoState(data);
	}

	@Override
	public TempoMapUndoState undo(final ChartData data) {
		final BeatsMapUndoState beatsMapRedoState = beatsMapUndoState.undo(data);

		final ArrayList2<GuitarUndoState> guitarRedoStates = guitarUndoStates.map(state -> state.undo(data));
		final VocalUndoState vocalRedoState = vocalUndoState.undo(data);

		return new TempoMapUndoState(beatsMapRedoState, guitarRedoStates, vocalRedoState);
	}
}
