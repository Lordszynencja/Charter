package log.charter.io.gp.gp7;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import log.charter.io.Logger;

public class GP7ZipReader {
	private static void writeEntry(final InputStream inputStream, final OutputStream out) throws IOException {
		final byte buffer[] = new byte[2048];
		int read = 0;
		while ((read = inputStream.read(buffer)) > 0) {
			out.write(buffer, 0, read);
		}
		out.close();
	}

	private static void saveEntry(final InputStream inputStream, final File saveLocation) throws IOException {
		writeEntry(inputStream, new FileOutputStream(saveLocation));
	}

	private static byte[] readEntry(final InputStream inputStream) throws IOException {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();

		final byte buffer[] = new byte[2048];
		int read = 0;
		while ((read = inputStream.read(buffer)) > 0) {
			out.write(buffer, 0, read);
		}

		final byte[] bytes = out.toByteArray();

		out.close();

		return bytes;
	}

	public static void unpackFile(final File file, final String path, final File saveLocation) {
		try (ZipFile zipFile = new ZipFile(file)) {
			final Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				final ZipEntry entry = entries.nextElement();
				if (entry.getName().equals(path)) {
					saveEntry(zipFile.getInputStream(entry), saveLocation);

					return;
				}
			}

			zipFile.close();
		} catch (final IOException e) {
			Logger.error("Couldn't open file " + file, e);
		}
	}

	public static String readTabXML(final File file) {
		try (ZipFile zipFile = new ZipFile(file)) {
			final Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				final ZipEntry entry = entries.nextElement();
				if (entry.getName().endsWith(".gpif")) {
					final byte[] bytes = readEntry(zipFile.getInputStream(entry));
					zipFile.close();

					return new String(bytes);
				}
			}

			zipFile.close();
			return null;
		} catch (final IOException e) {
			Logger.error("Couldn't open file " + file, e);
			return null;
		}
	}
}
