package log.charter.util.fft;

public class FFTTest {
	private static final int bufferSize = 4096;

	private final float[] buffer = new float[bufferSize];
	private int bufferPosition = 0;

	private final NoteExtractor noteExtractor;
	private final Complex[] output;
	public double[] magnitudes = new double[1];

	public FFTTest() {
		noteExtractor = new NoteExtractor(bufferSize, 44100);
		output = noteExtractor.allocOutput();
	}

	public void addData(final float[] data) throws InterruptedException {
		for (final float f : data) {
			buffer[bufferPosition++] = f;
			bufferPosition = bufferPosition % bufferSize;
		}

		final float[] tempBuffer = new float[bufferSize];
		for (int i = 0; i < bufferSize; i++) {
			tempBuffer[i] = buffer[(bufferPosition + i) % bufferSize];
		}

		noteExtractor.execute(tempBuffer, output);

		final double[] newMagnitudes = new double[output.length];
		for (int i = 0; i < output.length; i++) {
			newMagnitudes[i] = Math.sqrt(output[i].real * output[i].real + output[i].imag * output[i].imag);
		}
		magnitudes = newMagnitudes;
	}
}
