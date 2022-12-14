package log.charter.data.managers.selection;

import log.charter.data.ChartData;
import log.charter.data.PositionWithIdAndType.PositionType;
import log.charter.song.Vocal;
import log.charter.util.CollectionUtils.ArrayList2;

class VocalsSelectionManager extends SingleTypeSelectionManager<Vocal> {
	private final ChartData data;

	VocalsSelectionManager(final ChartData data) {
		super(Selection::new);
		this.data = data;
	}

	@Override
	protected ArrayList2<Vocal> getAvailable() {
		return PositionType.VOCAL.getPositions(data);
	}
}
