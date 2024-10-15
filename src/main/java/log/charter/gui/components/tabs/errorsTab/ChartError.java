package log.charter.gui.components.tabs.errorsTab;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.position.virtual.IVirtualConstantPosition;
import log.charter.services.data.ChartTimeHandler;
import log.charter.util.Utils;
import log.charter.util.Utils.TimeUnit;

public class ChartError {
	public enum ChartErrorSeverity {
		WARNING, ERROR;
	}

	public static class ChartPosition {
		private static String getArrangementName(final ChartData chartData, final Integer arrangementId) {
			return chartData.songChart.arrangements.get(arrangementId).getTypeNameLabel(arrangementId);
		}

		private static String getLevelName(final Integer levelId) {
			return "Level " + levelId;
		}

		private static String getTimeText(final ImmutableBeatsMap beats, final IVirtualConstantPosition time) {
			return Utils.formatTime(time.toPosition(beats).position(), TimeUnit.MILISECONDS, TimeUnit.MILISECONDS,
					TimeUnit.HOURS);
		}

		private static String getName(final ChartData chartData, final Integer arrangementId, final Integer levelId,
				final IVirtualConstantPosition time) {
			String name = "Chart";
			if (arrangementId != null) {
				name = getArrangementName(chartData, arrangementId);
			}
			if (levelId != null) {
				name += ", " + getLevelName(levelId);
			}
			if (time != null) {
				name += ": " + getTimeText(chartData.beats(), time);
			}

			return name;
		}

		public final String description;

		private final Integer arrangementId;
		private final Integer levelId;
		private final IVirtualConstantPosition time;

		public ChartPosition(final ChartData chartData, final Integer arrangementId) {
			this(chartData, arrangementId, null, null);
		}

		public ChartPosition(final ChartData chartData, final Integer arrangementId,
				final IVirtualConstantPosition time) {
			this(chartData, arrangementId, null, time);
		}

		public ChartPosition(final ChartData chartData, final Integer arrangementId, final Integer levelId,
				final IVirtualConstantPosition time) {
			description = getName(chartData, arrangementId, levelId, time);
			this.arrangementId = arrangementId;
			this.levelId = levelId;
			this.time = time;
		}

		public void goTo(final ChartTimeHandler chartTimeHandler) {
			chartTimeHandler.moveTo(arrangementId, levelId, time);
		}
	}

	public final String message;
	public final ChartErrorSeverity severity;
	public final ChartPosition position;

	public ChartError(final Label label, final ChartErrorSeverity severity, final ChartPosition position) {
		this(label.label(), severity, position);
	}

	public ChartError(final String message, final ChartErrorSeverity severity, final ChartPosition position) {
		this.message = message;
		this.severity = severity;
		this.position = position;
	}
}
