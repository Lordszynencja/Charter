package log.charter.gui;

import static java.lang.Math.max;

import log.charter.data.config.Config;

public class Framer {
	public static double frameLength = 1000.0 / Config.FPS;

	private double nextFrameTime = System.nanoTime() / 1_000_000;
	private int currentFrame = 0;
	private int framesDone = 0;

	private final Runnable runnable;

	public Framer(final Runnable runnable) {
		this.runnable = runnable;
	}

	private long getCurrentTime() {
		return System.nanoTime() / 1_000_000;
	}

	private long getSleepLength() {
		return max(1, (long) (nextFrameTime - getCurrentTime()));
	}

	public void start() {
		new Thread(() -> {
			try {
				while (true) {
					while (nextFrameTime <= getCurrentTime()) {
						nextFrameTime += frameLength;
						currentFrame++;
					}

					Thread.sleep(getSleepLength());
				}
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}).start();

		new Thread(() -> {
			try {
				while (true) {
					while (currentFrame > framesDone) {
						runnable.run();
						framesDone++;
					}

					Thread.sleep(getSleepLength());
				}
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}).start();
	}
}
