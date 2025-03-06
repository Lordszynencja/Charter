package log.charter.util.chordRecognition;

import static java.util.Arrays.asList;
import static log.charter.util.SoundUtils.soundToSimpleName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import log.charter.data.song.configs.Tuning;
import log.charter.util.SoundUtils;
import log.charter.util.collections.ArrayList2;

public class ChordNameSuggester {
	private static List<Integer> soundsToNotes(final int[] sounds) {
		final List<Integer> notes = new ArrayList<>(sounds.length);
		for (int i = 0; i < sounds.length; i++) {
			int note = sounds[i];
			while (note < 0) {
				note += 12;
			}
			note = note % 12;

			if (!notes.contains(note)) {
				notes.add(note);
			}
		}

		return notes;
	}

	private static List<String> recognizeChord(final int[] sounds) {
		final List<Integer> notes = soundsToNotes(sounds);
		if (notes.size() == 1) {
			return asList(soundToSimpleName(notes.get(0), true));
		}

		final List<String> foundNames = new ArrayList2<>();
		for (int i = 0; i < notes.size(); i++) {
			final int root = notes.get(i);
			final List<String> foundNamesForRoot = ChordNameAdder.getSuggestedChordNames(root, notes);
			foundNames.addAll(foundNamesForRoot);

			if (root != sounds[0] % 12) {
				for (final String name : foundNamesForRoot) {
					foundNames.add(name + "/" + soundToSimpleName(root, true));
				}
			}
		}

		return foundNames;
	}

	private static boolean negativeExists(final int[] sounds) {
		for (final int sound : sounds) {
			if (sound < 0) {
				return true;
			}
		}

		return false;
	}

	public static List<String> suggestChordNames(final Tuning tuning, final Map<Integer, Integer> templateFrets) {
		final int[] sounds = SoundUtils.getSounds(tuning, false, templateFrets);

		while (negativeExists(sounds)) {
			for (int i = 0; i < sounds.length; i++) {
				sounds[i] += 12;
			}
		}

		Arrays.sort(sounds);

		if (sounds.length == 1) {
			return asList(soundToSimpleName(sounds[0], true));
		}

		return recognizeChord(sounds);
	}
}
