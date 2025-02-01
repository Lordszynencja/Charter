package log.charter.sound.effects.pass;

public enum PassFilterAlgorithm {
	BESSEL("Bessel"), //
	BUTTERWORTH("Butterworth"), //
	CHEBYSHEV_I("Chebyshev I"), //
	CHEBYSHEV_II("Chebyshev II");

	public final String label;

	private PassFilterAlgorithm(final String label) {
		this.label = label;
	}
}
