package log.charter.data.managers.selection;

import log.charter.data.ChartData;
import log.charter.data.PositionWithIdAndType.PositionType;
import log.charter.song.Beat;
import log.charter.util.CollectionUtils.ArrayList2;

class BeatsSelectionManager extends SingleTypeSelectionManager<Beat> {
	private final ChartData data;

	BeatsSelectionManager(final ChartData data) {
		super(Selection::new);
		this.data = data;
	}

	@Override
	protected ArrayList2<Beat> getAvailable() {
		return PositionType.BEAT.getPositions(data);
	}
}
