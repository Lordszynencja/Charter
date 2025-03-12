package log.charter;

import java.io.File;
import java.io.IOException;

import log.charter.data.config.Config;
import log.charter.data.config.GraphicalConfig;
import log.charter.data.config.values.PathsConfig;
import log.charter.io.Logger;
import log.charter.services.CharterContext;
import log.charter.services.mouseAndKeyboard.ShortcutConfig;

public class CharterMain {
	public static final String VERSION = "0.21.1";
	public static final String VERSION_DATE = "2025.03.12 22:00";
	public static final String TITLE = "Charter " + VERSION;

	public static void main(final String[] args) throws InterruptedException, IOException {
		try {
			new File("tmp_update.bat").delete();
		} catch (final SecurityException e) {
			Logger.debug("Couldn't delete tmp_update.bat", e);
		}

		try {
			Config.init();
			GraphicalConfig.init();
			ShortcutConfig.init();

			String pathToOpen = PathsConfig.lastPath;
			if (args.length > 0) {
				pathToOpen = args[0];
			}

			final CharterContext context = new CharterContext();
			context.init();

			if (pathToOpen != null && !pathToOpen.isEmpty()) {
				context.openProject(pathToOpen);
			}

			context.checkForUpdates();

			Logger.info("Charter started");
		} catch (final Throwable t) {
			Logger.error("Couldn't start Charter", t);
		}
	}
}
