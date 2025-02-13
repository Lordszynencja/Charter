package log.charter.gui.components.tabs.errorsTab;

import log.charter.data.ChartData;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.position.virtual.IVirtualConstantPosition;
import log.charter.util.Utils;
import log.charter.util.Utils.TimeUnit;

public abstract class ChartPosition {
	protected static String getArrangementName(final ChartData chartData, final Integer arrangementId) {
		return chartData.songChart.arrangements.get(arrangementId).getTypeNameLabel(arrangementId);
	}

	protected static String getLevelName(final Integer levelId) {
		return "Level " + levelId;
	}

	protected static String getTimeText(final double time) {
		return Utils.formatTime((int) time, TimeUnit.MILISECONDS, TimeUnit.MILISECONDS, TimeUnit.HOURS);
	}

	protected static String getTimeText(final ImmutableBeatsMap beats, final IVirtualConstantPosition time) {
		return getTimeText(time.toPosition(beats).position());
	}

	public final String description;

	public ChartPosition(final String description) {
		this.description = description;
	}

	abstract public void goTo();
}
