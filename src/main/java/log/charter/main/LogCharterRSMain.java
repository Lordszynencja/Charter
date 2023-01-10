package log.charter.main;

import java.io.IOException;

import log.charter.data.config.Config;
import log.charter.gui.CharterFrame;

public class LogCharterRSMain {
	public static final String VERSION = "0.8.1-2023.01.09";
	public static final String TITLE = "LoG Charter RS";

	public static void main(final String[] args) throws InterruptedException, IOException {
		Config.init();

		if (args.length > 0) {
			new CharterFrame(args[0]);
		} else {
			new CharterFrame();
		}

		new Thread(() -> {
			try {
				while (true) {
					Config.save();
					Thread.sleep(10000);
				}
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}).start();
	}

}
