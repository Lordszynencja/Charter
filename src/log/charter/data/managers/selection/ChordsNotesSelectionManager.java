package log.charter.data.managers.selection;

import log.charter.data.ChartData;
import log.charter.data.PositionWithIdAndType.PositionType;
import log.charter.util.CollectionUtils.ArrayList2;

class ChordsNotesSelectionManager extends SingleTypeSelectionManager<ChordOrNote> {
	private final ChartData data;

	ChordsNotesSelectionManager(final ChartData data) {
		super(Selection::new);
		this.data = data;
	}

	@Override
	protected ArrayList2<ChordOrNote> getAvailable() {
		return PositionType.GUITAR_NOTE.getPositions(data);
	}
}
