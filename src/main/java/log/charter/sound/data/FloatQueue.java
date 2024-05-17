package log.charter.sound.data;

import java.util.LinkedList;
import java.util.List;

import log.charter.sound.IResampler;
import log.charter.sound.Resampler;

public class FloatQueue {
	private final List<float[]> buffers = new LinkedList<>();
	private float[] buffer;
	private int position = 0;

	private boolean closed = false;

	private final IResampler resampler;

	public FloatQueue(final int size, final int sampleRate, final int targetSampleRate) {
		if (size <= 0) {
			throw new IllegalArgumentException("Size must be positive");
		}

		buffer = new float[size];
		resampler = Resampler.create(sampleRate, targetSampleRate, this::addResampled);
	}

	public int available() {
		return buffers.size() * buffer.length;
	}

	public boolean bufferAvailable() {
		return !buffers.isEmpty();
	}

	public void add(final float f) throws Exception {
		resampler.addSample(f);
	}

	private void addResampled(final float f) throws InterruptedException {
		buffer[position++] = f;
		if (position >= buffer.length) {
			synchronized (buffers) {
				buffers.add(buffer);
			}

			buffer = new float[buffer.length];
			position = 0;
		}
	}

	public float[] take() throws InterruptedException {
		synchronized (buffers) {
			return buffers.remove(0);
		}
	}

	public void close() {
		synchronized (buffers) {
			buffers.add(buffer);
		}

		closed = true;
	}

	public boolean isFinished() {
		return closed && buffers.isEmpty();
	}
}