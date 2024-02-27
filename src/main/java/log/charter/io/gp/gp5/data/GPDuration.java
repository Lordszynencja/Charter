package log.charter.io.gp.gp5.data;

public enum GPDuration {
	NOTE_1(64), //
	NOTE_2(32), //
	NOTE_4(16), //
	NOTE_8(8), //
	NOTE_16(4), //
	NOTE_32(2), //
	NOTE_64(1);

	public static GPDuration fromValue(final int value) {
		return switch (value) {
			case -2 -> GPDuration.NOTE_1;
			case -1 -> GPDuration.NOTE_2;
			case 0 -> GPDuration.NOTE_4;
			case 1 -> GPDuration.NOTE_8;
			case 2 -> GPDuration.NOTE_16;
			case 3 -> GPDuration.NOTE_32;
			case 4 -> GPDuration.NOTE_64;
			default -> GPDuration.NOTE_4;
		};
	}

	public final int length;
	public final int denominator;

	private GPDuration(final int length) {
		this.length = length;
		denominator = 64 / length;
	}
}
