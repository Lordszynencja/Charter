package log.charter.main;

import java.io.IOException;
import java.io.InputStream;

import log.charter.data.config.Config;
import log.charter.gui.CharterFrame;
import log.charter.util.RW;

public class LogCharterRSMain {
	public static final String VERSION = "0.2.0-2022.12.26";
	public static final String TITLE = "LoG Charter RS";

	private static void createHelpFile() throws IOException {
		final InputStream input = ClassLoader.getSystemResourceAsStream("log/charter/main/help.txt");
		if (input != null) {
			final byte[] bytes = new byte[input.available()];
			input.read(bytes);
			RW.writeB("help.txt", bytes);
		}
	}

	public static void main(final String[] args) throws InterruptedException, IOException {
		Config.init();
		createHelpFile();

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
