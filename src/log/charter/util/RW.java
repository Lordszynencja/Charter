package log.charter.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class RW {

	public static File getOrCreateFile(final String filename) {
		final File f = new File(filename);
		if (!f.exists()) {
			final int split = filename.lastIndexOf("/");
			if (split != -1) {
				final String folder = filename.substring(0, split);
				try {
					Files.createDirectories(Paths.get(folder));
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
			try {
				Files.createFile(Paths.get(filename));
				return new File(filename);
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
		return f;
	}

	public static String read(final File f) {
		return new String(readB(f));
	}

	public static String read(final String filename) {
		return new String(readB(filename));
	}

	public static byte[] readB(final File f) {
		if (!f.exists()) {
			return new byte[0];
		}

		try {
			final FileInputStream input = new FileInputStream(f);
			final byte[] bytes = new byte[(int) f.length()];
			input.read(bytes);
			input.close();
			return bytes;
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return new byte[0];
	}

	public static byte[] readB(final String filename) {
		return readB(new File(filename));
	}

	public static Map<String, String> readConfig(final String filename) {
		final String[] lines = read(filename).split("\r\n|\r|\n");

		final Map<String, String> config = new HashMap<>();
		for (final String line : lines) {
			final int split = line.indexOf('=');
			if (split != -1) {
				config.put(line.substring(0, split).trim(), line.substring(split + 1));
			}
		}

		return config;
	}

	public static void write(final File f, final String content) {
		writeB(f, content.getBytes());
	}

	public static void write(final String filename, final String content) {
		writeB(filename, content.getBytes());
	}

	public static void write(final String filename, final String content, final String charset) {
		writeB(filename, content.getBytes(Charset.forName(charset)));
	}

	public static void writeB(final File f, final byte[] content) {
		try {
			final FileOutputStream output = new FileOutputStream(f);
			output.write(content);
			output.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public static void writeB(final String filename, final byte[] content) {
		writeB(getOrCreateFile(filename), content);
	}

	public static void writeConfig(final String filename, final Map<String, String> config) {
		final List<String> lines = new ArrayList<>(config.size());

		for (final Entry<String, String> entry : config.entrySet()) {
			lines.add(entry.getKey() + "=" + entry.getValue());
		}
		lines.sort(null);

		writeB(filename, String.join("\r\n", lines).getBytes());
	}
}
