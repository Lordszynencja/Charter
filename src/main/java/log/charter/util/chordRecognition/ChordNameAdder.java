package log.charter.util.chordRecognition;

import static log.charter.util.chordRecognition.ChordNameSuggester.getToneName;

import log.charter.util.CollectionUtils.ArrayList2;

public class ChordNameAdder {
	public static String min = "m";
	public static String maj = "Δ";
	public static String dim = "°";
	public static String add = "add";
	public static String aug = "aug";
	public static String sus = "sus";

	static interface ChordTypeChecker {
		boolean check(final int root, final ArrayList2<Integer> notes);
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

	void add(final int root, final ArrayList2<Integer> notes, final ArrayList2<String> foundNames) {
		if (checker.check(root, notes)) {
			foundNames.add(nameGenerator.generate(root));
		}
	}

	private static ChordTypeChecker makeSimpleChecker(final int... requiredTones) {
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

	private static ArrayList2<ChordNameAdder> adders = new ArrayList2<>(
			new ChordNameAdder(makeSimpleChecker(1, 4, 7, 10), root -> getToneName(root) + "7(b9)"), //
			new ChordNameAdder(makeSimpleChecker(1, 4, 8, 10), root -> getToneName(root) + "7(b9,b13)"), //
			new ChordNameAdder(makeSimpleChecker(1, 4, 9, 10), root -> getToneName(root) + "13(b9)"), //
			new ChordNameAdder(makeSimpleChecker(1, 5, 10), root -> getToneName(root) + "7" + sus + "(b9)"), //

			new ChordNameAdder(makeSimpleChecker(2, 3, 7), root -> getToneName(root) + min + add + "9"), //
			new ChordNameAdder(makeSimpleChecker(2, 3, 7, 10), root -> getToneName(root) + min + "9"), //
			new ChordNameAdder(makeSimpleChecker(2, 3, 9), root -> getToneName(root) + min + "6(9)"), //
			new ChordNameAdder(makeSimpleChecker(2, 4, 7), root -> getToneName(root) + add + "9"), //
			new ChordNameAdder(makeSimpleChecker(2, 4, 7, 10), root -> getToneName(root) + "9"), //
			new ChordNameAdder(makeSimpleChecker(2, 4, 7, 11), root -> getToneName(root) + maj + "9"), //
			new ChordNameAdder(makeSimpleChecker(2, 4, 9), root -> getToneName(root) + "6(9)"), //
			new ChordNameAdder(makeSimpleChecker(2, 4, 9, 10), root -> getToneName(root) + "9(13)"), //
			new ChordNameAdder(makeSimpleChecker(2, 4, 9, 11), root -> getToneName(root) + maj + "9(13)"), //
			new ChordNameAdder(makeSimpleChecker(2, 5, 9, 10), root -> getToneName(root) + "13" + sus), //
			new ChordNameAdder(makeSimpleChecker(2, 5, 10), root -> getToneName(root) + "9" + sus), //
			new ChordNameAdder(makeSimpleChecker(2, 7), root -> getToneName(root) + sus + "2"), //

			new ChordNameAdder(makeSimpleChecker(3), root -> getToneName(root) + min), //
			new ChordNameAdder(makeSimpleChecker(3, 4, 10), root -> getToneName(root) + "7(#9)"), //
			new ChordNameAdder(makeSimpleChecker(3, 5, 6, 10), root -> getToneName(root) + min + "11(b5)"), //
			new ChordNameAdder(makeSimpleChecker(3, 5, 10), root -> getToneName(root) + min + "11"), //
			new ChordNameAdder(makeSimpleChecker(3, 6, 8, 9), root -> getToneName(root) + dim + "b13"), //
			new ChordNameAdder(makeSimpleChecker(3, 6, 9), root -> getToneName(root) + dim), //
			new ChordNameAdder(makeSimpleChecker(3, 6, 10), root -> getToneName(root) + min + "7(b5)"), //
			new ChordNameAdder(makeSimpleChecker(3, 7), root -> getToneName(root) + min), //
			new ChordNameAdder(makeSimpleChecker(3, 7, 8, 10), root -> getToneName(root) + min + "7(b13)"), //
			new ChordNameAdder(makeSimpleChecker(3, 7, 9), root -> getToneName(root) + min + "6"), //
			new ChordNameAdder(makeSimpleChecker(3, 7, 9, 10), root -> getToneName(root) + min + "13"), //
			new ChordNameAdder(makeSimpleChecker(3, 7, 11), root -> getToneName(root) + min + maj + "7"), //
			new ChordNameAdder(makeSimpleChecker(3, 9, 10), root -> getToneName(root) + min + "13"), //
			new ChordNameAdder(makeSimpleChecker(3, 10), root -> getToneName(root) + min + "7"), //

			new ChordNameAdder(makeSimpleChecker(4), root -> getToneName(root)), //
			new ChordNameAdder(makeSimpleChecker(4, 6, 10), root -> getToneName(root) + "7(#11)"), //
			new ChordNameAdder(makeSimpleChecker(4, 6, 10), root -> getToneName(root) + "7(b5)"), //
			new ChordNameAdder(makeSimpleChecker(4, 6, 11), root -> getToneName(root) + maj + "7(#11)"), //
			new ChordNameAdder(makeSimpleChecker(4, 7), root -> getToneName(root)), //
			new ChordNameAdder(makeSimpleChecker(4, 7, 11), root -> getToneName(root) + maj + "7"), //
			new ChordNameAdder(makeSimpleChecker(4, 8), root -> getToneName(root) + aug), //
			new ChordNameAdder(makeSimpleChecker(4, 8, 10), root -> getToneName(root) + "7(#5)"), //
			new ChordNameAdder(makeSimpleChecker(4, 8, 10), root -> getToneName(root) + "7(b13)"), //
			new ChordNameAdder(makeSimpleChecker(4, 8, 11), root -> getToneName(root) + maj + "7(#5)"), //
			new ChordNameAdder(makeSimpleChecker(4, 9), root -> getToneName(root) + "6"), //
			new ChordNameAdder(makeSimpleChecker(4, 9, 10), root -> getToneName(root) + "13"), //
			new ChordNameAdder(makeSimpleChecker(4, 9, 11), root -> getToneName(root) + maj + "13"), //
			new ChordNameAdder(makeSimpleChecker(4, 10), root -> getToneName(root) + "7"), //

			new ChordNameAdder(makeSimpleChecker(5, 7), root -> getToneName(root) + sus), //
			new ChordNameAdder(makeSimpleChecker(5, 7, 10), root -> getToneName(root) + "7" + sus), //

			new ChordNameAdder(makeSimpleChecker(7), root -> getToneName(root) + "5"));

	public static ArrayList2<String> getSuggestedChordNames(final int root, final ArrayList2<Integer> notes) {
		final ArrayList2<String> foundNames = new ArrayList2<>();
		adders.forEach(adder -> adder.add(root, notes, foundNames));
		return foundNames;
	}
}
