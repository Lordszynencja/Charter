package log.charter.gui.chartPanelDrawers.common.waveform;

import java.util.ArrayList;
import java.util.List;

import log.charter.sound.data.AudioDataShort;
import log.charter.util.CollectionUtils.Pair;

public class WaveformMap {
	private final List<List<WaveformInformation>> levels = new ArrayList<>();

	public WaveformMap(final AudioDataShort audio) {
		levels.add(new WaveformInformationGenerator(audio).getLevel());
	}

	public Pair<Integer, List<WaveformInformation>> getLevel(double pixelSpan) {
		int level = 0;
		while (level < levels.size() - 1 && pixelSpan > 1) {
			level++;
			pixelSpan /= 2;
		}

		return new Pair<>(level, levels.get(level));
	}
}
