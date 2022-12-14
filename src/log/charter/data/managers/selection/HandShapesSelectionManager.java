package log.charter.data.managers.selection;

import log.charter.data.ChartData;
import log.charter.data.PositionWithIdAndType.PositionType;
import log.charter.song.HandShape;
import log.charter.util.CollectionUtils.ArrayList2;

class HandShapesSelectionManager extends SingleTypeSelectionManager<HandShape> {
	private final ChartData data;

	HandShapesSelectionManager(final ChartData data) {
		super(Selection::new);
		this.data = data;
	}

	@Override
	protected ArrayList2<HandShape> getAvailable() {
		return PositionType.HAND_SHAPE.getPositions(data);
	}
}
