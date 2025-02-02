package log.charter.services.utils;

import java.util.function.DoubleConsumer;

import log.charter.data.config.Config;

public class Framer {
	private static final double scale = 1_000_000_000.0;

	private double frameLength;

	private long frameTime = System.nanoTime();
	private long previousFrameTime = System.nanoTime();

	private final DoubleConsumer runnable;
	private Thread thread;

	public Framer(final DoubleConsumer runnable) {
		this(runnable, Config.FPS);
	}

	public Framer(final DoubleConsumer runnable, final int fps) {
		this.runnable = runnable;
		setFPS(fps);
	}

	public void setFPS(final int fps) {
		frameLength = scale / fps;
	}

	private void sleepUntilNextFrame() throws InterruptedException {
		previousFrameTime = frameTime;
		frameTime += frameLength;
		final long currentTime = System.nanoTime();
		if (frameTime <= currentTime) {
			frameTime = currentTime;
		}

		final long sleepLength = (long) (frameTime - currentTime);
		if (sleepLength <= 0) {
			return;
		}

		final long milis = sleepLength / 1_000_000;
		final int nanos = (int) (sleepLength % 1_000_000);
		Thread.sleep(milis, nanos);
	}

	public void start(final String name) {
		thread = new Thread(() -> {
			while (!thread.isInterrupted()) {
				runnable.accept((frameTime - previousFrameTime) / scale);
				try {
					sleepUntilNextFrame();
				} catch (final InterruptedException e) {
					return;
				}
			}
		});
		thread.setName(name);
		thread.start();
	}

	public void stop() {
		if (thread != null) {
			thread.interrupt();
			try {
				thread.join();
			} catch (final InterruptedException e) {
				return;
			}
		}
	}
}
