package log.charter.data.undoSystem;

import static log.charter.util.CollectionUtils.map;

import java.util.ArrayList;
import java.util.List;

import log.charter.data.ChartData;
import log.charter.data.song.Arrangement;
import log.charter.data.song.Beat;
import log.charter.services.data.ChartTimeHandler;

public class TempoMapUndoState extends UndoState {
	private final List<GuitarUndoState> guitarUndoStates;
	private final List<VocalUndoState> vocalUndoStates;
	private final List<Beat> beats;

	private TempoMapUndoState(final List<GuitarUndoState> guitarUndoStates, final List<VocalUndoState> vocalUndoStates,
			final List<Beat> beats) {
		this.guitarUndoStates = guitarUndoStates;
		this.vocalUndoStates = vocalUndoStates;
		this.beats = beats;
	}

	public TempoMapUndoState(final ChartData data) {
		guitarUndoStates = new ArrayList<>();
		for (int arrangementId = 0; arrangementId < data.songChart.arrangements.size(); arrangementId++) {
			final Arrangement arrangement = data.songChart.arrangements.get(arrangementId);
			for (int level = 0; level < arrangement.levels.size(); level++) {
				guitarUndoStates.add(new GuitarUndoState(data, arrangementId, level));
			}
		}

		vocalUndoStates = new ArrayList<>();
		for (int vocalPathId = 0; vocalPathId < data.songChart.vocalPaths.size(); vocalPathId++) {
			vocalUndoStates.add(new VocalUndoState(vocalPathId, data.songChart.vocalPaths.get(vocalPathId)));
		}

		beats = map(data.beats(), Beat::new);
	}

	@Override
	public TempoMapUndoState undo(final ChartData chartData, final ChartTimeHandler chartTimeHandler) {
		final List<GuitarUndoState> guitarRedoStates = map(guitarUndoStates,
				state -> state.undo(chartData, chartTimeHandler));
		final List<VocalUndoState> vocalRedoStates = map(vocalUndoStates,
				state -> state.undo(chartData, chartTimeHandler));
		final List<Beat> beatsRedo = map(chartData.beats(), Beat::new);
		chartData.songChart.beatsMap.beats = beats;

		return new TempoMapUndoState(guitarRedoStates, vocalRedoStates, beatsRedo);
	}
}
