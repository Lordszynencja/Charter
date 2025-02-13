package log.charter.gui.components.tabs.errorsTab;

import log.charter.services.data.ChartTimeHandler;
import log.charter.services.editModes.EditMode;
import log.charter.services.editModes.ModeManager;

public class ChartPositionOnTempoMap extends ChartPosition {
	private final double time;
	private final ChartTimeHandler chartTimeHandler;
	private final ModeManager modeManager;

	public ChartPositionOnTempoMap(final double time, final ChartTimeHandler chartTimeHandler,
			final ModeManager modeManager) {
		super(getTimeText(time));

		this.time = time;
		this.chartTimeHandler = chartTimeHandler;
		this.modeManager = modeManager;
	}

	@Override
	public void goTo() {
		modeManager.setMode(EditMode.TEMPO_MAP);
		chartTimeHandler.nextTime(time);
	}

}
