package log.charter.sound.data;

import static java.lang.Math.max;

import java.util.ArrayList;
import java.util.List;

import log.charter.sound.asio.ASIOHandler;

public class FloatMixer {
	private final List<FloatQueue> queues = new ArrayList<>();

	private int maxQueuesAtOnce = 1;

	private float[] mix(final List<float[]> buffers) {
		maxQueuesAtOnce = max(maxQueuesAtOnce, buffers.size());

		final int bufferSize = ASIOHandler.getBufferSize();
		final float[] mixBuffer = new float[bufferSize];
		for (final float[] buffer : buffers) {
			for (int i = 0; i < buffer.length && i < bufferSize; i++) {
				mixBuffer[i] += buffer[i] / maxQueuesAtOnce;
			}
		}

		return mixBuffer;
	}

	public float[] getNextBuffer() {
		final List<float[]> buffers = new ArrayList<>();
		for (final FloatQueue queue : queues) {
			try {
				buffers.add(queue.take());
			} catch (final InterruptedException e) {
			}
		}
		queues.removeIf(q -> q.isFinished() && !q.bufferAvailable());

		return mix(buffers);
	}

	public FloatQueue generateQueue(final int sampleRate) {
		final FloatQueue queue = new FloatQueue(ASIOHandler.getBufferSize(), sampleRate,
				(int) ASIOHandler.getSampleRate());
		queues.add(queue);

		return queue;
	}
}
