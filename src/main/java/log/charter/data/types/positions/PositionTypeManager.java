package log.charter.data.types.positions;

import log.charter.data.ChartData;
import log.charter.data.types.PositionWithIdAndType;
import log.charter.song.notes.IPosition;
import log.charter.util.CollectionUtils.ArrayList2;

public interface PositionTypeManager<T extends IPosition> {
	public ArrayList2<T> getPositions(final ChartData data);

	public ArrayList2<PositionWithIdAndType> getPositionsWithIdsAndTypes(final ChartData data);
}
