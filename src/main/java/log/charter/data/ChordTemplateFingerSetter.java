package log.charter.data;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.List;

import log.charter.data.config.values.InstrumentConfig;
import log.charter.data.song.ChordTemplate;

public class ChordTemplateFingerSetter {

	private static class Shape {
		/**
		 * -1 means string is empty or not used other values mean difference from lowest
		 * fret
		 */
		private final int[] requiredFrets;
		private final Integer[] fingers;

		public Shape(final int[] requiredFrets, final Integer[] fingers) {
			this.requiredFrets = requiredFrets;
			this.fingers = fingers;
		}

		public boolean is(final ChordTemplate template, final int[] shape) {
			if (shape.length > requiredFrets.length) {
				return false;
			}

			for (int i = 0; i < shape.length; i++) {
				if (shape[i] != requiredFrets[i]) {
					return false;
				}
			}

			return true;
		}

		public void apply(final ChordTemplate template, final int fromString) {
			for (int i = fromString; i < fromString + fingers.length; i++) {
				if (template.frets.getOrDefault(i, 0) != 0) {
					if (fingers[i - fromString] == null) {
						template.fingers.remove(i);
					} else {
						template.fingers.put(i, fingers[i - fromString]);
					}
				}
			}
		}
	}

	private static int[] i(final int... v) {
		return v;
	}

	private static Integer[] I(final Integer... v) {
		return v;
	}

	private static List<Shape> generate2StringChordFingerings() {
		final List<Shape> shapes = new ArrayList<>();

		for (int i = 0; i < 7; i++) {
			shapes.add(new Shape(i(0, i), I(1, min(4, i + 1))));
			shapes.add(new Shape(i(0, -1, i), I(1, null, min(4, i + 2))));
		}
		for (int i = 1; i < 5; i++) {
			shapes.add(new Shape(i(i, -1, 0), I(min(4, i + 1), null, 1)));
		}

		for (int i = 0; i < 6; i++) {
			shapes.add(new Shape(i(0, -1, -1, i), I(1, null, null, min(4, i + 2))));
		}
		for (int i = 1; i < 4; i++) {
			shapes.add(new Shape(i(i, -1, -1, 0), I(min(4, i + 2), null, null, 1)));
		}

		for (int i = 0; i < 5; i++) {
			shapes.add(new Shape(i(0, -1, -1, -1, i), I(1, null, null, null, min(4, i + 2))));
		}
		for (int i = 1; i < 4; i++) {
			shapes.add(new Shape(i(i, -1, -1, -1, 0), I(min(4, i + 2), null, null, null, 1)));
		}

		for (int i = 0; i < 5; i++) {
			shapes.add(new Shape(i(0, -1, -1, -1, i), I(1, null, null, null, min(4, i / 2 + 3))));
		}
		for (int i = 1; i < 4; i++) {
			shapes.add(new Shape(i(i, -1, -1, -1, 0), I(min(4, i + 2), null, null, null, 1)));
		}

		for (int i = 0; i < 5; i++) {
			shapes.add(new Shape(i(0, -1, -1, -1, -1, i), I(1, null, null, null, null, min(4, i / 2 + 3))));
		}
		for (int i = 1; i < 4; i++) {
			shapes.add(new Shape(i(i, -1, -1, -1, -1, 0), I(min(4, i + 2), null, null, null, null, 1)));
		}

		return shapes;
	}

	private static List<Shape> generate3StringChordFingerings() {
		final List<Shape> shapes = new ArrayList<>();

		for (int i = 0; i < 5; i++) {
			shapes.add(new Shape(i(0, 0, i), I(1, 1, min(4, i + 1))));
		}

		shapes.add(new Shape(i(0, 1, 0), I(1, 3, 2)));
		shapes.add(new Shape(i(0, 1, 1), I(1, 2, 3)));
		shapes.add(new Shape(i(0, 1, 2), I(1, 2, 3)));
		shapes.add(new Shape(i(0, 1, 3), I(1, 2, 4)));
		shapes.add(new Shape(i(0, 1, 4), I(1, 2, 4)));

		shapes.add(new Shape(i(0, 2, 0), I(1, 3, 1)));
		shapes.add(new Shape(i(0, 2, 1), I(1, 3, 2)));
		shapes.add(new Shape(i(0, 2, 2), I(1, 3, 4)));
		shapes.add(new Shape(i(0, 2, 3), I(1, 3, 4)));
		shapes.add(new Shape(i(0, 2, 4), I(1, 3, 4)));

		shapes.add(new Shape(i(0, 3, 0), I(1, 4, 1)));
		shapes.add(new Shape(i(0, 3, 1), I(1, 4, 2)));
		shapes.add(new Shape(i(0, 3, 2), I(1, 4, 3)));
		shapes.add(new Shape(i(0, 3, 3), I(1, 4, 4)));

		shapes.add(new Shape(i(1, 0, 0), I(2, 1, 1)));
		shapes.add(new Shape(i(1, 0, 1), I(2, 1, 3)));
		shapes.add(new Shape(i(1, 0, 2), I(2, 1, 3)));
		shapes.add(new Shape(i(1, 0, 3), I(2, 1, 4)));
		shapes.add(new Shape(i(1, 0, 4), I(2, 1, 4)));

		shapes.add(new Shape(i(1, 1, 0), I(2, 3, 1)));
		shapes.add(new Shape(i(1, 2, 0), I(2, 3, 1)));
		shapes.add(new Shape(i(1, 3, 0), I(2, 4, 1)));
		shapes.add(new Shape(i(1, 4, 0), I(2, 4, 1)));

		shapes.add(new Shape(i(2, 2, 0), I(3, 4, 1)));
		shapes.add(new Shape(i(2, 3, 0), I(3, 4, 1)));
		shapes.add(new Shape(i(2, 4, 0), I(2, 4, 1)));

		return shapes;
	}

	private static List<Shape> generate4StringChordFingerings() {
		return List.of(//
				new Shape(i(0, 0, 0, 0), I(1, 1, 1, 1)), //
				new Shape(i(0, 0, 0, 2), I(1, 1, 1, 3)), //
				new Shape(i(0, 2, 3, 0), I(1, 3, 4, 1)), //
				new Shape(i(1, 0, 1, 0), I(2, 1, 3, 4)), //
				new Shape(i(1, 0, 1, 1), I(2, 1, 3, 4)), //
				new Shape(i(1, 0, 1, 2), I(2, 1, 3, 4)), //
				new Shape(i(1, 0, 2, 1), I(2, 1, 4, 3)), //
				new Shape(i(2, 0, 2, 2), I(2, 1, 3, 4)), //
				new Shape(i(2, 1, -1, 0), I(3, 2, null, 1)), //
				new Shape(i(2, 2, 0, 0), I(3, 4, 1, 1)), //
				new Shape(i(2, 2, 1, 0), I(3, 4, 2, 1)), //
				new Shape(i(2, 2, 2, 0), I(2, 3, 4, 1)), //
				new Shape(i(2, 2, 3, 0), I(2, 3, 4, 1)));
	}

	private static List<Shape> generate5StringChordFingerings() {
		return List.of(//
				new Shape(i(0, 0, 0, 0, 0), I(1, 1, 1, 1, 1)), //
				new Shape(i(1, 0, -1, 1, 1), I(2, 1, null, 3, 4)), //
				new Shape(i(1, 0, 1, -1, 1), I(2, 1, 3, null, 4)), //
				new Shape(i(2, 0, 1, 0, 0), I(3, 1, 2, 1, 1)), //
				new Shape(i(2, 0, 2, 0, 0), I(3, 1, 4, 1, 1)), //
				new Shape(i(2, 2, 0, 0, 0), I(3, 4, 1, 1, 1)), //
				new Shape(i(2, 2, 1, 0, 0), I(3, 4, 2, 1, 1)), //
				new Shape(i(2, 2, 2, 0, 0), I(2, 3, 4, 1, 1)), //
				new Shape(i(2, 2, 3, 0, 0), I(2, 3, 4, 1, 1)), //
				new Shape(i(3, 2, 0, 1, 0), I(4, 3, 1, 2, 1)));
	}

	private static List<Shape> generate6StringChordFingerings() {
		return List.of(//
				new Shape(i(0, 0, 0, 0, 0, 0), I(1, 1, 1, 1, 1, 1)), //
				new Shape(i(0, 0, 2, 2, 0, 0), I(1, 1, 3, 4, 1, 1)), //
				new Shape(i(0, 0, 2, 2, 1, 0), I(1, 1, 3, 4, 2, 1)), //
				new Shape(i(0, 0, 2, 2, 2, 0), I(1, 1, 2, 3, 4, 1)), //
				new Shape(i(0, 0, 2, 2, 3, 0), I(1, 1, 2, 3, 4, 1)), //
				new Shape(i(0, 2, 0, 1, 0, 0), I(1, 3, 1, 2, 1, 1)), //
				new Shape(i(0, 2, 0, 2, 0, 0), I(1, 3, 1, 4, 1, 1)), //
				new Shape(i(0, 2, 2, 0, 0, 0), I(1, 3, 4, 1, 1, 1)), //
				new Shape(i(0, 2, 2, 1, 0, 0), I(1, 3, 4, 2, 1, 1)), //
				new Shape(i(0, 2, 2, 2, 0, 0), I(1, 2, 3, 4, 1, 1)), //
				new Shape(i(0, 2, 2, 3, 0, 0), I(1, 2, 3, 4, 1, 1)), //
				new Shape(i(0, 3, 2, 0, 1, 0), I(1, 4, 3, 1, 2, 1)), //
				new Shape(i(1, 0, -1, -1, -1, 1), I(2, 1, null, null, null, 4)), //
				new Shape(i(1, 0, -1, -1, 1, 1), I(2, 1, null, null, 3, 4)));
	}

	private static List<Shape> getAllShapes() {
		final List<Shape> shapes = new ArrayList<>();
		shapes.addAll(generate2StringChordFingerings());
		shapes.addAll(generate3StringChordFingerings());
		shapes.addAll(generate4StringChordFingerings());
		shapes.addAll(generate5StringChordFingerings());
		shapes.addAll(generate6StringChordFingerings());

		return shapes;
	}

	private static List<List<Shape>> sort(final List<Shape> shapes) {
		final List<List<Shape>> sortedShapes = new ArrayList<>();
		for (int i = 2; i <= 6; i++) {
			sortedShapes.add(new ArrayList<>());
		}
		for (final Shape shape : shapes) {
			sortedShapes.get(shape.requiredFrets.length - 2).add(shape);
		}

		return sortedShapes;
	}

	private static final List<List<Shape>> recognizedShapes = sort(getAllShapes());

	private static void cleanTemplate(final ChordTemplate template) {
		for (int i = 0; i < InstrumentConfig.maxStrings; i++) {
			if (template.frets.getOrDefault(i, 0) == 0) {
				template.fingers.remove(i);
			}
		}
	}

	public static void setSuggestedFingers(final ChordTemplate template) {
		cleanTemplate(template);
		final ChordTemplateFingerSetter setter = new ChordTemplateFingerSetter(template);

		if (!setter.calculateLowestStringAndFret()) {
			return;
		}

		final Shape recognizedShape = setter.recognizeShape();
		if (recognizedShape != null) {
			recognizedShape.apply(template, setter.lowestNonzeroString);
		}
	}

	private final ChordTemplate template;

	private int lowestNonzeroString = InstrumentConfig.maxStrings;
	private int highestNonzeroString = -1;
	private int lowestString = InstrumentConfig.maxStrings;
	private int highestString = -1;
	private int lowestNonzeroFret = InstrumentConfig.frets + 1;

	private ChordTemplateFingerSetter(final ChordTemplate template) {
		this.template = template;
	}

	/**
	 *
	 * @return true if there are non-zero-fret strings
	 */
	private boolean calculateLowestStringAndFret() {
		for (final int string : template.frets.keySet()) {
			lowestString = min(string, lowestString);
			highestString = max(string, highestString);

			if (template.frets.get(string) != 0) {
				lowestNonzeroString = min(string, lowestNonzeroString);
				highestNonzeroString = max(string, highestNonzeroString);
				lowestNonzeroFret = min(template.frets.get(string), lowestNonzeroFret);
			}
		}

		return highestNonzeroString != -1;
	}

	private int[] calculateShape() {
		final int[] frets = new int[highestNonzeroString - lowestNonzeroString + 1];
		for (int i = 0; i < frets.length; i++) {
			frets[i] = template.frets.getOrDefault(lowestNonzeroString + i, -1);
			if (frets[i] > 0) {
				frets[i] -= lowestNonzeroFret;
			} else {
				frets[i] = -1;
			}
		}

		return frets;
	}

	private Shape recognizeShape() {
		final int[] frets = calculateShape();
		if (frets.length < 2 || frets.length > 6) {
			return null;
		}

		for (final Shape shape : recognizedShapes.get(frets.length - 2)) {
			if (shape.is(template, frets)) {
				return shape;
			}
		}

		return null;
	}
}
