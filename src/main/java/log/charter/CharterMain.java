package log.charter;

import java.io.File;
import java.io.IOException;

import log.charter.data.config.Config;
import log.charter.data.config.GraphicalConfig;
import log.charter.data.config.values.PathsConfig;
import log.charter.io.Logger;
import log.charter.services.CharterContext;
import log.charter.services.mouseAndKeyboard.ShortcutConfig;
import log.charter.util.RW;

public class CharterMain {
	public static final String VERSION = "0.21.19";
	public static final String VERSION_DATE = "2026.01.30 13:10";
	public static final String TITLE = "Charter " + VERSION;

	private static void deleteTempUpdateFile() {
		try {
			final File tempUpdateFile = new File(RW.getJarDirectory(), "tmp_update.bat");
			if (tempUpdateFile.exists()) {
				tempUpdateFile.delete();
			}
		} catch (final SecurityException e) {
			Logger.debug("Couldn't delete tmp_update.bat", e);
		}
	}

	private static void initConfigs() {
		Config.init();
		GraphicalConfig.init();
		ShortcutConfig.init();
	}

	private static String getPathToOpen(final String[] args) {
		if (args.length > 0) {
			return args[0];
		}

		return PathsConfig.lastPath;
	}

	private static void startContext(final String[] args) {
		final String pathToOpen = getPathToOpen(args);

		final CharterContext context = new CharterContext();
		context.init();

		if (pathToOpen != null && !pathToOpen.isBlank()) {
			context.openProject(pathToOpen);
		}

		context.checkForUpdates();
	}

	public static void main(final String[] args) throws InterruptedException, IOException {
		try {
			deleteTempUpdateFile();
			initConfigs();
			startContext(args);
			Logger.info("Charter started");
		} catch (final Throwable t) {
			Logger.error("Couldn't start Charter", t);
		}
	}
}
