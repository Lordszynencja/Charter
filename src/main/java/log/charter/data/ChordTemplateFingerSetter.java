package log.charter.data;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.List;

import log.charter.data.config.values.InstrumentConfig;
import log.charter.data.song.ChordTemplate;

public class ChordTemplateFingerSetter {

	private static class Shape {
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

	private static final List<Shape> recognizedShapes = generateRecognizedShapes();

	private static List<Shape> generateRecognizedShapes() {//
		return List.of(//
				new Shape(new int[] { 0 }, new Integer[] {}), //

				new Shape(new int[] { 0, 0 }, new Integer[] {}), //
				new Shape(new int[] { 0, 1 }, new Integer[] {}), //

				new Shape(new int[] { 0, -1, 0 }, new Integer[] { 1, null, 2 }), //
				new Shape(new int[] { 0, 0, 0 }, new Integer[] { 1, 1, 1 }), //
				new Shape(new int[] { 0, -1, 2 }, new Integer[] { 1, null, 3 }), //
				new Shape(new int[] { 0, 1, 2 }, new Integer[] { 1, 2, 3 }), //
				new Shape(new int[] { 0, 1, 0 }, new Integer[] { 1, 3, 2 }), //
				new Shape(new int[] { 0, 1, 1 }, new Integer[] { 1, 3, 4 }), //
				new Shape(new int[] { 1, -1, 0 }, new Integer[] { 2, null, 1 }), //
				new Shape(new int[] { 1, 0, 1 }, new Integer[] { 2, 1, 3 }), //
				new Shape(new int[] { 1, 1, 0 }, new Integer[] { 2, 3, 1 }), //
				new Shape(new int[] { 1, 2, 0 }, new Integer[] { 2, 3, 1 }), //
				new Shape(new int[] { 2, 2, 0 }, new Integer[] { 3, 4, 1 }), //

				new Shape(new int[] { 0, 0, 0, 0 }, new Integer[] { 1, 1, 1, 1 }), //
				new Shape(new int[] { 0, 0, 0, 2 }, new Integer[] { 1, 1, 1, 3 }), //
				new Shape(new int[] { 0, 2, 3, 0 }, new Integer[] { 1, 3, 4, 1 }), //
				new Shape(new int[] { 1, 0, 1, 0 }, new Integer[] { 2, 1, 3, 4 }), //
				new Shape(new int[] { 1, 0, 1, 1 }, new Integer[] { 2, 1, 3, 4 }), //
				new Shape(new int[] { 1, 0, 1, 2 }, new Integer[] { 2, 1, 3, 4 }), //
				new Shape(new int[] { 1, 0, 2, 1 }, new Integer[] { 2, 1, 4, 3 }), //
				new Shape(new int[] { 2, 0, 2, 2 }, new Integer[] { 2, 1, 3, 4 }), //
				new Shape(new int[] { 2, 1, -1, 0 }, new Integer[] { 3, 2, null, 1 }), //
				new Shape(new int[] { 2, 2, 0, 0 }, new Integer[] { 3, 4, 1, 1 }), //
				new Shape(new int[] { 2, 2, 1, 0 }, new Integer[] { 3, 4, 2, 1 }), //
				new Shape(new int[] { 2, 2, 2, 0 }, new Integer[] { 2, 3, 4, 1 }), //
				new Shape(new int[] { 2, 2, 3, 0 }, new Integer[] { 2, 3, 4, 1 }), //

				new Shape(new int[] { 1, 0, -1, 1, 1 }, new Integer[] { 2, 1, null, 3, 4 }), //
				new Shape(new int[] { 1, 0, 1, -1, 1 }, new Integer[] { 2, 1, 3, null, 4 }), //
				new Shape(new int[] { 2, 0, 1, 0, 0 }, new Integer[] { 3, 1, 2, 1, 1 }), //
				new Shape(new int[] { 2, 0, 2, 0, 0 }, new Integer[] { 3, 1, 4, 1, 1 }), //
				new Shape(new int[] { 2, 2, 0, 0, 0 }, new Integer[] { 3, 4, 1, 1, 1 }), //
				new Shape(new int[] { 2, 2, 1, 0, 0 }, new Integer[] { 3, 4, 2, 1, 1 }), //
				new Shape(new int[] { 2, 2, 2, 0, 0 }, new Integer[] { 2, 3, 4, 1, 1 }), //
				new Shape(new int[] { 2, 2, 3, 0, 0 }, new Integer[] { 2, 3, 4, 1, 1 }), //
				new Shape(new int[] { 3, 2, 0, 1, 0 }, new Integer[] { 4, 3, 1, 2, 1 }), //

				new Shape(new int[] { 0, 0, 2, 2, 0, 0 }, new Integer[] { 1, 1, 3, 4, 1, 1 }), //
				new Shape(new int[] { 0, 0, 2, 2, 1, 0 }, new Integer[] { 1, 1, 3, 4, 2, 1 }), //
				new Shape(new int[] { 0, 0, 2, 2, 2, 0 }, new Integer[] { 1, 1, 2, 3, 4, 1 }), //
				new Shape(new int[] { 0, 0, 2, 2, 3, 0 }, new Integer[] { 1, 1, 2, 3, 4, 1 }), //
				new Shape(new int[] { 0, 2, 0, 1, 0, 0 }, new Integer[] { 1, 3, 1, 2, 1, 1 }), //
				new Shape(new int[] { 0, 2, 0, 2, 0, 0 }, new Integer[] { 1, 3, 1, 4, 1, 1 }), //
				new Shape(new int[] { 0, 2, 2, 0, 0, 0 }, new Integer[] { 1, 3, 4, 1, 1, 1 }), //
				new Shape(new int[] { 0, 2, 2, 1, 0, 0 }, new Integer[] { 1, 3, 4, 2, 1, 1 }), //
				new Shape(new int[] { 0, 2, 2, 2, 0, 0 }, new Integer[] { 1, 2, 3, 4, 1, 1 }), //
				new Shape(new int[] { 0, 2, 2, 3, 0, 0 }, new Integer[] { 1, 2, 3, 4, 1, 1 }), //
				new Shape(new int[] { 0, 3, 2, 0, 1, 0 }, new Integer[] { 1, 4, 3, 1, 2, 1 }), //
				new Shape(new int[] { 1, 0, -1, -1, -1, 1 }, new Integer[] { 2, 1, null, null, null, 4 }), //
				new Shape(new int[] { 1, 0, -1, -1, 1, 1 }, new Integer[] { 2, 1, null, null, 3, 4 }));
	}

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
		for (final Shape shape : recognizedShapes) {
			if (shape.is(template, frets)) {
				return shape;
			}
		}

		return null;
	}
}
