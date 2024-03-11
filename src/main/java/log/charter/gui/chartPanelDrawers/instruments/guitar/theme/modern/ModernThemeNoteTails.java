package log.charter.gui.chartPanelDrawers.instruments.guitar.theme.modern;

import static java.lang.Math.sin;
import static log.charter.data.config.Config.maxStrings;
import static log.charter.data.config.GraphicalConfig.noteHeight;
import static log.charter.gui.ChartPanelColors.getStringBasedColor;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.tailHeight;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.centeredTextWithBackground;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.clippedShapes;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.filledPolygon;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.filledRectangle;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.filledTriangle;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.lineVertical;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.strokedRectangle;
import static log.charter.util.Utils.stringId;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import log.charter.data.config.Zoom;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.ChartPanelColors.StringColorLabelType;
import log.charter.gui.chartPanelDrawers.data.EditorNoteDrawingData;
import log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapePositionWithSize;
import log.charter.gui.chartPanelDrawers.drawableShapes.StrokedTriangle;
import log.charter.gui.chartPanelDrawers.instruments.guitar.theme.HighwayDrawData;
import log.charter.util.data.IntRange;
import log.charter.util.data.Position2D;

public class ModernThemeNoteTails {
	private static final Color[] noteTailColors = new Color[maxStrings];
	private static Font slideFretFont = new Font(Font.SANS_SERIF, Font.BOLD, noteHeight / 2);

	public static void reloadGraphics() {
		for (int i = 0; i < maxStrings; i++) {
			noteTailColors[stringId(i, maxStrings)] = getStringBasedColor(StringColorLabelType.NOTE_TAIL, i,
					maxStrings);
		}

		slideFretFont = new Font(Font.SANS_SERIF, Font.BOLD, noteHeight / 2);
	}

	private final HighwayDrawData data;

	public ModernThemeNoteTails(final HighwayDrawData data) {
		this.data = data;
	}

	private IntRange getDefaultTailTopBottom(final int y) {
		final int topY = y - tailHeight / 3;
		final int bottomY = y + tailHeight / 3 + 1;
		return new IntRange(topY, bottomY);
	}

	private void addSlideBox(final Position2D a, final Position2D b, final Position2D c, final ColorLabel color) {
		data.noteTails.add(new StrokedTriangle(a, b, c, color));
	}

	private void addSlideCommon(final EditorNoteDrawingData note, final int y, final Color backgroundColor,
			final Color fretColor) {
		IntRange topBottom = getDefaultTailTopBottom(y);
		topBottom = new IntRange(topBottom.min - 1, topBottom.max);
		final Position2D a = new Position2D(note.x, topBottom.min);
		final Position2D b = new Position2D(note.x, topBottom.max);
		final int tailEndY = note.slideTo < note.fretNumber ? topBottom.max : topBottom.min;
		final Position2D c = new Position2D(note.x + note.length, tailEndY);
		final int tailEndFretTextY = note.slideTo < note.fretNumber ? topBottom.max + noteHeight / 3
				: topBottom.min - noteHeight / 3;
		final Position2D fretTextPosition = new Position2D(note.x + note.length, tailEndFretTextY);
		final Color color = noteTailColors[stringId(note.string, data.strings)];

		if (note.vibrato || note.tremolo) {
			if (note.vibrato) {
				final List<DrawableShape> shapes = new ArrayList<>();
				final int vibratoSpeed = (int) (Zoom.zoom * 100);
				final int vibratoLineHeight = tailHeight / 2;
				final int vibratoAmplitude = tailHeight - vibratoLineHeight - 1;
				final int vibratoOffset = (vibratoAmplitude - tailHeight) / 2;
				for (int i = 0; i < note.length + 2; i++) {
					final int segmentY = y
							+ (int) (vibratoOffset - vibratoAmplitude * sin(i * Math.PI / vibratoSpeed) / 2);

					shapes.add(lineVertical(note.x + i, segmentY, segmentY + vibratoLineHeight, color));
				}
				data.noteTails.add(clippedShapes(
						new ShapePositionWithSize(note.x, y - tailHeight / 2, note.length, tailHeight), shapes));
			}
			if (note.tremolo) {
				int x = note.x + 40;
				final int totalHeight = topBottom.max - topBottom.min;
				int middleY = y;
				int height = totalHeight;
				while (x < note.x + note.length) {
					final int distance = x - note.x;
					final double lengthRatio1 = (distance - 20) * 1.0 / note.length;
					final double lengthRatio2 = distance * 1.0 / note.length;
					final int h1 = (int) (totalHeight * (1 - lengthRatio1));
					final int h2 = (int) (totalHeight * (1 - lengthRatio2));
					final int middleY1 = (int) (y * (1 - lengthRatio1) + tailEndY * lengthRatio1);
					final int middleY2 = (int) (y * (1 - lengthRatio2) + tailEndY * lengthRatio2);

					data.noteTails.add(filledPolygon(color, //
							new Position2D(x - 40, middleY - 2 * height / 3), //
							new Position2D(x - 20, middleY1 - h1 / 3), //
							new Position2D(x, middleY2 - 2 * h2 / 3), //
							new Position2D(x, middleY2 + h2 / 3), //
							new Position2D(x - 20, middleY1 + 2 * h1 / 3), //
							new Position2D(x - 40, middleY + height / 3)));

					middleY = middleY2;
					height = h2;
					x += 40;
				}

				data.noteTails.add(filledTriangle(new Position2D(x - 40, middleY - 2 * height / 3),
						new Position2D(x - 40, middleY + height / 3), new Position2D(note.x + note.length, tailEndY),
						color));
			}
		} else {
			data.noteTails.add(filledTriangle(a, b, c, color));
		}

		data.slideFrets.add(centeredTextWithBackground(fretTextPosition, slideFretFont, note.slideTo + "", color.darker().darker().darker(),
				backgroundColor, noteTailColors[stringId(note.string, data.strings)]));

		if (note.highlighted) {
			addSlideBox(a, b, c, ColorLabel.HIGHLIGHT);
		} else if (note.selected) {
			addSlideBox(a, b, c, ColorLabel.SELECT);
		}
	}

	private void addSlideNoteTailShape(final EditorNoteDrawingData note, final int y) {
		addSlideCommon(note, y, ColorLabel.SLIDE_NORMAL_FRET_BG.color(), ColorLabel.SLIDE_NORMAL_FRET_TEXT.color());
	}

	private void addUnpitchedSlideNoteTailShape(final EditorNoteDrawingData note, final int y) {
		addSlideCommon(note, y, ColorLabel.SLIDE_UNPITCHED_FRET_BG.color(),
				ColorLabel.SLIDE_UNPITCHED_FRET_TEXT.color());
	}

	private void addTailBox(final int x, final int length, final int y, final ColorLabel color) {
		final IntRange topBottom = getDefaultTailTopBottom(y);

		final ShapePositionWithSize position = new ShapePositionWithSize(x, topBottom.min - 1, length,
				topBottom.max - topBottom.min + 1);
		data.noteTails.add(strokedRectangle(position, color));
	}

	private void addNormalNoteTailShape(final EditorNoteDrawingData note, final int y) {
		final IntRange topBottom = getDefaultTailTopBottom(y);
		final int x = note.x - 1;
		final int length = note.length + 1;
		final Color color = noteTailColors[stringId(note.string, data.strings)];

		if (note.vibrato || note.tremolo) {
			if (note.vibrato && note.tremolo) {
				final int vibratoSpeed = (int) (Zoom.zoom * 100);
				final int vibratoLineHeight = tailHeight / 2;
				final int vibratoAmplitude = tailHeight - vibratoLineHeight - 1;
				final int vibratoOffset = (vibratoAmplitude - tailHeight) / 2;
				for (int i = 0; i < note.length + 2; i++) {
					final int segmentY = y
							+ (int) (vibratoOffset - vibratoAmplitude * sin(i * Math.PI / vibratoSpeed) / 2);

					data.noteTails.add(lineVertical(x + i, segmentY, y, color));
				}

				int fragmentX = x;
				final int y0 = y + tailHeight / 4;
				final int y1 = y + tailHeight / 2;
				while (fragmentX <= x + length - 40) {
					data.noteTails.add(filledPolygon(color, //
							new Position2D(fragmentX, y), //
							new Position2D(fragmentX + 40, y), //
							new Position2D(fragmentX + 40, y1), //
							new Position2D(fragmentX + 20, y0), //
							new Position2D(fragmentX, y1)));
					fragmentX += 40;
				}

				data.noteTails.add(filledPolygon(color, //
						new Position2D(fragmentX, y), //
						new Position2D(x + length, y), //
						new Position2D(fragmentX, y1)));
			} else if (note.vibrato) {
				final int vibratoSpeed = (int) (Zoom.zoom * 100);
				final int vibratoLineHeight = topBottom.max - topBottom.min;
				final int vibratoAmplitude = tailHeight - vibratoLineHeight - 1;
				final int vibratoOffset = (vibratoAmplitude - tailHeight) / 2;
				for (int i = 0; i < note.length + 2; i++) {
					final int segmentY = y
							+ (int) (vibratoOffset - vibratoAmplitude * sin(i * Math.PI / vibratoSpeed) / 2);

					data.noteTails.add(lineVertical(x + i, segmentY, segmentY + vibratoLineHeight, color));
				}
			} else {
				int fragmentX = x;
				final int y0 = y + tailHeight / 2;
				final int y1 = y + tailHeight / 4;
				final int y2 = y - tailHeight / 4;
				final int y3 = y - tailHeight / 2;
				while (fragmentX <= x + length - 40) {
					data.noteTails.add(filledPolygon(color, //
							new Position2D(fragmentX, y0), //
							new Position2D(fragmentX + 20, y1), //
							new Position2D(fragmentX + 40, y0), //
							new Position2D(fragmentX + 40, y2), //
							new Position2D(fragmentX + 20, y3), //
							new Position2D(fragmentX, y2)));
					fragmentX += 40;
				}

				data.noteTails.add(filledPolygon(color, //
						new Position2D(fragmentX, y0), //
						new Position2D(x + length, y1), //
						new Position2D(x + length, y3), //
						new Position2D(fragmentX, y2)));
			}
		} else {
			final ShapePositionWithSize position = new ShapePositionWithSize(x, topBottom.min, length,
					topBottom.max - topBottom.min);
			data.noteTails.add(filledRectangle(position, color));
		}

		if (note.highlighted) {
			addTailBox(note.x, note.length, y, ColorLabel.HIGHLIGHT);
		} else if (note.selected) {
			addTailBox(note.x, note.length, y, ColorLabel.SELECT);
		}
	}

	public void addNoteTail(final EditorNoteDrawingData note, final int y) {
		if (note.length == 0) {
			return;
		}

		if (note.slideTo != null) {
			if (note.unpitchedSlide) {
				addUnpitchedSlideNoteTailShape(note, y);
			} else {
				addSlideNoteTailShape(note, y);
			}
		} else {
			addNormalNoteTailShape(note, y);
		}
	}

	private void addSlideShapeBox(final int x, final int length, final int y, final ColorLabel color,
			final boolean slideUp) {
		final IntRange topBottom = getDefaultTailTopBottom(y);
		final Position2D a = new Position2D(x, topBottom.min - 1);
		final Position2D b = new Position2D(x, topBottom.max);
		final Position2D c = new Position2D(x + length, slideUp ? topBottom.min - 1 : topBottom.max);

		data.highlights.add(new StrokedTriangle(a, b, c, color));
	}

	private void addNormalTailShapeBox(final int x, final int length, final int y, final ColorLabel color) {
		final IntRange topBottom = getDefaultTailTopBottom(y);

		final ShapePositionWithSize position = new ShapePositionWithSize(x, topBottom.min - 1, length,
				topBottom.max - topBottom.min + 1);
		data.highlights.add(strokedRectangle(position, color));
	}

	public void addTailShapeBox(final int x, final int length, final int y, final ColorLabel color, final boolean slide,
			final boolean slideUp) {
		if (length == 0) {
			return;
		}

		if (slide) {
			addSlideShapeBox(x, length, y, color, slideUp);
		} else {
			addNormalTailShapeBox(x, length, y, color);
		}
	}
}
