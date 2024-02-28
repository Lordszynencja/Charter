package log.charter.util.chordRecognition;

import static log.charter.util.SoundUtils.soundToSimpleName;

import java.util.Arrays;
import java.util.Map.Entry;

import log.charter.song.configs.Tuning;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashMap2;

public class ChordNameSuggester {
	private static ArrayList2<Integer> soundsToNotes(final int[] sounds) {
		final ArrayList2<Integer> notes = new ArrayList2<>();
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

	private static ArrayList2<String> recognizeChord(final int[] sounds) {
		final ArrayList2<Integer> notes = soundsToNotes(sounds);
		if (notes.size() == 1) {
			return new ArrayList2<>(soundToSimpleName(notes.get(0), true));
		}

		final ArrayList2<String> foundNames = new ArrayList2<>();
		for (int i = 0; i < notes.size(); i++) {
			final int root = notes.get(i);
			final ArrayList2<String> foundNamesForRoot = ChordNameAdder.getSuggestedChordNames(root, notes);
			if (root != sounds[0] % 12) {
				foundNamesForRoot.addAll(
						foundNamesForRoot.map(chordName -> chordName + "/" + soundToSimpleName(sounds[0], true)));
			}
			foundNames.addAll(foundNamesForRoot);
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

	public static ArrayList2<String> suggestChordNames(final Tuning tuning,
			final HashMap2<Integer, Integer> templateFrets) {
		final int[] sounds = new int[templateFrets.size()];
		int i = 0;
		for (final Entry<Integer, Integer> stringWithFret : templateFrets.entrySet()) {
			sounds[i++] = tuning.getStringOffset(stringWithFret.getKey()) + stringWithFret.getValue();
		}

		while (negativeExists(sounds)) {
			for (i = 0; i < sounds.length; i++) {
				sounds[i] += 12;
			}
		}

		Arrays.sort(sounds);

		if (sounds.length == 1) {
			return new ArrayList2<>(soundToSimpleName(sounds[0], true));
		}

		return recognizeChord(sounds);
	}
}
