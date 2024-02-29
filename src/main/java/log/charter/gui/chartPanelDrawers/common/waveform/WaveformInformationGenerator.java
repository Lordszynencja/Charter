package log.charter.gui.chartPanelDrawers.common.waveform;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.List;

import log.charter.sound.data.MusicDataInt;

public class WaveformInformationGenerator {
	private List<WaveformInformation> level = null;
	private final float rate;

	public WaveformInformationGenerator(final float rate) {
		this.rate = rate;
	}

	private float getValue(final int sample) {
		if (sample >= 0) {
			return (float) sample / MusicDataInt.maxValue;
		}

		return (float) sample / MusicDataInt.minValue;
	}

	private float getValue(final int[][] musicValues, final int position) {
		float value = 0;
		for (int i = 0; i < musicValues.length; i++) {
			value = max(value, getValue(musicValues[i][position]));
		}
		return value;
	}

	private void addFrame(final int[][] musicValues, final int t) {
		final int start = (int) (t * rate / 1000);
		final int end = min(musicValues.length, (int) ((t + 1) * rate / 1000));

		float height = 0;
		final RMSCalculator rmsCalculator = new RMSCalculator(end - start);
		for (int i = start; i < end; i++) {
			final float value = getValue(musicValues, i);
			height = Math.max(height, value);
			rmsCalculator.addValue(value);
		}

		level.add(new WaveformInformation(height, rmsCalculator.getRMS() > 4));
	}

	public List<WaveformInformation> getLevel(final int audioLength, final int[][] musicValues) {
		if (level != null) {
			return level;
		}

		level = new ArrayList<>(audioLength);

		for (int t = 0; t < audioLength; t++) {
			addFrame(musicValues, t);
		}

		return level;
	}
}
