package log.charter.io.gp.gp5;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class GP5BinaryUtils {
	private static final String encoding = "UTF-8";

	public static boolean readBoolean(final ByteArrayInputStream data) {
		return data.read() != 0;
	}

	public static int readShortInt8(final ByteArrayInputStream data) {
		final int v = data.read();
		return ((v & 0x80) >> 7) * -128 + (v & 0x7F);
	}

	public static int readInt32LE(final ByteArrayInputStream data) {
		final int b0 = data.read();
		final int b1 = data.read();
		final int b2 = data.read();
		final int b3 = data.read();
		return (b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
	}

	public static double readDouble(final ByteArrayInputStream data) {
		final byte[] bytes = new byte[8];
		try {
			data.read(bytes);
		} catch (final IOException e) {
			e.printStackTrace();
		}

		long l = 0;
		for (int i = 0; i < 8; i++) {
			final int offset = i * 8;
			l |= (long) (bytes[i] & 0xFF) << offset;
		}

		return Double.longBitsToDouble(l);
	}

	public static String readString(final ByteArrayInputStream data, final int length) {
		final byte[] bytes = new byte[length];
		data.read(bytes, 0, length);
		try {
			return new String(bytes, encoding);
		} catch (final UnsupportedEncodingException e) {
			return new String(bytes);
		}
	}

	public static String readStringWithSizeSkip(final ByteArrayInputStream data) {
		data.skip(4);
		return readString(data, data.read());
	}

	public static String readStringWithSize(final ByteArrayInputStream data) {
		return readString(data, readInt32LE(data));
	}

	public static String readStringWithSkip(final ByteArrayInputStream data, final int length) {
		final int stringLength = data.read();
		final String s = readString(data, stringLength);
		if (stringLength < length) {
			data.skip(length - stringLength);
		}

		return s;
	}

	public static String readStringWithByteSkip(final ByteArrayInputStream data) {
		final int length = readInt32LE(data) - 1;
		data.read();
		return readString(data, length);
	}

	public static Color readColor(final ByteArrayInputStream data, final boolean readAlpha) {
		final int r = data.read();
		final int g = data.read();
		final int b = data.read();
		int a = 255;
		if (readAlpha) {
			a = data.read();
		} else {
			data.skip(1);
		}

		return new Color(r, g, b, a);
	}
}
