package log.charter.io;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.InputStream;

public class ClipboardHandler {
	private static final byte[] dehexes = new byte[256];
	static {
		for (int i = 0; i < 256; i++) {
			dehexes[i] = -1;
		}

		dehexes['0'] = 0;
		dehexes['1'] = 1;
		dehexes['2'] = 2;
		dehexes['3'] = 3;
		dehexes['4'] = 4;
		dehexes['5'] = 5;
		dehexes['6'] = 6;
		dehexes['7'] = 7;
		dehexes['8'] = 8;
		dehexes['9'] = 9;
		dehexes['A'] = 10;
		dehexes['B'] = 11;
		dehexes['C'] = 12;
		dehexes['D'] = 13;
		dehexes['E'] = 14;
		dehexes['F'] = 15;
	}

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
					final byte[] stringBytes = ((String) contents.getTransferData(flavor)).getBytes();
					final byte[] bytes = new byte[stringBytes.length / 2];

					boolean isHex = true;
					for (int i = 0; i < bytes.length; i++) {
						final byte b0 = dehexes[stringBytes[i * 2]];
						if ((b0 < 0) || (b0 > 15)) {
							isHex = false;
							break;
						}
						final byte b1 = dehexes[stringBytes[(i * 2) + 1]];
						if ((b1 < 0) || (b1 > 15)) {
							isHex = false;
							break;
						}
						bytes[i] = (byte) ((b0 << 4) | b1);
					}
					if (isHex) {
						return bytes;
					}
				}
			}
		} catch (final Exception e) {
		}

		return new byte[0];
	}

	public static void setClipboardBytes(final byte[] bytes) {
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new BytesSelection(bytes), null);
	}
}
