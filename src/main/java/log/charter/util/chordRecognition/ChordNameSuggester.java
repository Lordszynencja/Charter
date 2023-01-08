package log.charter.util.chordRecognition;

import java.util.Arrays;
import java.util.Map.Entry;

import log.charter.song.configs.Tuning;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashMap2;

public class ChordNameSuggester {
	private static final String[] toneNames = { //
			"E", // 0
			"F", // 1
			"F#", // 2
			"G", // 3
			"G#", // 4
			"A", // 5
			"A#", // 6
			"B", // 7
			"C", // 8
			"C#", // 9
			"D", // 10
			"D#"// 11
	};

	public static String getToneName(int tone) {
		while (tone < 0) {
			tone += 12;
		}
		tone = tone % 12;
		return toneNames[tone];
	}

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
			return new ArrayList2<>(getToneName(notes.get(0)));
		}

		final ArrayList2<String> foundNames = new ArrayList2<>();
		for (int i = 0; i < notes.size(); i++) {
			final int root = notes.get(i);
			foundNames.addAll(ChordNameAdder.getSuggestedChordNames(root, notes));
		}

		return foundNames;
	}

	public static ArrayList2<String> suggestChordNames(final Tuning tuning,
			final HashMap2<Integer, Integer> templateFrets) {
		final int[] sounds = new int[templateFrets.size()];
		int i = 0;
		for (final Entry<Integer, Integer> stringWithFret : templateFrets.entrySet()) {
			sounds[i++] = tuning.getStringOffset(stringWithFret.getKey()) + stringWithFret.getValue();
		}

		Arrays.sort(sounds);

		if (sounds.length == 1) {
			return new ArrayList2<>(getToneName(sounds[0]));
		}

		return recognizeChord(sounds);
	}
}
