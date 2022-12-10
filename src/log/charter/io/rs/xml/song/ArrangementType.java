package log.charter.io.rs.xml.song;

public enum ArrangementType {
	Combo(6), Rhythm(6), Lead(6), Bass(4);

	public final int strings;

	private ArrangementType(final int strings) {
		this.strings = strings;
	}
}
