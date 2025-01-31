package log.charter.sound;

import static java.lang.Math.tan;

public class PassFilter {
	public enum PassType {
		Lowpass, //
		Highpass, //
		BAND_PASS;
	}

	private final float c;
	private final float a1;
	private final float a2;
	private final float a3;
	private final float b1;
	private final float b2;

	private final float[] inputHistory = new float[2];
	private final float[] outputHistory = new float[3];

	public PassFilter(final int sampleRate, final float frequency, final float resonance, final PassType passType) {
		switch (passType) {
			case Lowpass:
				c = 1.0f / (float) tan(Math.PI * frequency / sampleRate);
				a1 = 1.0f / (1.0f + resonance * c + c * c);
				a2 = 2f * a1;
				a3 = a1;
				b1 = 2.0f * (1.0f - c * c) * a1;
				b2 = (1.0f - resonance * c + c * c) * a1;
				break;
			case Highpass:
				c = (float) tan(Math.PI * frequency / sampleRate);
				a1 = 1.0f / (1.0f + resonance * c + c * c);
				a2 = -2f * a1;
				a3 = a1;
				b1 = 2.0f * (c * c - 1.0f) * a1;
				b2 = (1.0f - resonance * c + c * c) * a1;
				break;
			default:
				throw new IllegalArgumentException("Unknown pass filter type: " + passType);
		}
	}

	public float update(final float value) {
		final float newOutput = a1 * value//
				+ a2 * inputHistory[0]//
				+ a3 * inputHistory[1]//
				- b1 * outputHistory[0]//
				- b2 * outputHistory[1];

		inputHistory[1] = inputHistory[0];
		inputHistory[0] = value;

		outputHistory[2] = outputHistory[1];
		outputHistory[1] = outputHistory[0];
		outputHistory[0] = newOutput;

		return newOutput;
	}
}
