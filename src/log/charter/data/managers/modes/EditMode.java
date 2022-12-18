package log.charter.data.managers.modes;

public enum EditMode {
	GUITAR("Guitar"), //
	VOCALS("Vocals");

	public final String label;

	private EditMode(final String label) {
		this.label = label;
	}
}
