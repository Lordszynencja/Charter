package log.charter.util.chordRecognition;

import java.util.Arrays;
import java.util.Map.Entry;

import log.charter.song.configs.Tuning;
import log.charter.song.configs.Tuning.TuningType;
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

	private static HashMap2<Integer, Integer> prepareFrets(final int... frets) {
		final HashMap2<Integer, Integer> fretsMap = new HashMap2<>();
		for (int string = 0; string < frets.length; string++) {
			if (frets[string] >= 0) {
				fretsMap.put(string, frets[string]);
			}
		}

		return fretsMap;
	}

	private static void getAndPrintChordNames(final Tuning tuning, final int... frets) {
		final ArrayList2<String> chordNames = ChordNameSuggester.suggestChordNames(tuning, prepareFrets(frets));
		String fretsName = tuning.tuningType.name + " ";
		for (int i = 0; i < tuning.strings; i++) {
			fretsName += frets.length > i && frets[i] >= 0 ? "" + frets[i] : "-";
		}
		System.out.println(fretsName + ":");
		chordNames.forEach(System.out::println);
	}

	public static void main(final String[] args) {
		final Tuning standard = new Tuning();
		standard.tuning(TuningType.E_STANDARD);
		standard.strings = 6;

//		getAndPrintChordNames(standard, 0, 2);
//		getAndPrintChordNames(standard, 1, 1, 3);
//		getAndPrintChordNames(standard, 1, 3, 3);
		getAndPrintChordNames(standard, 0, 2, 2, 1, 0, 0);

//		final Tuning dropD = new Tuning();
//		dropD.tuning(TuningType.E_DROP_D);
//		dropD.strings = 6;
//		getAndPrintChordNames(dropD, 0, 0);
//		getAndPrintChordNames(dropD, 0, 2);
//		getAndPrintChordNames(dropD, 0, 0, 0);
//		getAndPrintChordNames(dropD, 2, 0, 2);
	}
}
