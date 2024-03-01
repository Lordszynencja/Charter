package log.charter.data.undoSystem;

import log.charter.data.ChartData;
import log.charter.gui.handlers.data.ChartTimeHandler;
import log.charter.song.Arrangement;
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
			final Arrangement arrangement = data.songChart.arrangements.get(arrangementId);
			for (int level = 0; level < arrangement.levels.size(); level++) {
				guitarUndoStates.add(new GuitarUndoState(data, arrangementId, level));
			}
		}

		vocalUndoState = new VocalUndoState(data);

		beats = data.songChart.beatsMap.beats.map(Beat::new);
	}

	@Override
	public TempoMapUndoState undo(final ChartData data, final ChartTimeHandler chartTimeHandler) {
		final ArrayList2<GuitarUndoState> guitarRedoStates = guitarUndoStates
				.map(state -> state.undo(data, chartTimeHandler));
		final VocalUndoState vocalRedoState = vocalUndoState.undo(data, chartTimeHandler);
		final ArrayList2<Beat> beatsRedo = data.songChart.beatsMap.beats.map(Beat::new);
		data.songChart.beatsMap.beats = beats;

		return new TempoMapUndoState(guitarRedoStates, vocalRedoState, beatsRedo);
	}
}
