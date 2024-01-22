package log.charter.data.undoSystem;

import log.charter.data.ChartData;
import log.charter.song.ArrangementChart;
import log.charter.song.Beat;
import log.charter.util.CollectionUtils.ArrayList2;

public class TempoMapUndoState extends UndoState {
	private final ArrayList2<GuitarUndoState> guitarUndoStates;
	private final VocalUndoState vocalUndoState;
	private final ArrayList2<Beat> beats;

	private TempoMapUndoState(final ArrayList2<GuitarUndoState> guitarUndoStates, final VocalUndoState vocalUndoState,
			final ArrayList2<Beat> beats) {
		this.guitarUndoStates = guitarUndoStates;
		this.vocalUndoState = vocalUndoState;
		this.beats = beats;
	}

	public TempoMapUndoState(final ChartData data) {
		guitarUndoStates = new ArrayList2<>();
		for (int arrangementId = 0; arrangementId < data.songChart.arrangements.size(); arrangementId++) {
			final ArrangementChart arrangement = data.songChart.arrangements.get(arrangementId);
			for (final Integer levelId : arrangement.levels.keySet()) {
				guitarUndoStates.add(new GuitarUndoState(data, arrangementId, levelId));
			}
		}

		vocalUndoState = new VocalUndoState(data);

		beats = data.songChart.beatsMap.beats.map(Beat::new);
	}

	@Override
	public TempoMapUndoState undo(final ChartData data) {
		final ArrayList2<GuitarUndoState> guitarRedoStates = guitarUndoStates.map(state -> state.undo(data));
		final VocalUndoState vocalRedoState = vocalUndoState.undo(data);
		final ArrayList2<Beat> beatsRedo = data.songChart.beatsMap.beats.map(Beat::new);
		data.songChart.beatsMap.beats = beats;

		return new TempoMapUndoState(guitarRedoStates, vocalRedoState, beatsRedo);
	}
}
