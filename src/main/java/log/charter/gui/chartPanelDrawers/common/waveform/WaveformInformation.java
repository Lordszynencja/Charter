package log.charter.gui.chartPanelDrawers.common.waveform;

public class WaveformInformation {
	public final float height;
	public final boolean rms;

	public WaveformInformation(final float height, final boolean rms) {
		this.height = height;
		this.rms = rms;
	}

	public WaveformInformation add(final WaveformInformation other) {
		return new WaveformInformation(Math.max(height, other.height), rms || other.rms);
	}
}
