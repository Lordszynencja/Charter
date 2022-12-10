package log.charter.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import log.charter.data.Config;

public class Logger {
	private static PrintStream out = System.out;

	static {
		try {
			final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			final String name = "log-" + dateFormat.format(new Date()) + ".txt";
			final File dir = new File("logs/");
			if (!dir.exists()) {
				dir.mkdirs();
			}
			out = new PrintStream(new FileOutputStream(dir.getAbsolutePath() + "/" + name, false));
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public static void debug(final String msg) {
		if (Config.debugLogging) {
			out.println("[DEBUG] " + msg);

			if (out != System.out) {
				System.out.println("[DEBUG] " + msg);
			}
		}
	}

	public static void error(final String msg) {
		out.println("[ERROR] " + msg);

		if (out != System.out) {
			System.out.println("[ERROR] " + msg);
		}
	}

	public static void error(final String msg, final Exception e) {
		out.println("[ERROR] " + msg);
		e.printStackTrace(out);

		if (out != System.out) {
			System.out.println("[ERROR] " + msg);
			e.printStackTrace(System.out);
		}
	}

	public static void setOutput(final PrintStream output) {
		out = output;
	}
}
