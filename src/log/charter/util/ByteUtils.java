package log.charter.util;

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
}
