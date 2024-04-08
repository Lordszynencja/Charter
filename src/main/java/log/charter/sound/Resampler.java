package log.charter.sound;

import log.charter.util.data.Fraction;

public class Resampler implements IResampler {
	public static IResampler create(final int sampleRateFrom, final int sampleRateTo, final SampleSink sink) {
		if (sampleRateFrom == sampleRateTo) {
			return f -> sink.consume(f);
		}

		return new Resampler(sampleRateFrom, sampleRateTo, sink);
	}

	public static interface SampleSink {
		void consume(float f) throws Exception;
	}

	private float a = 0;
	private float b = 0;
	private float c = 0;

	private final Fraction distanceBetweenNewSamples;
	private Fraction position = new Fraction(0, 1);

	private final SampleSink sink;

	private Resampler(final int sampleRateFrom, final int sampleRateTo, final SampleSink sink) {
		distanceBetweenNewSamples = new Fraction(sampleRateFrom, sampleRateTo);

		this.sink = sink;
	}

	@Override
	public void addSample(final float sample) throws Exception {
		a = b;
		b = c;
		c = sample;

		while (position.numerator < position.denominator) {
			final float x = position.floatValue();
			final float l0 = x * (x - 1) / 2;
			final float l1 = -(x + 1) * (x - 1);
			final float l2 = (x + 1) * x / 2;

			final float y = a * l0 + b * l1 + c * l2;
			sink.consume(y);
			position = position.add(distanceBetweenNewSamples);
		}

		position = position.add(new Fraction(-1, 1));
	}
}
