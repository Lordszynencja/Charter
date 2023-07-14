package log.charter.io.gp.gp5;

import java.util.Arrays;

public class GPTrackData {
	public final String trackName;
	public final boolean isPercussion;
	public final int[] tuning;
	public final int fretCount;
	public final int capo;

	public GPTrackData(final String trackName, final boolean isPercussion, final int[] tuning, final int fretCount,
			final int capo) {
		this.trackName = trackName;
		this.isPercussion = isPercussion;
		this.tuning = tuning;
		this.fretCount = fretCount;
		this.capo = capo;
	}

	@Override
	public String toString() {
		return "GPTrackData [trackName=" + trackName + ", isPercussion=" + isPercussion + ", tuning="
				+ Arrays.toString(tuning) + ", fretCount=" + fretCount + ", capo=" + capo + "]";
	}

}
