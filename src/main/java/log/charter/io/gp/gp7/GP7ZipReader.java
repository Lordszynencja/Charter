package log.charter.io.gp.gp7;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import log.charter.io.Logger;
import log.charter.util.RW;

public class GP7ZipReader {
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

	public static String readTabXML(final File file) {
		try (ZipFile zipFile = new ZipFile(file)) {
			final Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				final ZipEntry entry = entries.nextElement();
				if (entry.getName().endsWith(".gpif")) {
					final byte[] bytes = readEntry(zipFile.getInputStream(entry));
					zipFile.close();

					RW.writeB(file.getAbsolutePath().replace(".gp", ".xml"), bytes);

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
