package log.charter.io.gp;

public class GPFileImportOptions {
	public final boolean importTempoMap;
	public final boolean generateFHP;
	public final int slideInSize;
	public final int slideOutSize;

	public GPFileImportOptions(final boolean importTempoMap, final boolean generateFHP, final int slideInSize,
			final int slideOutSize) {
		this.importTempoMap = importTempoMap;
		this.generateFHP = generateFHP;
		this.slideInSize = slideInSize;
		this.slideOutSize = slideOutSize;
	}
}
