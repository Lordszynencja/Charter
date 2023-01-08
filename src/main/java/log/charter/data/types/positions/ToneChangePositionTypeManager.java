package log.charter.data.types.positions;

import log.charter.data.ChartData;
import log.charter.data.types.PositionWithIdAndType;
import log.charter.song.ToneChange;
import log.charter.util.CollectionUtils.ArrayList2;

public class ToneChangePositionTypeManager implements PositionTypeManager<ToneChange> {
	@Override
	public ArrayList2<ToneChange> getPositions(final ChartData data) {
		return data.getCurrentArrangement().toneChanges;
	}

	@Override
	public ArrayList2<PositionWithIdAndType> getPositionsWithIdsAndTypes(final ChartData data) {
		return getPositions(data).mapWithId(PositionWithIdAndType::create);
	}
}
