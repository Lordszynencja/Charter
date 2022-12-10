package log.charter.gui.modes;

import java.util.List;

import log.charter.gui.ChartEventsHandler;
import log.charter.io.rs.xml.vocals.Vocal;

public class VocalModeHandler implements ModeHandler {

	private final ChartEventsHandler chartEventsHandler;

	public VocalModeHandler(final ChartEventsHandler chartEventsHandler) {
		this.chartEventsHandler = chartEventsHandler;
	}

	@Override
	public void handleHome() {
		if (!chartEventsHandler.isCtrl()) {
			chartEventsHandler.setNextTime(0);
			return;
		}

		final List<Vocal> vocals = chartEventsHandler.data.songChart.vocals.vocals;
		chartEventsHandler.setNextTime(vocals.isEmpty() ? 0 : vocals.get(0).time);
	}
}
