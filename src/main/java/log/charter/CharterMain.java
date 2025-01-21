package log.charter;

import java.io.IOException;

import log.charter.data.config.Config;
import log.charter.data.config.GraphicalConfig;
import log.charter.io.Logger;
import log.charter.services.CharterContext;
import log.charter.services.mouseAndKeyboard.ShortcutConfig;

public class CharterMain {
	public static final String VERSION = "0.18.1";
	public static final String VERSION_DATE = "2025.01.21 10:00";
	public static final String TITLE = "Charter " + VERSION;

	public static void main(final String[] args) throws InterruptedException, IOException {
		Config.init();
		GraphicalConfig.init();
		ShortcutConfig.init();

		String pathToOpen = Config.lastPath;
		if (args.length > 0) {
			pathToOpen = args[0];
		}

		final CharterContext context = new CharterContext();
		context.init();

		if (pathToOpen != null && !pathToOpen.isEmpty()) {
			context.openProject(pathToOpen);
		}

		Logger.info("Charter started");
	}
}
