package log.charter.gui.components.tabs.errorsTab;

import log.charter.data.ChartData;
import log.charter.data.song.position.virtual.IVirtualConstantPosition;
import log.charter.services.data.ChartTimeHandler;
import log.charter.services.editModes.ModeManager;

public class ChartPositionOnLevel extends ChartPosition {
	private final int arrangementId;
	private final int levelId;
	private final IVirtualConstantPosition time;
	private final ChartTimeHandler chartTimeHandler;
	private final ModeManager modeManager;

	public ChartPositionOnLevel(final ChartData chartData, final int arrangementId, final int levelId,
			final IVirtualConstantPosition time, final ChartTimeHandler chartTimeHandler,
			final ModeManager modeManager) {
		super(getArrangementName(chartData, arrangementId) + ", " + getLevelName(levelId) + ": "
				+ getTimeText(chartData.beats(), time));

		this.arrangementId = arrangementId;
		this.levelId = levelId;
		this.time = time;
		this.chartTimeHandler = chartTimeHandler;
		this.modeManager = modeManager;
	}

	@Override
	public void goTo() {
		modeManager.setArrangement(arrangementId);
		modeManager.setLevel(levelId);
		chartTimeHandler.nextTime(time);
	}
}
