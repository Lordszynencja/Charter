package log.charter.services.utils;

import log.charter.io.Logger;
import log.charter.services.AudioHandler;
import log.charter.services.CharterContext.Initiable;

public class AudioFramer implements Initiable {
	private AudioHandler audioHandler;
	private Thread thread;

	@Override
	public void init() {
		thread = new Thread(this::run);
	}

	public void start() {
		thread.start();
	}

	public void stop() {
		thread.interrupt();
	}

	private void run() {
		try {
			while (true) {
				audioFrame();
				Thread.sleep(0, 100_000);
			}
		} catch (final InterruptedException e) {
			Logger.info("Audio framer exiting", e);
		}
	}

	private void audioFrame() {
		try {
			audioHandler.frame();
		} catch (final Exception e) {
			Logger.error("Exception in audioFrame()", e);
		}
	}
}
