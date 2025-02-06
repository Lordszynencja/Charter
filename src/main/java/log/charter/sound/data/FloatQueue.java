package log.charter.sound.data;

import log.charter.sound.IResampler;
import log.charter.sound.Resampler;

public class FloatQueue {
	private static final int buffersLength = 1024 * 64;
	static int ids = 0;

	int queueId = ids++;

	private final float[][] buffers = new float[buffersLength][];
	private int from = 0;
	private int to = 0;

	private float[] buffer;
	private int position = 0;

	private boolean closed = false;

	private final IResampler resampler;

	public FloatQueue(final int bufferSize, final float resampleRatio) {
		if (bufferSize <= 0) {
			throw new IllegalArgumentException("Size must be positive");
		}

		buffer = new float[bufferSize];
		resampler = Resampler.create(resampleRatio, this::addResampled);
	}

	public int available() {
		return (from <= to ? to - from : buffersLength + to - from) * buffer.length;
	}

	public boolean bufferAvailable() {
		return from != to;
	}

	public void add(final float f) {
		if (!closed) {
			resampler.addSample(f);
		}
	}

	public void add(final float[] samples) {
		if (closed) {
			return;
		}

		if (position == 0 && samples.length == buffer.length) {
			buffer = samples;
			addBuffer();
			return;
		}

		for (final float sample : samples) {
			add(sample);
		}
	}

	private void addBuffer() {
		if (from != (to + 1) % buffersLength) {
			final int id = to;
			System.out.println("adding " + queueId + "-" + id + " -> " + this);
			buffers[id] = buffer;
			to = (to + 1) % buffersLength;
		}

		buffer = new float[buffer.length];
		position = 0;
	}

	private void addResampled(final float f) {
		buffer[position++] = f;
		if (position < buffer.length) {
			return;
		}

		addBuffer();
	}

	public float[] take() {
		if (from == to) {
			return new float[buffer.length];
		}

		final float[] buffer = buffers[from];
		buffers[from] = null;
		from = (from + 1) % buffersLength;

		return buffer;
	}

	public void close() {
		addBuffer();
		closed = true;
	}

	public boolean isFinished() {
		return closed && from == to;
	}

	@Override
	public String toString() {
		return "FloatQueue{from: " + from + ", to: " + to + ", position: " + position + ", closed: " + closed + "}";
	}
}