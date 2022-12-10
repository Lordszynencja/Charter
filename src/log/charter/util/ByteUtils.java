package log.charter.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ByteUtils {
	public static double bytesToDouble(final byte[] bytes) {
		final long l = (((long) bytes[0]) & 255)//
				| ((((long) bytes[1]) & 255) << 8) //
				| (((((long) bytes[2])) & 255) << 16)//
				| ((((long) bytes[3]) & 255) << 24)//
				| ((((long) bytes[4]) & 255) << 32)//
				| ((((long) bytes[5]) & 255) << 40)//
				| ((((long) bytes[6]) & 255) << 48)//
				| ((((long) bytes[7]) & 255) << 56);

		return Double.longBitsToDouble(l);
	}

	public static byte[] doubleToBytes(final double d) {
		final long l = Double.doubleToLongBits(d);

		return new byte[] { (byte) (l & 255), //
				(byte) ((l >> 8) & 255), //
				(byte) ((l >> 16) & 255), //
				(byte) ((l >> 24) & 255), //
				(byte) ((l >> 32) & 255), //
				(byte) ((l >> 40) & 255), //
				(byte) ((l >> 48) & 255), //
				(byte) ((l >> 56) & 255) };
	}

	public static boolean getBit(final byte b, final int pos) {
		return (b & getBitByte(pos)) > 0;
	}

	public static boolean getBit(final int b, final int pos) {
		return (b & getBitByte(pos)) > 0;
	}

	public static byte getBitByte(final int pos) {
		return (byte) (1 << pos);
	}

	public static List<byte[]> splitToList(final byte[] bytes) {
		int a = 0;
		final List<byte[]> list = new ArrayList<>(bytes.length / 20);
		while (a < bytes.length) {
			final int length = (bytes[a] & 255) + ((bytes[a + 1] & 255) << 8);
			a += 2;

			list.add(Arrays.copyOfRange(bytes, a, a + length));
			a += length;
		}

		return list;
	}

	public static byte[] joinList(final List<byte[]> list) {
		int length = 0;
		for (final byte[] b : list) {
			length += b.length + 2;
		}

		final byte[] bytes = new byte[length];
		int a = 0;
		for (final byte[] b : list) {
			bytes[a] = (byte) (b.length & 255);
			bytes[a + 1] = (byte) ((b.length >> 8) & 255);

			System.arraycopy(b, 0, bytes, a + 2, b.length);
			a += b.length + 2;
		}

		return bytes;
	}
}
