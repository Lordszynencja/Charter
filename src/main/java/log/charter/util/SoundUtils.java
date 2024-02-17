package log.charter.util;

public class SoundUtils {
	private static final String[] soundNamesSharp = { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
	private static final String[] soundNamesFlat = { "C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B" };

	public static String soundToFullName(final int distanceFromC0, final boolean useSharp) {
		final int soundInScale = distanceFromC0 % 12;
		final int scale = distanceFromC0 / 12;

		return "%s%d".formatted((useSharp ? soundNamesSharp : soundNamesFlat)[soundInScale], scale);
	}

	public static String soundToSimpleName(final int distanceFromC0, final boolean useSharp) {
		return (useSharp ? soundNamesSharp : soundNamesFlat)[distanceFromC0 % 12];
	}
}
