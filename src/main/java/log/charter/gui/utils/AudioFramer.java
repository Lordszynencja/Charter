package log.charter.gui.utils;

import log.charter.data.managers.CharterContext.Initiable;
import log.charter.gui.handlers.AudioHandler;
import log.charter.io.Logger;

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
