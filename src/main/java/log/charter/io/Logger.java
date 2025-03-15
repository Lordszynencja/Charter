package log.charter.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import log.charter.data.config.values.DebugConfig;
import log.charter.util.RW;

public class Logger {
	private static final SimpleDateFormat timeFormat = new SimpleDateFormat("<yyyy-MM-dd HH:mm:ss>");

	private static PrintStream out = System.out;

	static {
		try {
			final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			final String name = "log-" + dateFormat.format(new Date()) + ".txt";
			final File dir = new File(RW.getJarDirectory(), "logs");
			if (!dir.exists()) {
				dir.mkdirs();
				dir.mkdir();
			}

			out = new PrintStream(new FileOutputStream(new File(dir, name), true));
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private static String getLine(final String type, final String msg) {
		return "[" + type + "]" + timeFormat.format(new Date()) + " " + msg;
	}

	public static void debug(String msg) {
		if (!DebugConfig.logging) {
			return;
		}

		msg = getLine("DEBUG", msg);
		out.println(msg);

		if (out != System.out) {
			System.out.println(msg);
		}
	}

	public static void debug(String msg, final Exception e) {
		msg = getLine("DEBUG", msg);

		out.println(msg);
		e.printStackTrace(out);

		if (out != System.out) {
			System.out.println(msg);
			e.printStackTrace(System.out);
		}
	}

	public static void info(String msg) {
		msg = getLine("INFO", msg);

		out.println(msg);

		if (out != System.out) {
			System.out.println(msg);
		}
	}

	public static void info(String msg, final Exception e) {
		msg = getLine("INFO", msg);

		out.println(msg);
		e.printStackTrace(out);

		if (out != System.out) {
			System.out.println(msg);
			e.printStackTrace(System.out);
		}
	}

	public static void warning(String msg) {
		msg = getLine("WARNING", msg);

		out.println(msg);

		if (out != System.out) {
			System.out.println(msg);
		}
	}

	public static void warning(String msg, final Throwable e) {
		msg = getLine("WARNING", msg);

		out.println(msg);
		e.printStackTrace(out);

		if (out != System.out) {
			System.out.println(msg);
			e.printStackTrace(System.out);
		}
	}

	public static void error(String msg) {
		msg = getLine("ERROR", msg);

		out.println(msg);

		if (out != System.out) {
			System.out.println(msg);
		}
	}

	public static void error(String msg, final Throwable e) {
		msg = getLine("ERROR", msg);

		out.println(msg);
		e.printStackTrace(out);

		if (out != System.out) {
			System.out.println(msg);
			e.printStackTrace(System.out);
		}
	}
}
