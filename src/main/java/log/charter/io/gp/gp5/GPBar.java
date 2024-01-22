package log.charter.io.gp.gp5;

import java.util.List;

public class GPBar {
	public final List<List<GPBeat>> voices;

	public GPBar(final List<List<GPBeat>> voices) {
		this.voices = voices;
	}

	@Override
	public String toString() {
		return "GPBar [voices=" + voices + "]";
	}
}
