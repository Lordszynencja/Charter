package log.charter.data.types.positions;

import log.charter.data.ChartData;
import log.charter.data.types.PositionWithIdAndType;
import log.charter.song.vocals.Vocal;
import log.charter.util.CollectionUtils.ArrayList2;

public class VocalPositionTypeManager implements PositionTypeManager<Vocal> {
	@Override
	public ArrayList2<Vocal> getPositions(final ChartData data) {
		return data.songChart.vocals.vocals;
	}

	@Override
	public ArrayList2<PositionWithIdAndType> getPositionsWithIdsAndTypes(final ChartData data) {
		return getPositions(data).mapWithId(PositionWithIdAndType::create);
	}
}
