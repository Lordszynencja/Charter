package log.charter.util.chordRecognition;

import static java.util.Arrays.asList;
import static log.charter.util.SoundUtils.soundToSimpleName;

import java.util.ArrayList;
import java.util.List;

public class ChordNameAdder {
	public static String min = "m";
	public static String maj = "Δ";
	public static String dim = "°";
	public static String add = "add";
	public static String aug = "aug";
	public static String sus = "sus";

	static interface ChordTypeChecker {
		boolean check(final int root, final List<Integer> notes);
	}

	static interface ChordNameGenerator {
		String generate(int root);
	}

	private final ChordTypeChecker checker;
	private final ChordNameGenerator nameGenerator;

	private ChordNameAdder(final ChordTypeChecker checker, final ChordNameGenerator nameGenerator) {
		this.checker = checker;
		this.nameGenerator = nameGenerator;
	}

	void add(final int root, final List<Integer> notes, final List<String> foundNames) {
		if (checker.check(root, notes)) {
			foundNames.add(nameGenerator.generate(root));
		}
	}

	private static ChordTypeChecker notes(final int... requiredTones) {
		return (root, notes) -> {
			if (notes.size() != requiredTones.length + 1) {
				return false;
			}

			for (final int requiredTone : requiredTones) {
				if (!notes.contains((root + requiredTone) % 12)) {
					return false;
				}
			}
			return true;
		};
	}

	// 1 - 0 frets
	// #1, b9 - 1 frets
	// 9 - 2 frets
	// #9, b3 - 3 frets
	// 3 - 4 frets
	// 11 - 5 frets
	// #11, b5 - 6 frets
	// 5 - 7 frets
	// #5, b13 - 8 frets
	// dim7, b7, 13 - 9 frets
	// #13, 7 - 10 frets
	// maj7 - 11 frets

	private static ChordNameGenerator nameWith(final String additions) {
		return new ChordNameGenerator() {

			@Override
			public String generate(final int root) {
				return soundToSimpleName(root, true) + additions;
			}
		};
	}

	private static List<ChordNameAdder> adders = asList(new ChordNameAdder(notes(1, 4, 7, 10), nameWith("7(b9)")), //
			new ChordNameAdder(notes(1, 4, 8, 10), nameWith("7(b9,b13)")), //
			new ChordNameAdder(notes(1, 4, 9, 10), nameWith("13(b9)")), //
			new ChordNameAdder(notes(1, 5, 10), nameWith("7" + sus + "(b9)")), //

			new ChordNameAdder(notes(2, 3, 7), nameWith(min + add + "9")), //
			new ChordNameAdder(notes(2, 3, 7, 10), nameWith(min + "9")), //
			new ChordNameAdder(notes(2, 3, 9), nameWith(min + "6(9)")), //
			new ChordNameAdder(notes(2, 4, 7), nameWith(add + "9")), //
			new ChordNameAdder(notes(2, 4, 7, 10), nameWith("9")), //
			new ChordNameAdder(notes(2, 4, 7, 11), nameWith(maj + "9")), //
			new ChordNameAdder(notes(2, 4, 9), nameWith("6(9)")), //
			new ChordNameAdder(notes(2, 4, 9, 10), nameWith("9(13)")), //
			new ChordNameAdder(notes(2, 4, 9, 11), nameWith(maj + "9(13)")), //
			new ChordNameAdder(notes(2, 5, 9, 10), nameWith("13" + sus)), //
			new ChordNameAdder(notes(2, 5, 10), nameWith("9" + sus)), //
			new ChordNameAdder(notes(2, 7), nameWith(sus + "2")), //

			new ChordNameAdder(notes(3), nameWith(min)), //
			new ChordNameAdder(notes(3, 4, 10), nameWith("7(#9)")), //
			new ChordNameAdder(notes(3, 5, 6, 10), nameWith(min + "11(b5)")), //
			new ChordNameAdder(notes(3, 5, 10), nameWith(min + "11")), //
			new ChordNameAdder(notes(3, 6, 8, 9), nameWith(dim + "b13")), //
			new ChordNameAdder(notes(3, 6, 9), nameWith(dim)), //
			new ChordNameAdder(notes(3, 6, 10), nameWith(min + "7(b5)")), //
			new ChordNameAdder(notes(3, 7), nameWith(min)), //
			new ChordNameAdder(notes(3, 7, 8, 10), nameWith(min + "7(b13)")), //
			new ChordNameAdder(notes(3, 7, 9), nameWith(min + "6")), //
			new ChordNameAdder(notes(3, 7, 9, 10), nameWith(min + "13")), //
			new ChordNameAdder(notes(3, 7, 11), nameWith(min + maj + "7")), //
			new ChordNameAdder(notes(3, 9, 10), nameWith(min + "13")), //
			new ChordNameAdder(notes(3, 10), nameWith(min + "7")), //

			new ChordNameAdder(notes(4), nameWith("3")), //
			new ChordNameAdder(notes(4, 6, 10), nameWith("7(#11)")), //
			new ChordNameAdder(notes(4, 6, 10), nameWith("7(b5)")), //
			new ChordNameAdder(notes(4, 6, 11), nameWith(maj + "7(#11)")), //
			new ChordNameAdder(notes(4, 7), nameWith("")), //
			new ChordNameAdder(notes(4, 7, 9), nameWith("6")), //
			new ChordNameAdder(notes(4, 7, 10), nameWith("7")), //
			new ChordNameAdder(notes(4, 7, 11), nameWith(maj + "7")), //
			new ChordNameAdder(notes(4, 8), nameWith(aug)), //
			new ChordNameAdder(notes(4, 8, 10), nameWith("7(#5)")), //
			new ChordNameAdder(notes(4, 8, 10), nameWith("7(b13)")), //
			new ChordNameAdder(notes(4, 8, 11), nameWith(maj + "7(#5)")), //
			new ChordNameAdder(notes(4, 9), nameWith("6")), //
			new ChordNameAdder(notes(4, 9, 10), nameWith("13")), //
			new ChordNameAdder(notes(4, 9, 11), nameWith(maj + "13")), //
			new ChordNameAdder(notes(4, 10), nameWith("7")), //

			new ChordNameAdder(notes(5, 7), nameWith(sus)), //
			new ChordNameAdder(notes(5, 7, 10), nameWith("7" + sus)), //

			new ChordNameAdder(notes(7), nameWith("5")));

	public static List<String> getSuggestedChordNames(final int root, final List<Integer> notes) {
		final List<String> foundNames = new ArrayList<>();
		adders.forEach(adder -> adder.add(root, notes, foundNames));
		return foundNames;
	}
}
