package log.charter.gui.chartPanelDrawers.instruments.guitar.theme.modern;

import static log.charter.data.config.ChartPanelColors.getStringBasedColor;
import static log.charter.data.config.GraphicalConfig.noteHeight;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.tailHeight;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.centeredTextWithBackground;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.filledPolygon;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.filledRectangle;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.sine;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.strokedRectangle;
import static log.charter.util.Utils.stringId;

import java.awt.Color;
import java.awt.Font;

import log.charter.data.config.ChartPanelColors.ColorLabel;
import log.charter.data.config.ChartPanelColors.StringColorLabelType;
import log.charter.data.config.values.InstrumentConfig;
import log.charter.gui.chartPanelDrawers.data.EditorNoteDrawingData;
import log.charter.gui.chartPanelDrawers.drawableShapes.Line;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapePositionWithSize;
import log.charter.gui.chartPanelDrawers.drawableShapes.StrokedTriangle;
import log.charter.gui.chartPanelDrawers.instruments.guitar.theme.HighwayDrawData;
import log.charter.util.data.IntRange;
import log.charter.util.data.Position2D;

public class ModernThemeNoteTails {
	private static final Color[] noteTailColors = new Color[InstrumentConfig.maxPossibleStrings];
	private static Font slideFretFont = new Font(Font.SANS_SERIF, Font.BOLD, noteHeight / 2);

	public static void reloadGraphics() {
		for (int i = 0; i < InstrumentConfig.maxPossibleStrings; i++) {
			noteTailColors[stringId(i, InstrumentConfig.maxPossibleStrings)] = getStringBasedColor(
					StringColorLabelType.NOTE_TAIL, i, InstrumentConfig.maxPossibleStrings);
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

	private void addSlideCommon(final EditorNoteDrawingData note, final int y, final Color backgroundColor,
			final Color fretColor) {
		addNormalNoteTailShape(note, y);

		final int lineThickness = 2;

		final IntRange topBottom = getDefaultTailTopBottom(y);
		final int slideStartY = note.slideTo < note.fretNumber ? topBottom.min + lineThickness / 2
				: topBottom.max - lineThickness / 2;
		final int slideEndY = note.slideTo < note.fretNumber ? topBottom.max - lineThickness / 2
				: topBottom.min + lineThickness / 2;
		final int slideStartX = note.x + noteHeight / 4;
		final int slideEndX = note.linkNext ? note.x + note.length - lineThickness - noteHeight / 4
				: note.x + note.length - lineThickness;
		final Position2D slideStart = new Position2D(slideStartX, slideStartY);
		final Position2D slideEnd = new Position2D(slideEndX, slideEndY);

		data.noteTails.add(new Line(slideStart, slideEnd, Color.WHITE, lineThickness));

		if (note.unpitchedSlide) {
			final int tailEndFretTextY = note.slideTo < note.fretNumber ? topBottom.max + noteHeight / 3
					: topBottom.min - noteHeight / 3;
			final Position2D fretTextPosition = new Position2D(note.x + note.length, tailEndFretTextY);
			final Color color = noteTailColors[stringId(note.string, data.strings)];
			data.slideFrets.add(centeredTextWithBackground(fretTextPosition, slideFretFont, note.slideTo + "",
					color.darker().darker().darker(), Color.WHITE,
					noteTailColors[stringId(note.string, data.strings)]));
		}
	}

	private void addSlideNoteTailShape(final EditorNoteDrawingData note, final int y) {
		addSlideCommon(note, y, ColorLabel.SLIDE_NORMAL_FRET_BG.color(), ColorLabel.SLIDE_NORMAL_FRET_TEXT.color());
	}

	private void addUnpitchedSlideNoteTailShape(final EditorNoteDrawingData note, final int y) {
		addSlideCommon(note, y, ColorLabel.SLIDE_UNPITCHED_FRET_BG.color(),
				ColorLabel.SLIDE_UNPITCHED_FRET_TEXT.color());
	}

	private void addTailBox(final int x, final int length, final int y, final Color color) {
		addTailBox(x, length, y, color, 1);
	}

	private void addTailBox(final int x, final int length, final int y, final Color color, final int thickness) {
		final IntRange topBottom = getDefaultTailTopBottom(y);

		final ShapePositionWithSize position = new ShapePositionWithSize(x, topBottom.min - 1, length,
				topBottom.max - topBottom.min + 1);
		data.noteTails.add(strokedRectangle(position, color, thickness));
	}

	private void addNormalNoteTailShape(final EditorNoteDrawingData note, final int y) {
		final IntRange topBottom = getDefaultTailTopBottom(y);
		final int x = note.x - 1;
		final int length = note.length + 1;
		final Color color = noteTailColors[stringId(note.string, data.strings)];

		// Define tremolo appearance
		if (note.tremolo) {
			int fragmentX = x;
			final int intensity = 4;
			final int y0 = y + tailHeight / 2 - intensity + 1;
			final int y1 = y + tailHeight / 2 + 1;
			final int y2 = y - tailHeight / 2 + intensity;
			final int y3 = y - tailHeight / 2;

			final int fragmentSize = 8;
			while (fragmentX <= x + length - fragmentSize) {
				data.noteTails.add(filledPolygon(color.brighter(), //
						new Position2D(fragmentX, y0), //
						new Position2D(fragmentX + fragmentSize / 2, y1), //
						new Position2D(fragmentX + fragmentSize, y0), //
						new Position2D(fragmentX + fragmentSize, y2), //
						new Position2D(fragmentX + fragmentSize / 2, y3), //
						new Position2D(fragmentX, y2)));
				data.noteTails.add(filledPolygon(color, //
						new Position2D(fragmentX, y0 - 2), //
						new Position2D(fragmentX + fragmentSize / 2, y1 - 2), //
						new Position2D(fragmentX + fragmentSize, y0 - 2), //
						new Position2D(fragmentX + fragmentSize, y2 + 2), //
						new Position2D(fragmentX + fragmentSize / 2, y3 + 2), //
						new Position2D(fragmentX, y2 + 2)));
				fragmentX += fragmentSize;
			}

			// Add another partial fragment
			if (fragmentX <= x + length - fragmentSize / 2) {
				data.noteTails.add(filledPolygon(color.brighter(), //
						new Position2D(fragmentX, y0), //
						new Position2D(fragmentX + fragmentSize / 2, y1), //
						new Position2D(x + length, y0), //
						new Position2D(x + length, y2), //
						new Position2D(fragmentX + fragmentSize / 2, y3), //
						new Position2D(fragmentX, y2)));
				data.noteTails.add(filledPolygon(color, //
						new Position2D(fragmentX, y0 - 2), //
						new Position2D(fragmentX + fragmentSize / 2, y1 - 2), //
						new Position2D(x + length - 1, y0 - 2), //
						new Position2D(x + length - 1, y2 + 2), //
						new Position2D(fragmentX + fragmentSize / 2, y3 + 2), //
						new Position2D(fragmentX, y2 + 2)));
			}

			// Add another partial half fragment
			else {
				data.noteTails.add(filledPolygon(color.brighter(), //
						new Position2D(fragmentX, y0), //
						new Position2D(x + length, y1), //
						new Position2D(x + length, y3), //
						new Position2D(fragmentX, y2)));
				data.noteTails.add(filledPolygon(color, //
						new Position2D(fragmentX, y0 - 2), //
						new Position2D(x + length - 1, y1 - 2), //
						new Position2D(x + length - 1, y3 + 2), //
						new Position2D(fragmentX, y2 + 2)));
			}
		} else {
			final ShapePositionWithSize position = new ShapePositionWithSize(x, topBottom.min, length,
					topBottom.max - topBottom.min);
			data.noteTails.add(filledRectangle(position, color));
		}

		// Define vibrato appearance
		if (note.vibrato) {
			final Position2D from = new Position2D(x, y);
			data.noteTails.add(sine(from, length, tailHeight / 2 - 2, -8, 10, Color.GRAY.brighter(), 2));
		}

		if (!note.tremolo) {
			addTailBox(note.x, note.length, y, color.brighter());
		}

		if (note.highlighted) {
			addTailBox(note.x, note.length, y, ColorLabel.HIGHLIGHT.color(), 2);
		} else if (note.selected) {
			addTailBox(note.x, note.length, y, ColorLabel.SELECT.color(), 2);
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
