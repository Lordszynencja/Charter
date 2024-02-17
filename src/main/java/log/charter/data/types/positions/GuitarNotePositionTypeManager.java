package log.charter.data.types.positions;

import log.charter.data.ChartData;
import log.charter.data.types.PositionWithIdAndType;
import log.charter.song.notes.ChordOrNote;
import log.charter.util.CollectionUtils.ArrayList2;

public class GuitarNotePositionTypeManager implements PositionTypeManager<ChordOrNote> {
	@Override
	public ArrayList2<ChordOrNote> getPositions(final ChartData data) {
		return data.getCurrentArrangementLevel().sounds;
	}

	@Override
	public ArrayList2<PositionWithIdAndType> getPositionsWithIdsAndTypes(final ChartData data) {
		return getPositions(data).mapWithId(PositionWithIdAndType::create);
	}
}
