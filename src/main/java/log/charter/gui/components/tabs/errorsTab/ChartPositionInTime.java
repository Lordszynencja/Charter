package log.charter.gui.components.tabs.errorsTab;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.position.virtual.IVirtualConstantPosition;
import log.charter.services.data.ChartTimeHandler;

public class ChartPositionInTime extends ChartPosition {
	private final IVirtualConstantPosition time;
	private final ChartTimeHandler chartTimeHandler;

	public ChartPositionInTime(final ImmutableBeatsMap beats, final IVirtualConstantPosition time,
			final ChartTimeHandler chartTimeHandler) {
		super(getTimeText(beats, time));

		this.time = time;
		this.chartTimeHandler = chartTimeHandler;
	}

	@Override
	public void goTo() {
		chartTimeHandler.nextTime(time);
	}

}
