package log.charter.main;

import java.io.IOException;

import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;

public class LogCharterRSMain {
	public static final String VERSION = "0.9.13 - 2023.07.21";
	public static final String TITLE = "LoG Charter RS";

	public static void main(final String[] args) throws InterruptedException, IOException {
		Config.init();

		String pathToOpen = Config.lastPath;
		if (args.length > 0) {
			pathToOpen = args[0];
		}

		if (pathToOpen != null && !pathToOpen.isEmpty()) {
			new CharterFrame(LogCharterRSMain.TITLE + " : Loading project...", pathToOpen);
		} else {
			new CharterFrame(LogCharterRSMain.TITLE + " : " + Label.NO_PROJECT.label());
		}

		new Thread(() -> {
			try {
				while (true) {
					Config.save();
					Thread.sleep(1000);
				}
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}).start();
	}
}
