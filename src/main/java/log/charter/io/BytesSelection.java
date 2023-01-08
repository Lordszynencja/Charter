package log.charter.io;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;

public class BytesSelection implements Transferable, ClipboardOwner {

	private static final byte[] hexes = { '0', '1', '2', '3', '4', '5', '6', '7', '8', //
			'9', 'A', 'B', 'C', 'D', 'E', 'F' };

	private static final int plainText = 0;
	private static final int string = 1;
	private static final int inputStream = 2;
	private static final int byteArray = 3;

	private static final DataFlavor[] supportedFlavors;

	static {
		DataFlavor[] flavors;

		try {
			flavors = new DataFlavor[] { new DataFlavor("text/plain; charset=unicode; class=java.io.InputStream"), //
					new DataFlavor("application/x-java-serialized-object; class=java.lang.String"), //
					new DataFlavor("application/octet-stream; class=java.io.InputStream"), //
					new DataFlavor("application/octet-stream") };
		} catch (final Exception e) {
			flavors = new DataFlavor[0];
		}

		supportedFlavors = flavors;
	}

	private final byte[] bytes;
	private final byte[] hexedBytes;

	public BytesSelection(final byte[] bytes) {
		this.bytes = bytes;
		hexedBytes = new byte[bytes.length * 2];
		for (int i = 0; i < bytes.length; i++) {
			hexedBytes[i * 2] = hexes[(bytes[i] >> 4) & 15];
			hexedBytes[(i * 2) + 1] = hexes[bytes[i] & 15];
		}
	}

	@Override
	public Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (supportedFlavors[plainText].equals(flavor)) {
			return new StringReader(new String(bytes));
		} else if (supportedFlavors[string].equals(flavor)) {
			return new String(bytes);
		} else if (supportedFlavors[inputStream].equals(flavor)) {
			return new ByteArrayInputStream(bytes);
		} else if (supportedFlavors[byteArray].equals(flavor)) {
			return bytes;
		}

		throw new UnsupportedFlavorException(flavor);
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return supportedFlavors.clone();
	}

	@Override
	public boolean isDataFlavorSupported(final DataFlavor flavor) {
		for (final DataFlavor supportedFlavor : supportedFlavors) {
			if (supportedFlavor.equals(flavor)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void lostOwnership(final Clipboard clipboard, final Transferable contents) {
	}
}