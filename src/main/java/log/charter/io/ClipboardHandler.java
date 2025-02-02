package log.charter.io;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.InputStream;

public class ClipboardHandler {
	public static byte[] readClipboardBytes() {
		try {
			final Transferable contents = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);

			for (final DataFlavor flavor : contents.getTransferDataFlavors()) {
				if (flavor.isMimeTypeEqual("application/octet-stream")) {
					final Object o = contents.getTransferData(flavor);

					if (InputStream.class.isAssignableFrom(o.getClass())) {
						final InputStream in = (InputStream) o;

						final byte[] bytes = new byte[in.available()];
						in.read(bytes);
						in.close();

						return bytes;
					} else if (o instanceof byte[]) {
						return (byte[]) o;
					}
				} else if (flavor.isMimeTypeEqual("text/plain") && (flavor.getRepresentationClass() == String.class)) {
					return ((String) contents.getTransferData(flavor)).getBytes();
				}
			}
		} catch (final Exception e) {
			Logger.error("Exception when reading clipboard bytes", e);
		}

		return new byte[0];
	}

	public static void setClipboardBytes(final byte[] bytes) {
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new BytesSelection(bytes), null);
	}
}
