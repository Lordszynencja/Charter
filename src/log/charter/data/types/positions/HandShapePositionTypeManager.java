package log.charter.data.types.positions;

import log.charter.data.ChartData;
import log.charter.data.types.PositionWithIdAndType;
import log.charter.song.HandShape;
import log.charter.util.CollectionUtils.ArrayList2;

public class HandShapePositionTypeManager implements PositionTypeManager<HandShape> {
	@Override
	public ArrayList2<HandShape> getPositions(final ChartData data) {
		return data.getCurrentArrangementLevel().handShapes;
	}

	@Override
	public ArrayList2<PositionWithIdAndType> getPositionsWithIdsAndTypes(final ChartData data) {
		return getPositions(data).mapWithId(PositionWithIdAndType::create);
	}
}
