package log.charter.gui;

import static log.charter.util.ScalingUtils.xToTime;

import log.charter.data.ChartData;
import log.charter.gui.PositionWithIdAndType.PositionType;
import log.charter.gui.handlers.ChartPanelMouseListener;

public class HighlightManager {
	private ChartData data;
	private ChartPanelMouseListener chartPanelMouseListener;
	private SelectionManager selectionManager;

	public void init(final ChartData data, final ChartPanelMouseListener chartPanelMouseListener,
			final SelectionManager selectionManager) {
		this.data = data;
		this.chartPanelMouseListener = chartPanelMouseListener;
		this.selectionManager = selectionManager;
	}

	public PositionWithIdAndType getHighlight() {
		final int x = chartPanelMouseListener.getMouseX();
		final int y = chartPanelMouseListener.getMouseY();
		final PositionWithIdAndType existingPosition = selectionManager.findExistingPosition(x, y);

		return existingPosition == null //
				? new PositionWithIdAndType(xToTime(x, data.time), PositionType.fromY(y, data))//
				: existingPosition;
	}
}
