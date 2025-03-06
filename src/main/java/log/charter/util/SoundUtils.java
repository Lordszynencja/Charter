package log.charter.util;

import java.util.Map;
import java.util.Map.Entry;

import log.charter.data.song.configs.Tuning;

public class SoundUtils {
	public static class NoteWithScale {
		public final int scale;
		public final int note;

		public NoteWithScale(final int distanceFromC0) {
			if (distanceFromC0 >= 0) {
				scale = distanceFromC0 / 12;
				note = distanceFromC0 % 12;
			} else {
				scale = distanceFromC0 / 12 - 1;
				note = 12 + distanceFromC0 % 12;
			}
		}

		public String noteName(final boolean useSharp) {
			return (useSharp ? soundNamesSharp : soundNamesFlat)[note];
		}

		public String name(final boolean useSharp) {
			return "%s%d".formatted(noteName(useSharp), scale);
		}
	}

	private static final String[] soundNamesSharp = { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
	private static final String[] soundNamesFlat = { "C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B" };

	public static String soundToFullName(final int distanceFromC0, final boolean useSharp) {
		return new NoteWithScale(distanceFromC0).name(useSharp);
	}

	public static String soundToSimpleName(final int distanceFromC0, final boolean useSharp) {
		return new NoteWithScale(distanceFromC0).noteName(useSharp);
	}

	public static int[] getSounds(final Tuning tuning, final boolean bass, final Map<Integer, Integer> frets) {
		final int[] sounds = new int[frets.size()];
		int i = 0;
		for (final Entry<Integer, Integer> stringWithFret : frets.entrySet()) {
			sounds[i++] = Tuning.getStringDistanceFromC0(stringWithFret.getKey(), tuning.strings(), bass)
					+ stringWithFret.getValue();
		}

		return sounds;
	}
}
