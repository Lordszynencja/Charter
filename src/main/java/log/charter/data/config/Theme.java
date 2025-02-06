package log.charter.data.config;

import log.charter.data.config.Localization.Label;

public enum Theme {
	BASIC(Label.THEME_BASIC), //
	SQUARE(Label.THEME_SQUARE), //
	MODERN(Label.THEME_MODERN);

	public final Label label;

	private Theme(final Label label) {
		this.label = label;
	}
}
