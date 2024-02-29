package log.charter.gui.chartPanelDrawers.common.waveform;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sqrt;

public class RMSCalculator {
	private int counter = 0;
	private final float[] values;

	public RMSCalculator(final int[] musicValues, final int size, final int start) {
		values = new float[size];

		for (int i = 0; i < size; i++) {
			final int position = max(0, min(musicValues.length - 1, start - size + i));
			addValue(musicValues[position]);
		}
	}

	public RMSCalculator(final int size) {
		values = new float[size];
	}

	public void addValue(final float val) {
		values[counter] = val * val;
		counter = (counter + 1) % values.length;
	}

	public double getRMS() {
		float sum = 0;
		for (final float value : values) {
			sum += value;
		}

		return sqrt(sum);
	}
}