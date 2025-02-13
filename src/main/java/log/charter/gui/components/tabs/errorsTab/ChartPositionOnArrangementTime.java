package log.charter.gui.components.tabs.errorsTab;

import log.charter.data.ChartData;
import log.charter.data.song.position.virtual.IVirtualConstantPosition;
import log.charter.services.data.ChartTimeHandler;
import log.charter.services.editModes.ModeManager;

public class ChartPositionOnArrangementTime extends ChartPosition {
	private final int arrangementId;
	private final IVirtualConstantPosition time;
	private final ChartTimeHandler chartTimeHandler;
	private final ModeManager modeManager;

	public ChartPositionOnArrangementTime(final ChartData chartData, final int arrangementId,
			final IVirtualConstantPosition time, final ChartTimeHandler chartTimeHandler,
			final ModeManager modeManager) {
		super(getArrangementName(chartData, arrangementId) + ": " + getTimeText(chartData.beats(), time));

		this.arrangementId = arrangementId;
		this.time = time;
		this.chartTimeHandler = chartTimeHandler;
		this.modeManager = modeManager;
	}

	@Override
	public void goTo() {
		modeManager.setArrangement(arrangementId);
		chartTimeHandler.nextTime(time);
	}
}
