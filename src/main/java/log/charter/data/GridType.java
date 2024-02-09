package log.charter.data;

public enum GridType {
	MEASURE((b, n) -> b), //
	BEAT((b, n) -> 1), //
	NOTE((b, n) -> n);

	private static interface MultiplierGenerator {
		int generate(int timeSignatureBeats, int timeSignatureNoteDenominator);
	}

	private final MultiplierGenerator multiplierGenerator;

	private GridType(final MultiplierGenerator multiplierGenerator) {
		this.multiplierGenerator = multiplierGenerator;
	}

	public int getGridSizeMultiplier(final int timeSignatureBeats, final int timeSignatureNoteDenominator) {
		return multiplierGenerator.generate(timeSignatureBeats, timeSignatureNoteDenominator);
	}
}
