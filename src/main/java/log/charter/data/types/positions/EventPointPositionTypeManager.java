package log.charter.data.types.positions;

import log.charter.data.ChartData;
import log.charter.data.types.PositionWithIdAndType;
import log.charter.song.EventPoint;
import log.charter.util.CollectionUtils.ArrayList2;

public class EventPointPositionTypeManager implements PositionTypeManager<EventPoint> {
	@Override
	public ArrayList2<EventPoint> getPositions(final ChartData data) {
		return data.getCurrentArrangement().eventPoints;
	}

	@Override
	public ArrayList2<PositionWithIdAndType> getPositionsWithIdsAndTypes(final ChartData data) {
		return getPositions(data).mapWithId(PositionWithIdAndType::create);
	}
}
