package log.charter.gui.chartPanelDrawers.common.waveform;

import java.util.ArrayList;
import java.util.List;

import log.charter.sound.data.AudioData;
import log.charter.util.collections.Pair;

public class WaveformMap {
	private static final int levelDenominator = 2;

	public static int getSpanForLevel(final int level) {
		int timeSpan = 1;
		for (int i = 0; i < level; i++) {
			timeSpan *= levelDenominator;
		}

		return timeSpan;
	}

	private final List<List<WaveformInformation>> levels = new ArrayList<>();

	private List<WaveformInformation> generateNewLevel(final List<WaveformInformation> previousLevel) {
		final List<WaveformInformation> newLevel = new ArrayList<>(previousLevel.size() / levelDenominator + 1);
		WaveformInformation newInformation = null;
		int groupSize = 0;
		for (final WaveformInformation information : previousLevel) {
			if (groupSize == 0) {
				newInformation = information;
			} else {
				newInformation = newInformation.add(information);
			}

			groupSize++;

			if (groupSize == levelDenominator) {
				newLevel.add(newInformation);
				groupSize = 0;
			}
		}
		if (groupSize > 0) {
			newLevel.add(newInformation);
		}

		return newLevel;
	}

	public WaveformMap(final AudioData audio) {
		List<WaveformInformation> currentLevel = new WaveformInformationGenerator(audio).getLevel();
		levels.add(currentLevel);

		while (currentLevel.size() > 1) {
			final List<WaveformInformation> nextLevel = generateNewLevel(currentLevel);
			levels.add(nextLevel);
			currentLevel = nextLevel;
		}
	}

	public Pair<Integer, List<WaveformInformation>> getLevel(double pixelSpan) {
		int level = 0;
		while (level < levels.size() - 1 && pixelSpan >= 2) {
			level++;
			pixelSpan /= levelDenominator;
		}

		return new Pair<>(level, levels.get(level));
	}
}
