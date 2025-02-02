package log.charter.services.utils;

import log.charter.io.Logger;
import log.charter.services.CharterContext.Initiable;
import log.charter.services.audio.AudioHandler;

public class AudioFramer implements Initiable {
	private AudioHandler audioHandler;
	private Thread thread;

	@Override
	public void init() {
		thread = new Thread(this::run);
		thread.setName("Audio framer");
	}

	public void start() {
		thread.start();
	}

	public void stop() {
		thread.interrupt();
		try {
			thread.join();
		} catch (final InterruptedException e) {
			return;
		}
	}

	private void run() {
		while (!thread.isInterrupted()) {
			audioFrame();
			try {
				Thread.sleep(0, 100_000);
			} catch (final InterruptedException e) {
				return;
			}
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
