package log.charter.gui.components.tabs.errorsTab;

import log.charter.data.config.Localization.Label;

public class ChartError {
	public enum ChartErrorSeverity {
		WARNING, ERROR;
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
