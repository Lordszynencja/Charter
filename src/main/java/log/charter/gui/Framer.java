package log.charter.gui;

public class Framer {
	public static final int frameLength = 10;

	private int currentFrame = 0;
	private int framesDone = 0;

	private final Runnable runnable;

	public Framer(final Runnable runnable) {
		this.runnable = runnable;
	}

	public void start() {
		new Thread(() -> {
			try {
				while (true) {
					currentFrame++;
					Thread.sleep(frameLength);
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
					Thread.sleep(1);
				}
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}).start();
	}
}
