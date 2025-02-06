package log.charter.io.gpa;

public class GpaSyncPoint {
	public final double trackTime;
	public final int bar;
	public final double positionInBar;
	public final double beatLength;

	public GpaSyncPoint(final double trackTime, final int bar, final double positionInBar, final double beatLength) {
		this.trackTime = trackTime;
		this.bar = bar;
		this.positionInBar = positionInBar;
		this.beatLength = beatLength;
	}
}
