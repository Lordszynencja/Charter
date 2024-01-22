package log.charter.io.gp.gp5;

public enum KeySignatureType {
	Major, Minor;

	public static KeySignatureType fromValue(final int value) {
		return value == 0 ? Major : Minor;
	}
}