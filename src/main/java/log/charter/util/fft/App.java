package log.charter.util.fft;

import java.io.IOException;

public class App {
	public static void main(final String[] args) throws IOException {
		// Sampling rate
		final int sampleRate = 44100;

		// Frequency of the cosine wave in Hz
		final float frequency = 440;

		// Length of the cosine wave in samples
		final int bufferSize = 4096;

		final float[] input = new float[bufferSize];

		// Generate the cosine wave
		for (int i = 0; i < input.length; ++i) {
			final float t = i / (float) sampleRate;
			input[i] = (float) Math.cos(2 * Math.PI * frequency * t);
		}

		final NoteExtractor noteExtractor = new NoteExtractor(bufferSize, sampleRate);

		final float[] frequencies = noteExtractor.frequencies();
		final Complex[] output = noteExtractor.allocOutput();

		noteExtractor.execute(input, output);

		for (int bin = 0; bin < output.length; ++bin) {
			final float mag = (float) Math
					.sqrt(output[bin].real * output[bin].real + output[bin].imag * output[bin].imag);
			// Logger.debug.println("BIN: " + bin + " Frequency: " + frequencies[bin] + ": "
			// +
			// mag);
		}

		noteExtractor.close();
	}
}
