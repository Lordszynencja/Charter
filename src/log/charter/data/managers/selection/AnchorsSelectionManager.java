package log.charter.data.managers.selection;

import log.charter.data.ChartData;
import log.charter.data.PositionWithIdAndType.PositionType;
import log.charter.song.Anchor;
import log.charter.util.CollectionUtils.ArrayList2;

class AnchorsSelectionManager extends SingleTypeSelectionManager<Anchor> {
	private final ChartData data;

	AnchorsSelectionManager(final ChartData data) {
		super(Selection::new);
		this.data = data;
	}

	@Override
	protected ArrayList2<Anchor> getAvailable() {
		return PositionType.ANCHOR.getPositions(data);
	}
}
