package log.charter.gui.components;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.pow;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.centeredTextWithBackground;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.filledDiamond;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.lineHorizontal;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.lineVertical;
import static log.charter.util.Utils.getStringPosition;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JComponent;

import log.charter.data.ChartData;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShapeList;
import log.charter.song.ChordTemplate;
import log.charter.util.IntRange;
import log.charter.util.Position2D;

public class ChordTemplatePreview extends JComponent {
	private static final long serialVersionUID = 1L;
	private static final double fretsProportion = pow(2, -1.0 / 12);
	private static final int fretStart = 20;

	private final ChartData data;
	private final ChordTemplate chordTemplate;

	public ChordTemplatePreview(final ChartData data, final ChordTemplate chordTemplate) {
		super();
		this.data = data;
		this.chordTemplate = chordTemplate;
	}

	private static class FretPosition {
		public final int fret;
		public final int position;
		public final int length;

		private FretPosition(final int fret, final int position, final int length) {
			this.fret = fret;
			this.position = position;
			this.length = length;
		}

	}

	private IntRange getFretsRange() {
		int min = 28;
		int max = 0;
		for (final int fret : chordTemplate.frets.values()) {
			if (fret != 0) {
				min = min(min, fret);
				max = max(max, fret);
			}
		}
		if (min == 28 && max == 0) {
			return new IntRange(0, 3);
		}

		if (min > 0) {
			min--;
		}
		if (min > 0) {
			min--;
		}
		if (max < 28) {
			max++;
		}
		while (max - min < 3) {
			if (min > 0) {
				min--;
			}
			if (max < 28) {
				max++;
			}
		}

		return new IntRange(min, max);
	}

	private FretPosition[] getFretPositions() {
		final IntRange fretsRange = getFretsRange();
		final int fretsAmount = fretsRange.max - fretsRange.min + 1;

		double scaleLength;
		final double[] fretScales = new double[fretsAmount];
		if (fretsRange.min == 0) {
			scaleLength = 1;
			fretScales[0] = 1;
		} else {
			scaleLength = 0.5;
			fretScales[0] = 0.5;
		}

		double fretScale = 1;
		for (int i = 1; i < fretsAmount; i++) {
			fretScales[i] = fretScale;
			scaleLength += fretScale;
			fretScale *= fretsProportion;
		}
		scaleLength += 0.5 * fretScale;

		final FretPosition[] fretPositions = new FretPosition[fretsAmount];
		final double multiplier = getWidth() / scaleLength;
		int fretPosition = 0;
		for (int i = 0; i < fretsAmount; i++) {
			final int fretLength = (int) (fretScales[i] * multiplier);
			fretPosition += fretLength;
			fretPositions[i] = new FretPosition(fretsRange.min + i, fretPosition, fretLength);
		}

		return fretPositions;
	}

	private void drawBackground(final Graphics g) {
		g.setColor(ColorLabel.BASE_BG_1.color());
		g.fillRect(0, 0, getWidth(), getHeight());
	}

	private void drawFrets(final Graphics g, final FretPosition[] fretPositions) {
		final DrawableShapeList frets = new DrawableShapeList();

		for (final FretPosition fretPosition : fretPositions) {
			frets.add(lineVertical(fretPosition.position - 1, fretStart, getHeight(), ColorLabel.BASE_BG_2.color()));
			frets.add(lineVertical(fretPosition.position, fretStart, getHeight(), ColorLabel.BASE_BG_4.color()));
			frets.add(lineVertical(fretPosition.position + 1, fretStart, getHeight(), ColorLabel.BASE_BG_2.color()));
			frets.add(centeredTextWithBackground(new Position2D(fretPosition.position, 10), fretPosition.fret + "",
					null, ColorLabel.BASE_DARK_TEXT.color()));
		}

		frets.draw(g);
	}

	private void drawStrings(final Graphics g) {
		final int strings = data.getCurrentArrangement().tuning.strings;
		final int width = getWidth();
		final DrawableShapeList stringLines = new DrawableShapeList();

		final int stringSpace = (getHeight() - fretStart) / 6;
		int y = fretStart + stringSpace / 2;
		for (int i = 0; i < strings; i++) {
			final int stringNumber = getStringPosition(i, strings);
			final Color color = ColorLabel.valueOf("LANE_" + stringNumber).color();
			stringLines.add(lineHorizontal(0, width, y, color));
			final Color color2 = ColorLabel.valueOf("LANE_BRIGHT_" + stringNumber).color();
			stringLines.add(lineHorizontal(0, width, y + 1, color2));
			y += stringSpace;
		}

		stringLines.draw(g);
	}

	private void drawFretPressMarks(final Graphics g, final FretPosition[] fretPositions) {
		final int strings = data.getCurrentArrangement().tuning.strings;
		final int baseFret = fretPositions[0].fret;
		final DrawableShapeList pressMarks = new DrawableShapeList();

		final int stringSpace = (getHeight() - fretStart) / 6;
		int y = fretStart + stringSpace / 2;

		for (int i = 0; i < strings; i++) {
			final int stringNumber = getStringPosition(i, strings);
			final Integer fret = chordTemplate.frets.get(stringNumber);
			if (fret == null) {
				y += stringSpace;
				continue;
			}
			if (fret == 0) {
				for (int j = 0; j < 8; j++) {
					final Color color = ColorLabel.valueOf((j < 2 || j >= 6 ? "LANE_BRIGHT_" : "NOTE_") + stringNumber)
							.color();
					pressMarks.add(lineHorizontal(0, getWidth(), y - 3 + j, color));
				}
				y += stringSpace;
				continue;
			}

			final FretPosition fretPosition = fretPositions[fret - baseFret];
			final Position2D position = new Position2D(fretPosition.position - fretPosition.length / 2, y);
			pressMarks.add(filledDiamond(position.move(1, 0), 10, ColorLabel.valueOf("NOTE_" + stringNumber).color()));

			final Integer finger = chordTemplate.fingers.get(stringNumber);
			final String fingerText = finger == null ? "" : finger == 0 ? "T" : finger.toString();
			pressMarks.add(centeredTextWithBackground(position, fingerText, null, ColorLabel.BASE_TEXT.color()));
			y += stringSpace;
		}

		pressMarks.draw(g);
	}

	@Override
	protected void paintComponent(final Graphics g) {
		final FretPosition[] fretPositions = getFretPositions();
		drawBackground(g);
		drawFrets(g, fretPositions);
		drawStrings(g);
		drawFretPressMarks(g, fretPositions);
	}
}
