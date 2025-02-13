package log.charter.gui.components.tabs.errorsTab;

import log.charter.data.ChartData;
import log.charter.services.editModes.ModeManager;

public class ChartPositionOnArrangement extends ChartPosition {
	private final int arrangementId;
	private final ModeManager modeManager;

	public ChartPositionOnArrangement(final ChartData chartData, final int arrangementId,
			final ModeManager modeManager) {
		super(getArrangementName(chartData, arrangementId));

		this.arrangementId = arrangementId;
		this.modeManager = modeManager;
	}

	@Override
	public void goTo() {
		modeManager.setArrangement(arrangementId);
	}
}
