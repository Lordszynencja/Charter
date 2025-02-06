package log.charter.sound;

public class Resampler implements IResampler {
	public static IResampler create(final float resampleRatio, final SampleSink sink) {
		if (resampleRatio <= 0) {
			throw new IllegalArgumentException("resample ratio must be positive");
		}
		if (resampleRatio == 1) {
			return f -> sink.consume(f);
		}

		return new Resampler(resampleRatio, sink);
	}

	public static interface SampleSink {
		void consume(float f);
	}

	private float a = 0;
	private float b = 0;
	private float c = 0;

	private final float distanceBetweenNewSamples;
	private float position = 0;

	private final SampleSink sink;

	private Resampler(final float resampleRatio, final SampleSink sink) {
		distanceBetweenNewSamples = resampleRatio;
		this.sink = sink;
	}

	@Override
	public void addSample(final float sample) {
		a = b;
		b = c;
		c = sample;

		while (position < 1) {
			final float l0 = position * (position - 1) / 2;
			final float l1 = -(position + 1) * (position - 1);
			final float l2 = (position + 1) * position / 2;

			final float y = a * l0 + b * l1 + c * l2;
			sink.consume(y);
			position += distanceBetweenNewSamples;
		}

		position -= 1;
	}
}
