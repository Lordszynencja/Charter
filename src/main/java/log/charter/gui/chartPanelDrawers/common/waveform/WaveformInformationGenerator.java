package log.charter.gui.chartPanelDrawers.common.waveform;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.List;

import log.charter.sound.data.AudioDataShort;

public class WaveformInformationGenerator {
	private List<WaveformInformation> level = null;
	private final AudioDataShort audio;

	public WaveformInformationGenerator(final AudioDataShort audio) {
		this.audio = audio;
	}

	private float getValue(final short sample) {
		if (sample >= 0) {
			return (float) sample / AudioDataShort.maxValue;
		}

		return (float) sample / AudioDataShort.minValue;
	}

	private float getValue(final int position) {
		float value = 0;
		for (int i = 0; i < audio.data.length; i++) {
			value = max(value, getValue(audio.data[i][position]));
		}
		return value;
	}

	private void addFrame(final int t) {
		final int start = (int) (t * audio.sampleRate() / 1000);
		final int end = min(audio.data[0].length, (int) ((t + 1) * audio.sampleRate() / 1000));

		float height = 0;
		final RMSCalculator rmsCalculator = new RMSCalculator(end - start);
		for (int i = start; i < end; i++) {
			final float value = getValue(i);
			height = Math.max(height, value);
			rmsCalculator.addValue(value);
		}

		level.add(new WaveformInformation(height, rmsCalculator.getRMS() > 4));
	}

	public List<WaveformInformation> getLevel() {
		if (level != null) {
			return level;
		}

		final int length = audio.msLength();
		level = new ArrayList<>(length);

		for (int t = 0; t < length; t++) {
			addFrame(t);
		}

		return level;
	}
}
