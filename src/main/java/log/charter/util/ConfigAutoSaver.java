package log.charter.util;

import log.charter.data.config.Config;
import log.charter.data.config.GraphicalConfig;
import log.charter.services.mouseAndKeyboard.ShortcutConfig;

public class ConfigAutoSaver {
	private static Thread configSavingThread;

	public static void startConfigSavingThread() {
		configSavingThread = new Thread(() -> {
			while (!configSavingThread.isInterrupted()) {
				Config.save();
				GraphicalConfig.save();
				ShortcutConfig.save();

				try {
					Thread.sleep(1000);
				} catch (final InterruptedException e) {
					return;
				}
			}
		});
		configSavingThread.setName("Config backups");
		configSavingThread.start();

		ExitActions.addOnExit(() -> configSavingThread.interrupt());
	}
}
