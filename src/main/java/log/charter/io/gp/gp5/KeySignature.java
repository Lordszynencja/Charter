package log.charter.io.gp.gp5;

public enum KeySignature {
	Cb(-7), //
	Gb(-6), //
	Db(-5), //
	Ab(-4), //
	Eb(-3), //
	Bb(-2), //
	F(-1), //
	C(0), //
	G(1), //
	D(2), //
	A(3), //
	E(4), //
	B(5), //
	FSharp(6), //
	CSharp(7);

	public static KeySignature fromValue(final int value) {
		for (final KeySignature keySignature : values()) {
			if (keySignature.value == value) {
				return keySignature;
			}
		}

		return null;
	}

	public final int value;

	private KeySignature(final int value) {
		this.value = value;
	}
}