package log.charter.util.chordRecognition.test;

import static log.charter.util.chordRecognition.ChordNameAdder.add;
import static log.charter.util.chordRecognition.ChordNameAdder.aug;
import static log.charter.util.chordRecognition.ChordNameAdder.dim;
import static log.charter.util.chordRecognition.ChordNameAdder.maj;
import static log.charter.util.chordRecognition.ChordNameAdder.min;
import static log.charter.util.chordRecognition.ChordNameAdder.sus;
import static log.charter.util.chordRecognition.ChordNameSuggester.getToneName;
import static org.junit.Assert.assertTrue;

import java.util.function.Function;

import org.junit.Test;

import log.charter.song.configs.Tuning;
import log.charter.song.configs.Tuning.TuningType;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashMap2;
import log.charter.util.chordRecognition.ChordNameSuggester;

public class ChordNameSuggesterTest {
	private static HashMap2<Integer, Integer> prepareFrets(final int root, final String frets) {
		final HashMap2<Integer, Integer> fretsMap = new HashMap2<>();
		for (int string = 0; string < frets.length(); string++) {
			if (frets.charAt(string) != '-') {
				fretsMap.put(string, root + frets.charAt(string) - '0');
			}
		}

		return fretsMap;
	}

	private void testFor(final TuningType tuningType, final Function<Integer, String> chordNameGenerator,
			final String frets) {
		final Tuning tuning = new Tuning(tuningType);
		for (int root = 0; root < 12; root++) {
			final HashMap2<Integer, Integer> templateFrets = prepareFrets(root, frets);

			final ArrayList2<String> result = ChordNameSuggester.suggestChordNames(tuning, templateFrets);
			final String expectedName = chordNameGenerator.apply(root + 8);
			assertTrue(tuning.tuningType.name + " " + frets + " failed to be read as " + expectedName + " for root "
					+ getToneName(root + 8) + ", names: " + result, result.contains(expectedName));
		}
	}

	@Test
	public void test_C() {
		final Function<Integer, String> chordNameGenerator = root -> getToneName(root);
		testFor(TuningType.E_STANDARD, chordNameGenerator, "-3-----");
		testFor(TuningType.E_STANDARD, chordNameGenerator, "-32---");
		testFor(TuningType.E_STANDARD, chordNameGenerator, "-32010");
	}

	@Test
	public void test_Cm() {
		final Function<Integer, String> chordNameGenerator = root -> getToneName(root) + min;
		testFor(TuningType.E_STANDARD, chordNameGenerator, "--1-1-");
		testFor(TuningType.E_STANDARD, chordNameGenerator, "--101-");
	}

	@Test
	public void test_C5() {
		final Function<Integer, String> chordNameGenerator = root -> getToneName(root) + "5";
		testFor(TuningType.E_STANDARD, chordNameGenerator, "-35---");
		testFor(TuningType.E_STANDARD, chordNameGenerator, "-355--");

		final Function<Integer, String> chordNameGenerator2 = root -> getToneName(root - 10) + "5";
		testFor(TuningType.E_DROP_D, chordNameGenerator2, "00----");
		testFor(TuningType.E_DROP_D, chordNameGenerator2, "000---");
		testFor(TuningType.E_DROP_C, chordNameGenerator2, "20----");
		testFor(TuningType.E_DROP_C, chordNameGenerator2, "200---");
	}

	@Test
	public void test_Cadd9() {
		final Function<Integer, String> chordNameGenerator = root -> getToneName(root) + add + "9";
		testFor(TuningType.E_STANDARD, chordNameGenerator, "-32030");
	}

	@Test
	public void test_Caug() {
		final Function<Integer, String> chordNameGenerator = root -> getToneName(root) + aug;
		testFor(TuningType.E_STANDARD, chordNameGenerator, "--2110");
	}

	@Test
	public void test_Cdim() {
		final Function<Integer, String> chordNameGenerator = root -> getToneName(root) + dim;
		testFor(TuningType.E_STANDARD, chordNameGenerator, "--1212");
	}

	@Test
	public void test_Csus() {
		final Function<Integer, String> chordNameGenerator = root -> getToneName(root) + sus;
		testFor(TuningType.E_STANDARD, chordNameGenerator, "-3301-");
	}

	@Test
	public void test_Csus2() {
		final Function<Integer, String> chordNameGenerator = root -> getToneName(root) + sus + "2";
		testFor(TuningType.E_STANDARD, chordNameGenerator, "-3-033");
	}

	@Test
	public void test_C7sus() {
		final Function<Integer, String> chordNameGenerator = root -> getToneName(root) + "7" + sus;
		testFor(TuningType.E_STANDARD, chordNameGenerator, "-35363");
	}

	@Test
	public void test_C9sus() {
		final Function<Integer, String> chordNameGenerator = root -> getToneName(root) + "9" + sus;
		testFor(TuningType.E_STANDARD, chordNameGenerator, "-3-331");
	}

	@Test
	public void test_C9susb9() {
		final Function<Integer, String> chordNameGenerator = root -> getToneName(root) + "7" + sus + "(b9)";
		testFor(TuningType.E_STANDARD, chordNameGenerator, "-3-321");
	}

	@Test
	public void test_C13sus() {
		final Function<Integer, String> chordNameGenerator = root -> getToneName(root) + "13" + sus;
		testFor(TuningType.E_STANDARD, chordNameGenerator, "-33335");
	}

	@Test
	public void test_Cmadd9() {
		final Function<Integer, String> chordNameGenerator = root -> getToneName(root) + min + add + "9";
		testFor(TuningType.E_STANDARD, chordNameGenerator, "-35743");
	}

	@Test
	public void test_Cm6() {
		final Function<Integer, String> chordNameGenerator = root -> getToneName(root) + min + "6";
		testFor(TuningType.E_STANDARD, chordNameGenerator, "-31213");
	}

	@Test
	public void test_Cm69() {
		final Function<Integer, String> chordNameGenerator = root -> getToneName(root) + min + "6(9)";
		testFor(TuningType.E_STANDARD, chordNameGenerator, "-3123-");
	}

	@Test
	public void test_Cm7() {
		final Function<Integer, String> chordNameGenerator = root -> getToneName(root) + min + "7";
		testFor(TuningType.E_STANDARD, chordNameGenerator, "-3131-");
	}

	@Test
	public void test_Cm9() {
		final Function<Integer, String> chordNameGenerator = root -> getToneName(root) + min + "9";
		testFor(TuningType.E_STANDARD, chordNameGenerator, "-31333");
	}

	@Test
	public void test_Cm11() {
		final Function<Integer, String> chordNameGenerator = root -> getToneName(root) + min + "11";
		testFor(TuningType.E_STANDARD, chordNameGenerator, "-31341");
	}

	@Test
	public void test_Cm13() {
		final Function<Integer, String> chordNameGenerator = root -> getToneName(root) + min + "13";
		testFor(TuningType.E_STANDARD, chordNameGenerator, "-35345");
	}

	@Test
	public void test_Cm7b13() {
		final Function<Integer, String> chordNameGenerator = root -> getToneName(root) + min + "7(b13)";
		testFor(TuningType.E_STANDARD, chordNameGenerator, "-36343");
	}

	@Test
	public void test_Cmmaj7() {
		final Function<Integer, String> chordNameGenerator = root -> getToneName(root) + min + maj + "7";
		testFor(TuningType.E_STANDARD, chordNameGenerator, "-3100-");
	}

	@Test
	public void test_Cmaj7() {
		final Function<Integer, String> chordNameGenerator = root -> getToneName(root) + maj + "7";
		testFor(TuningType.E_STANDARD, chordNameGenerator, "-32000");
	}

	@Test
	public void test_Cmaj9() {
		final Function<Integer, String> chordNameGenerator = root -> getToneName(root) + maj + "9";
		testFor(TuningType.E_STANDARD, chordNameGenerator, "-30000");
	}

	@Test
	public void test_Cmaj7sharp11() {
		final Function<Integer, String> chordNameGenerator = root -> getToneName(root) + maj + "7(#11)";
		testFor(TuningType.E_STANDARD, chordNameGenerator, "-3-452");
	}

	@Test
	public void test_Cmaj7sharp5() {
		final Function<Integer, String> chordNameGenerator = root -> getToneName(root) + maj + "7(#5)";
		testFor(TuningType.E_STANDARD, chordNameGenerator, "-32100");
	}

	@Test
	public void test_Cmaj13() {
		final Function<Integer, String> chordNameGenerator = root -> getToneName(root) + maj + "13";
		testFor(TuningType.E_STANDARD, chordNameGenerator, "-32200");
	}

	@Test
	public void test_Cmaj913() {
		final Function<Integer, String> chordNameGenerator = root -> getToneName(root) + maj + "9(13)";
		testFor(TuningType.E_STANDARD, chordNameGenerator, "8-9755");
	}

	@Test
	public void test_C7() {
		final Function<Integer, String> chordNameGenerator = root -> getToneName(root) + "7";
		testFor(TuningType.E_STANDARD, chordNameGenerator, "--2310");
	}

	@Test
	public void test_C7b9() {
		final Function<Integer, String> chordNameGenerator = root -> getToneName(root) + "7(b9)";
		testFor(TuningType.E_STANDARD, chordNameGenerator, "-32323");
	}

	@Test
	public void test_C9() {
		final Function<Integer, String> chordNameGenerator = root -> getToneName(root) + "9";
		testFor(TuningType.E_STANDARD, chordNameGenerator, "-32333");
	}

	@Test
	public void test_C7sharp9() {
		final Function<Integer, String> chordNameGenerator = root -> getToneName(root) + "7(#9)";
		testFor(TuningType.E_STANDARD, chordNameGenerator, "-3234-");
	}

	@Test
	public void test_C7sharp11() {
		final Function<Integer, String> chordNameGenerator = root -> getToneName(root) + "7(#11)";
		testFor(TuningType.E_STANDARD, chordNameGenerator, "--2312");
	}

	@Test
	public void test_C7b5() {
		final Function<Integer, String> chordNameGenerator = root -> getToneName(root) + "7(b5)";
		testFor(TuningType.E_STANDARD, chordNameGenerator, "--2312");
	}

	@Test
	public void test_C7sharp5() {
		final Function<Integer, String> chordNameGenerator = root -> getToneName(root) + "7(#5)";
		testFor(TuningType.E_STANDARD, chordNameGenerator, "-1211-");
	}

	@Test
	public void test_C7b13() {
		final Function<Integer, String> chordNameGenerator = root -> getToneName(root) + "7(b13)";
		testFor(TuningType.E_STANDARD, chordNameGenerator, "-1211-");
	}

	@Test
	public void test_C13() {
		final Function<Integer, String> chordNameGenerator = root -> getToneName(root) + "13";
		testFor(TuningType.E_STANDARD, chordNameGenerator, "-1221-");
	}

	@Test
	public void test_C913() {
		final Function<Integer, String> chordNameGenerator = root -> getToneName(root) + "9(13)";
		testFor(TuningType.E_STANDARD, chordNameGenerator, "-32335");
	}

	@Test
	public void test_C7b9b13() {
		final Function<Integer, String> chordNameGenerator = root -> getToneName(root) + "7(b9,b13)";
		testFor(TuningType.E_STANDARD, chordNameGenerator, "-32324");
	}

	@Test
	public void test_C13b9() {
		final Function<Integer, String> chordNameGenerator = root -> getToneName(root) + "13(b9)";
		testFor(TuningType.E_STANDARD, chordNameGenerator, "-32325");
	}

	@Test
	public void test_C6() {
		final Function<Integer, String> chordNameGenerator = root -> getToneName(root) + "6";
		testFor(TuningType.E_STANDARD, chordNameGenerator, "-32210");
	}

	@Test
	public void test_C69() {
		final Function<Integer, String> chordNameGenerator = root -> getToneName(root) + "6(9)";
		testFor(TuningType.E_STANDARD, chordNameGenerator, "-3-230");
	}

	@Test
	public void test_Cm7b5() {
		final Function<Integer, String> chordNameGenerator = root -> getToneName(root) + min + "7(b5)";
		testFor(TuningType.E_STANDARD, chordNameGenerator, "--1312");
	}

	@Test
	public void test_Cm11b5() {
		final Function<Integer, String> chordNameGenerator = root -> getToneName(root) + min + "11(b5)";
		testFor(TuningType.E_STANDARD, chordNameGenerator, "-34341");
	}

	@Test
	public void test_Cdimb13() {
		final Function<Integer, String> chordNameGenerator = root -> getToneName(root) + dim + "b13";
		testFor(TuningType.E_STANDARD, chordNameGenerator, "-34244");
	}
}
