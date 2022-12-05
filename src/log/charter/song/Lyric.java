package log.charter.song;

import static java.lang.System.arraycopy;
import static log.charter.util.ByteUtils.bytesToDouble;
import static log.charter.util.ByteUtils.doubleToBytes;
import static log.charter.util.ByteUtils.getBit;
import static log.charter.util.ByteUtils.getBitByte;

import java.util.Arrays;
import java.util.List;

public class Lyric extends Event {
	private static final int BIT_TONELESS = 0;
	private static final int BIT_WORD_PART = 1;
	private static final int BIT_CONNECTED = 2;

	public static String joinLyrics(final List<Lyric> lyrics) {
		final StringBuilder b = new StringBuilder();

		for (final Lyric l : lyrics) {
			if (l.lyric == null) {
				continue;
			}
			b.append(l.lyric.replace("=", "-"));
			if (!l.wordPart) {
				b.append(" ");
			}
		}

		return b.toString();
	}

	public static Lyric fromBytes(final byte[] bytes, final double offset) {
		final double pos = offset + bytesToDouble(Arrays.copyOfRange(bytes, 3, 11));
		final double length = bytesToDouble(Arrays.copyOfRange(bytes, 11, 19));
		final int tone = (bytes[0] & 255) + ((bytes[1] & 255) << 8);
		final String lyric = new String(Arrays.copyOfRange(bytes, 19, bytes.length));
		return new Lyric(pos, length, tone, lyric, //
				getBit(bytes[1], BIT_TONELESS), //
				getBit(bytes[1], BIT_WORD_PART), //
				getBit(bytes[1], BIT_CONNECTED));
	}

	public int tone;
	public String lyric;
	public boolean toneless = false;
	public boolean wordPart = false;
	public boolean connected = false;

	public Lyric(final double pos, final int tone) {
		super(pos);
		this.tone = tone;
		lyric = null;
	}

	public Lyric(final double pos, final int tone, final String lyric, final boolean toneless, final boolean wordPart,
			final boolean connected) {
		super(pos);
		setLength(50);
		this.tone = tone;
		this.lyric = lyric;
		this.toneless = toneless;
		this.wordPart = wordPart;
		this.connected = connected;
	}

	public Lyric(final double pos, final double length, final int tone, final String lyric, final boolean toneless,
			final boolean wordPart, final boolean connected) {
		super(pos);
		setLength(length);
		this.tone = tone;
		this.lyric = lyric;
		this.toneless = toneless;
		this.wordPart = wordPart;
		this.connected = connected;
	}

	public Lyric(final double pos, final String lyric) {
		super(pos);
		tone = -1;
		this.lyric = lyric;
	}

	public Lyric(final Lyric l) {
		super(l);
		tone = l.tone;
		lyric = l.lyric;
		toneless = l.toneless;
		wordPart = l.wordPart;
		connected = l.connected;
	}

	public byte[] toBytes(final double offset) {
		final byte[] stringBytes = lyric.getBytes();
		final byte[] bytes = new byte[stringBytes.length + 19];
		bytes[0] = (byte) (tone & 255);
		bytes[1] = (byte) ((tone >> 8) & 255);
		bytes[2] = 0;
		bytes[2] |= (toneless ? getBitByte(BIT_TONELESS) : 0);
		bytes[2] |= (wordPart ? getBitByte(BIT_WORD_PART) : 0);
		bytes[2] |= (connected ? getBitByte(BIT_CONNECTED) : 0);

		arraycopy(doubleToBytes(pos - offset), 0, bytes, 3, 8);
		arraycopy(doubleToBytes(getLength()), 0, bytes, 11, 8);
		arraycopy(stringBytes, 0, bytes, 19, stringBytes.length);

		return bytes;
	}

	@Override
	public String toString() {
		return "Lyric{pos: " + pos//
				+ ", length: " + getLength()//
				+ ", tone: " + tone//
				+ ", lyric: " + lyric + "}";
	}
}
