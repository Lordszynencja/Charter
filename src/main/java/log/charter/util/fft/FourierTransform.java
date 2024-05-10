package log.charter.util.fft;

import static org.bytedeco.fftw.global.fftw3.FFTW_ESTIMATE;
import static org.bytedeco.fftw.global.fftw3.fftwf_alloc_complex;
import static org.bytedeco.fftw.global.fftw3.fftwf_alloc_real;
import static org.bytedeco.fftw.global.fftw3.fftwf_destroy_plan;
import static org.bytedeco.fftw.global.fftw3.fftwf_execute;
import static org.bytedeco.fftw.global.fftw3.fftwf_free;
import static org.bytedeco.fftw.global.fftw3.fftwf_plan_dft_r2c_1d;

import org.bytedeco.fftw.global.fftw3.fftwf_plan;
import org.bytedeco.javacpp.FloatPointer;

public class FourierTransform {
	private static final int REAL = 0;
	private static final int IMAG = 1;

	private final int bufferSize;
	private final FloatPointer input;
	private final FloatPointer output;
	private final fftwf_plan plan;

	// Temporary buffer used to translate the output into Complex values
	private final float[] outputBuffer;

	// Performs FFT of real input data
	public FourierTransform(final int bufferSize) {
		this.bufferSize = bufferSize;
		input = fftwf_alloc_real(bufferSize);
		output = fftwf_alloc_complex(bufferSize / 2 + 1);
		outputBuffer = new float[(bufferSize / 2 + 1) * 2];
		plan = fftwf_plan_dft_r2c_1d(bufferSize, input, output, FFTW_ESTIMATE);
	}

	public float[] allocInput() {
		return new float[bufferSize];
	}

	public Complex[] allocOutput() {
		final Complex[] output = new Complex[bufferSize / 2 + 1];
		for (int i = 0; i < output.length; i++) {
			output[i] = new Complex();
		}
		return output;
	}

	public void execute(final float[] input, final Complex[] output) {
		this.input.put(input);
		fftwf_execute(plan);
		this.output.get(outputBuffer);

		// Normalize the output and store it in the output array
		for (int bin = 0; bin < output.length; bin++) {
			output[bin].real = outputBuffer[2 * bin + REAL] / 100;
			output[bin].imag = outputBuffer[2 * bin + IMAG] / 100;
		}
	}

	@Override
	protected void finalize() {
		fftwf_destroy_plan(plan);
		fftwf_free(input);
		fftwf_free(output);
	}
}
