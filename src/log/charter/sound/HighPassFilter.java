package log.charter.sound;

import static java.lang.Math.tan;

public class HighPassFilter {
	public enum PassType {
		Lowpass, //
		Highpass;
	}

	public float value;

	private float c, a1, a2, a3, b1, b2;

	private final float[] inputHistory = new float[2];
	private final float[] outputHistory = new float[3];

	public HighPassFilter(final float frequency, final int sampleRate, final PassType passType, final float resonance) {
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
		}
	}

	public float update(final float newInput) {
		final float newOutput = a1 * newInput//
				+ a2 * inputHistory[0]//
				+ a3 * inputHistory[1]//
				- b1 * outputHistory[0]//
				- b2 * outputHistory[1];

		inputHistory[1] = inputHistory[0];
		inputHistory[0] = newInput;

		outputHistory[2] = outputHistory[1];
		outputHistory[1] = outputHistory[0];
		outputHistory[0] = newOutput;

		return newOutput;
	}
}
