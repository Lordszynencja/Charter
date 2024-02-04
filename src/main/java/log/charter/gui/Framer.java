package log.charter.gui;

import static java.lang.Math.max;

import log.charter.data.config.Config;

public class Framer {
	public double frameLength = 1000.0 / Config.FPS;

	private double nextFrameTime = System.nanoTime() / 1_000_000;
	private int currentFrame = 0;
	private int framesDone = 0;

	private final Runnable runnable;

	public Framer(final Runnable runnable) {
		this(runnable, Config.FPS);
	}

	public Framer(final Runnable runnable, final int fps) {
		this.runnable = runnable;
		setFPS(fps);
	}

	public void setFPS(final int fps) {
		frameLength = 1000.0 / fps;
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
