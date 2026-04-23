package log.charter.util;

import java.util.Map;
import java.util.Map.Entry;

import log.charter.data.song.Arrangement;
import log.charter.data.song.configs.Tuning;
import log.charter.io.Logger;

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

	public static int getSound(final Tuning tuning, final boolean bass, final int string, final int fret) {
		return Tuning.getStringDistanceFromC0(string, tuning.strings(), bass) + fret + tuning.getTuningRaw()[string];
	}

	public static String simpleName(final Arrangement arrangement, final int string, final int fret) {
		return soundToSimpleName(getSound(arrangement.tuning, arrangement.isBass(), string, fret), true);
	}

	public static int[] getSounds(final Tuning tuning, final boolean bass, final Map<Integer, Integer> frets) {
		final int[] sounds = new int[frets.size()];
		int i = 0;
		for (final Entry<Integer, Integer> stringWithFret : frets.entrySet()) {
			final int string = stringWithFret.getKey();
			try {
				sounds[i++] = getSound(tuning, bass, string, stringWithFret.getValue());
			} catch (final Exception e) {
				Logger.error(
						"Couldn't read sound for string " + string + " in tuning " + tuning.getFullName("%s %s", bass),
						e);
			}
		}

		return sounds;
	}
}
