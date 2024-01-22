package log.charter.io.gp.gp5;

public class GPMasterBar {
	public final int timeSignatureNumerator;
	public final int timeSignatureDenominator;
	public final boolean isRepeatStart;
	public final int repeatCount;
	public final int alternateEndings;
	public final KeySignature keySignature;
	public final KeySignatureType keySignatureType;
	public final TripletFeel tripletFeel;
	public final boolean isDoubleBar;

	public GPMasterBar(final int timeSignatureNumerator, final int timeSignatureDenominator,
			final boolean isRepeatStart, final int repeatCount, final int alternateEndings,
			final KeySignature keySignature, final KeySignatureType keySignatureType, final TripletFeel tripletFeel,
			final boolean isDoubleBar) {
		this.timeSignatureNumerator = timeSignatureNumerator;
		this.timeSignatureDenominator = timeSignatureDenominator;
		this.isRepeatStart = isRepeatStart;
		this.repeatCount = repeatCount;
		this.alternateEndings = alternateEndings;
		this.keySignature = keySignature;
		this.keySignatureType = keySignatureType;
		this.tripletFeel = tripletFeel;
		this.isDoubleBar = isDoubleBar;
	}

	public boolean isRepeatEnd() {
		return repeatCount > 0;
	}

	@Override
	public String toString() {
		return "GPMasterBar [timeSignatureNumerator=" + timeSignatureNumerator + ", timeSignatureDenominator="
				+ timeSignatureDenominator + ", isRepeatStart=" + isRepeatStart + ", repeatCount=" + repeatCount
				+ ", alternateEndings=" + alternateEndings + ", keySignature=" + keySignature + ", keySignatureType="
				+ keySignatureType + ", tripletFeel=" + tripletFeel + ", isDoubleBar=" + isDoubleBar + "]";
	}

}
