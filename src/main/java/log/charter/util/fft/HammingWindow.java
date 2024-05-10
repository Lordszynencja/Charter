package log.charter.util.fft;

public class HammingWindow {

	// Alpha always equals 25/46 for a Hamming window
	// Why? I don't know. Ask the guy who invented it.
	private static final float alpha = 25 / 46f;

	public static float[] generate(final int length) {
		final int N = length;

		final float[] W = new float[N];
		for (int n = 0; n < N; ++n) {
			W[n] = alpha + (1 - alpha) * (float) Math.cos(2 * Math.PI * n / (float) N);
		}

		return W;
	}
}
