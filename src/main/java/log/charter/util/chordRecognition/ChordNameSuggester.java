package log.charter.util.chordRecognition;

import static java.lang.Math.floorMod;
import static java.util.Arrays.asList;
import static log.charter.util.SoundUtils.soundToSimpleName;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import log.charter.data.song.ChordTemplate;
import log.charter.data.song.configs.Tuning;
import log.charter.util.SoundUtils;
import log.charter.util.collections.ArrayList2;

public class ChordNameSuggester {
	private static final List<String> tonesSharp = asList("A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#", "G",
			"G#");
	private static final List<String> tonesFlat = asList("A", "Bb", "B", "C", "Db", "D", "Eb", "E", "F", "Gb", "G",
			"Ab");
	private static final Set<String> possibleTones = new HashSet<>();

	static {
		possibleTones.addAll(tonesSharp);
		possibleTones.addAll(tonesFlat);
	}

	public static String changeToneName(final String tone, final int change) {
		if (!possibleTones.contains(tone)) {
			return "?";
		}

		if (change % 12 == 0) {
			return tone;
		}

		final List<List<String>> order = change > 0 ? asList(tonesSharp, tonesFlat) : asList(tonesFlat, tonesSharp);

		for (final List<String> tones : order) {
			final int id = tones.indexOf(tone);
			if (id >= 0) {
				return tones.get(floorMod(id + change, 12));
			}
		}

		throw new IllegalArgumentException("Unknown error when changing tone " + tone + " by " + change);
	}

	private static List<String> recognizeChord(final List<Integer> notes) {
		if (notes.size() == 1) {
			return asList(soundToSimpleName(notes.get(0), true));
		}

		final List<String> foundNames = new ArrayList2<>();
		for (int i = 0; i < notes.size(); i++) {
			final int root = notes.get(i);
			final List<String> foundNamesForRoot = ChordNameAdder.getSuggestedChordNames(root, notes);
			foundNames.addAll(foundNamesForRoot);

			if (i != 0) {
				for (final String name : foundNamesForRoot) {
					foundNames.add(name + "/" + soundToSimpleName(notes.get(0), true));
				}
			}
		}

		return foundNames;
	}

	public static List<String> suggestChordNames(final Tuning tuning, final Map<Integer, Integer> templateFrets) {
		final List<Integer> notes = SoundUtils.getNotes(tuning, false, templateFrets);

		if (notes.size() == 1) {
			return asList();
		}

		return recognizeChord(notes);
	}

	public static void setFirstSuggestedName(final ChordTemplate template, final Tuning tuning) {
		if (SoundUtils.getNotes(tuning, false, template.frets).size() <= 2) {
			return;
		}

		final List<String> suggestions = suggestChordNames(tuning, template.frets);
		template.chordName = suggestions.isEmpty() ? "" : suggestions.get(0);
	}
}
