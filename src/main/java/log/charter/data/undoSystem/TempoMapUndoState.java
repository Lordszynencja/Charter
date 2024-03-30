package log.charter.data.undoSystem;

import static log.charter.util.CollectionUtils.map;

import java.util.List;

import log.charter.data.ChartData;
import log.charter.data.song.Arrangement;
import log.charter.data.song.Beat;
import log.charter.services.data.ChartTimeHandler;
import log.charter.util.collections.ArrayList2;

public class TempoMapUndoState extends UndoState {
	private final List<GuitarUndoState> guitarUndoStates;
	private final VocalUndoState vocalUndoState;
	private final List<Beat> beats;

	private TempoMapUndoState(final List<GuitarUndoState> guitarUndoStates, final VocalUndoState vocalUndoState,
			final List<Beat> beats) {
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

		beats = map(data.beats(), Beat::new);
	}

	@Override
	public TempoMapUndoState undo(final ChartData chartData, final ChartTimeHandler chartTimeHandler) {
		final List<GuitarUndoState> guitarRedoStates = map(guitarUndoStates,
				state -> state.undo(chartData, chartTimeHandler));
		final VocalUndoState vocalRedoState = vocalUndoState.undo(chartData, chartTimeHandler);
		final List<Beat> beatsRedo = map(chartData.beats(), Beat::new);
		chartData.songChart.beatsMap.beats = beats;

		return new TempoMapUndoState(guitarRedoStates, vocalRedoState, beatsRedo);
	}
}
