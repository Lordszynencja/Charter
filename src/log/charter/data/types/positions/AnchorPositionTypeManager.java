package log.charter.data.types.positions;

import log.charter.data.ChartData;
import log.charter.data.types.PositionWithIdAndType;
import log.charter.song.Anchor;
import log.charter.util.CollectionUtils.ArrayList2;

public class AnchorPositionTypeManager implements PositionTypeManager<Anchor> {
	@Override
	public ArrayList2<Anchor> getPositions(final ChartData data) {
		return data.getCurrentArrangementLevel().anchors;
	}

	@Override
	public ArrayList2<PositionWithIdAndType> getPositionsWithIdsAndTypes(final ChartData data) {
		return getPositions(data).mapWithId(PositionWithIdAndType::create);
	}
}
