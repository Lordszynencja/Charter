package log.charter.data;

public enum EditMode {
	VOCALS("Vocals"), GUITAR("Guitar");

	public final String name;

	private EditMode(final String name) {
		this.name = name;
	}
}
