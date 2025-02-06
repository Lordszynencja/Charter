package log.charter.sound.data;

import static java.lang.Math.max;

import java.util.ArrayList;
import java.util.List;

public class FloatMixer {
	private final int bufferSize;

	private final List<FloatQueue> queues = new ArrayList<>();
	private final FloatQueue output;

	private int maxQueuesAtOnce = 1;

	public FloatMixer(final int bufferSize) {
		this.bufferSize = bufferSize;

		output = new FloatQueue(bufferSize, 1);
	}

	public boolean hasNextBuffer() {
		return output.bufferAvailable();
	}

	public float[] getNextBuffer() {
		return output.take();
	}

	private boolean queuesHaveData() {
		for (final FloatQueue queue : queues) {
			if (!queue.isFinished() && !queue.bufferAvailable()) {
				return false;
			}
		}

		return true;
	}

	private float[] mix(final List<float[]> buffers) {
		maxQueuesAtOnce = max(maxQueuesAtOnce, buffers.size());

		final float[] mixBuffer = new float[bufferSize];
		for (final float[] buffer : buffers) {
			for (int i = 0; i < buffer.length && i < bufferSize; i++) {
				mixBuffer[i] += buffer[i] / maxQueuesAtOnce;
			}
		}

		return mixBuffer;
	}

	public void tryMixingAll() {
		queues.removeIf(q -> q.isFinished() && !q.bufferAvailable());
		while (queuesHaveData()) {
			final List<float[]> buffers = new ArrayList<>();
			for (final FloatQueue queue : queues) {
				buffers.add(queue.take());
			}
			queues.removeIf(q -> q.isFinished() && !q.bufferAvailable());

			output.add(mix(buffers));
		}
	}

	public FloatQueue generateQueue(final float resampleRatio) {
		final FloatQueue queue = new FloatQueue(bufferSize, resampleRatio) {
			@Override
			public void add(final float[] samples) {
				super.add(samples);
				tryMixingAll();
			}
		};
		queues.add(queue);

		return queue;
	}
}
