package log.charter.main;

import java.io.IOException;

import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;

public class LogCharterRSMain {
	public static final String VERSION = "0.8.3 - 2023.01.13";
	public static final String TITLE = "LoG Charter RS";

	public static void main(final String[] args) throws InterruptedException, IOException {
		Config.init();

		if (args.length > 0) {
			new CharterFrame(LogCharterRSMain.TITLE + " : Loading project...", args[0]);
		} else {
			new CharterFrame(LogCharterRSMain.TITLE + " : " + Label.NO_PROJECT.label());
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
