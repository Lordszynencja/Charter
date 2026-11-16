package log.charter.data.config;

import log.charter.data.config.Localization.Label;

public enum Quality {
	LOW(Label.QUALITY_LOW), //
	HIGH(Label.QUALITY_HIGH);

	public final Label label;

	private Quality(final Label label) {
		this.label = label;
	}
}
