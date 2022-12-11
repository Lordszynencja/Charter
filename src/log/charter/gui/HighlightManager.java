package log.charter.gui;

import static log.charter.util.ScalingUtils.xToTime;

import log.charter.data.ChartData;
import log.charter.gui.PositionWithIdAndType.PositionType;

public class HighlightManager {
	private ChartData data;
	private SelectionManager selectionManager;

	public void init(final ChartData data, final SelectionManager selectionManager) {
		this.data = data;
		this.selectionManager = selectionManager;
	}

	public PositionWithIdAndType getHighlight() {
		final PositionWithIdAndType existingPosition = selectionManager.findExistingPosition(data.mx, data.my);
		return existingPosition == null //
				? new PositionWithIdAndType(xToTime(data.mx, data.time), PositionType.fromY(data.my, data))//
				: existingPosition;
	}
}
