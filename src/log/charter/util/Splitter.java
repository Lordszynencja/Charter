package log.charter.util;

import java.util.regex.Pattern;

public class Splitter {
	public static final Pattern wordRegex = Pattern.compile("\\s+");
	public static final Pattern PLWordsRegex = Pattern.compile(
			"[^A-Za-z\u0105\u0104\u0119\u0118\u015b\u015a\u0107\u0106\u017a\u0179\u017c\u017b\u00f3\u00d3\u0142\u0141\u0144\u0143]+");
	public static final Pattern PLWordsRestRegex = Pattern.compile(
			"[A-Za-z\u0105\u0104\u0119\u0118\u015b\u015a\u0107\u0106\u017a\u0179\u017c\u017b\u00f3\u00d3\u0142\u0141\u0144\u0143]+");
	public static final Pattern numbersRegex = Pattern.compile("[^0-9]+");
	public static final Pattern endLineRegex = Pattern.compile("(\r\n|\n\r|\r|\n)");

	public static String[] split(final String s, final Pattern p) {
		return p.split(s);
	}

	public static Integer[] splitToNumbers(final String s) {
		final String[] numbersStrings = split(s, numbersRegex);
		final Integer[] numbers = new Integer[numbersStrings.length];
		for (int i = 0; i < numbersStrings.length; i++) {
			numbers[i] = Integer.valueOf(numbersStrings[i]);
		}
		return numbers;
	}
}
