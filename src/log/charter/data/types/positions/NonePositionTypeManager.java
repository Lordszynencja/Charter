package log.charter.data.types.positions;

import log.charter.data.ChartData;
import log.charter.data.types.PositionWithIdAndType;
import log.charter.song.Position;
import log.charter.util.CollectionUtils.ArrayList2;

public class NonePositionTypeManager implements PositionTypeManager<Position> {
	@Override
	public ArrayList2<Position> getPositions(final ChartData data) {
		return new ArrayList2<>();
	}

	@Override
	public ArrayList2<PositionWithIdAndType> getPositionsWithIdsAndTypes(final ChartData data) {
		return new ArrayList2<>();
	}
}
