package log.charter.data.types.positions;

import log.charter.data.ChartData;
import log.charter.data.managers.selection.ChordOrNote;
import log.charter.data.types.PositionWithIdAndType;
import log.charter.song.Level;
import log.charter.util.CollectionUtils.ArrayList2;

public class GuitarNotePositionTypeManager implements PositionTypeManager<ChordOrNote> {
	@Override
	public ArrayList2<ChordOrNote> getPositions(final ChartData data) {
		final Level currentLevel = data.getCurrentArrangementLevel();
		final ArrayList2<ChordOrNote> list = currentLevel.notes.map(ChordOrNote::new);
		list.addAll(currentLevel.chords.map(ChordOrNote::new));
		list.sort(null);

		return list;
	}

	@Override
	public ArrayList2<PositionWithIdAndType> getPositionsWithIdsAndTypes(final ChartData data) {
		return getPositions(data).mapWithId(PositionWithIdAndType::create);
	}
}
