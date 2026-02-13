package log.charter.gui.components.tabs.errorsTab;

import log.charter.data.config.Localization.Label;
import log.charter.gui.components.tabs.errorsTab.position.ChartPositionGenerator.ChartPosition;

public class ChartError {
	public enum ChartErrorSeverity {
		WARNING, ERROR;
	}

	public final String message;
	public final ChartErrorSeverity severity;
	public final ChartPosition position;

	public ChartError(final Label label, final ChartPosition position) {
		this(label.label(), position);
	}

	public ChartError(final Label label, final ChartErrorSeverity severity, final ChartPosition position) {
		this(label.label(), severity, position);
	}

	public ChartError(final String message, final ChartPosition position) {
		this(message, ChartErrorSeverity.ERROR, position);
	}

	public ChartError(final String message, final ChartErrorSeverity severity, final ChartPosition position) {
		this.message = message;
		this.severity = severity;
		this.position = position;
	}

}
