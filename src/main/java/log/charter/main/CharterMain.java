package log.charter.main;

import java.io.IOException;

import log.charter.data.config.Config;
import log.charter.data.config.GraphicalConfig;
import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.io.Logger;

public class CharterMain {
	public static final String VERSION = "0.15.3";
	public static final String VERSION_DATE = "2024.02.28";
	public static final String TITLE = "Charter " + VERSION;

	public static void main(final String[] args) throws InterruptedException, IOException {
		Config.init();
		GraphicalConfig.init();

		String pathToOpen = Config.lastPath;
		if (args.length > 0) {
			pathToOpen = args[0];
		}

		if (pathToOpen != null && !pathToOpen.isEmpty()) {
			new CharterFrame(TITLE + " : Loading project...", pathToOpen);
		} else {
			new CharterFrame(TITLE + " : " + Label.NO_PROJECT.label());
		}

		new Thread(() -> {
			try {
				while (true) {
					Config.save();
					GraphicalConfig.save();

					Thread.sleep(1000);
				}
			} catch (final InterruptedException e) {
				Logger.error("Error in config save thread", e);
			}
		}).start();

		Logger.info("Program started");
	}
}
