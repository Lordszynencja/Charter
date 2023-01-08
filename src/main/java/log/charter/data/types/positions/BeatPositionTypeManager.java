package log.charter.data.types.positions;

import log.charter.data.ChartData;
import log.charter.data.types.PositionWithIdAndType;
import log.charter.song.Beat;
import log.charter.util.CollectionUtils.ArrayList2;

public class BeatPositionTypeManager implements PositionTypeManager<Beat> {
	@Override
	public ArrayList2<Beat> getPositions(final ChartData data) {
		return data.songChart.beatsMap.beats;
	}

	@Override
	public ArrayList2<PositionWithIdAndType> getPositionsWithIdsAndTypes(final ChartData data) {
		return getPositions(data).mapWithId(PositionWithIdAndType::create);
	}
}
