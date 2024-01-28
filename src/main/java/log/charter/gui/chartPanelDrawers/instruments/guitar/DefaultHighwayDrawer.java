package log.charter.gui.chartPanelDrawers.instruments.guitar;

import static java.lang.Math.round;
import static java.lang.Math.sin;
import static log.charter.data.config.Config.maxBendValue;
import static log.charter.data.config.Config.noteHeight;
import static log.charter.data.config.Config.noteWidth;
import static log.charter.data.config.Config.showChordIds;
import static log.charter.gui.ChartPanelColors.getStringBasedColor;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.anchorTextY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.anchorY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.getLaneY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesBottom;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesTop;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.tailHeight;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.toneChangeY;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.centeredImage;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.centeredTextWithBackground;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.clippedShapes;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.filledOval;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.filledPolygon;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.filledRectangle;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.filledTriangle;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.line;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.lineHorizontal;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.lineVertical;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.strokedRectangle;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.strokedTriangle;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.text;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.textWithBackground;
import static log.charter.gui.chartPanelDrawers.instruments.guitar.EditorNoteDrawingData.fromChord;
import static log.charter.gui.chartPanelDrawers.instruments.guitar.EditorNoteDrawingData.fromNote;
import static log.charter.util.ScalingUtils.timeToX;
import static log.charter.util.Utils.getStringPosition;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import log.charter.data.config.Zoom;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.ChartPanelColors.StringColorLabelType;
import log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape;
import log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShapeList;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapePositionWithSize;
import log.charter.io.Logger;
import log.charter.song.Anchor;
import log.charter.song.BendValue;
import log.charter.song.ChordTemplate;
import log.charter.song.HandShape;
import log.charter.song.ToneChange;
import log.charter.song.enums.HOPO;
import log.charter.song.enums.Harmonic;
import log.charter.song.enums.Mute;
import log.charter.song.notes.Chord;
import log.charter.song.notes.Note;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.IntRange;
import log.charter.util.Position2D;
import log.charter.util.RW;

public class DefaultHighwayDrawer implements HighwayDrawer {
	public static BufferedImage loadImage(final String path) {
		try {
			return ImageIO.read(new File(RW.getProgramDirectory(), path));
		} catch (final IOException e) {
			try {
				return ImageIO.read(new File(path));
			} catch (final IOException e2) {
				Logger.error("Couldn't load image " + path, e);
				return new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
			}
		}
	}

	protected static final BufferedImage palmMuteMarker = loadImage("images/palmMute.png");
	protected static final BufferedImage muteMarker = loadImage("images/mute.png");

	protected final Color selectColor = ColorLabel.SELECT.color();
	protected final Color[] noteColors;
	protected final Color[] noteAccentColors;
	protected final Color[] noteTailColors;

	protected final Font anchorFont;
	protected final Font bendValueFont;
	protected final Font fretFont;

	protected final BufferedImage palmMuteImage;
	protected final BufferedImage muteImage;

	protected final int strings;
	protected final int bendStepSize;
	protected final int time;
	protected final int[] stringPositions;

	protected final DrawableShapeList anchors;
	protected final DrawableShapeList bendValues;
	protected final DrawableShapeList chordNames;
	protected final DrawableShapeList handShapes;
	protected final DrawableShapeList noteTails;
	protected final DrawableShapeList noteTailSelects;
	protected final DrawableShapeList notes;
	protected final DrawableShapeList noteFrets;
	protected final DrawableShapeList noteIds;
	protected final DrawableShapeList selects;
	protected final DrawableShapeList slideFrets;
	protected final DrawableShapeList toneChanges;

	public DefaultHighwayDrawer(final int strings, final int time) {
		this.strings = strings;
		this.time = time;

		stringPositions = new int[strings];
		noteColors = new Color[strings];
		noteAccentColors = new Color[strings];
		noteTailColors = new Color[strings];
		for (int i = 0; i < strings; i++) {
			stringPositions[i] = getLaneY(getStringPosition(i, strings));
			noteColors[i] = getStringBasedColor(StringColorLabelType.NOTE, i, strings);
			noteAccentColors[i] = getStringBasedColor(StringColorLabelType.NOTE_ACCENT, i, strings);
			noteTailColors[i] = getStringBasedColor(StringColorLabelType.NOTE_TAIL, i, strings);
		}

		bendStepSize = tailHeight / 3;

		anchorFont = defineAnchorFont();
		bendValueFont = defineBendFont();
		fretFont = defineFretFont();

		palmMuteImage = definePalmMuteImage();
		muteImage = defineMuteImage();

		anchors = new DrawableShapeList();
		bendValues = new DrawableShapeList();
		chordNames = new DrawableShapeList();
		handShapes = new DrawableShapeList();
		noteTails = new DrawableShapeList();
		noteTailSelects = new DrawableShapeList();
		notes = new DrawableShapeList();
		noteFrets = new DrawableShapeList();
		noteIds = new DrawableShapeList();
		selects = new DrawableShapeList();
		slideFrets = new DrawableShapeList();
		toneChanges = new DrawableShapeList();
	}

	protected Font defineAnchorFont() {
		return new Font(Font.DIALOG, Font.BOLD, 13);
	}

	protected Font defineBendFont() {
		return new Font(Font.DIALOG, Font.BOLD, 15);
	}

	protected Font defineFretFont() {
		return new Font(Font.SANS_SERIF, Font.BOLD, 15);
	}

	protected BufferedImage definePalmMuteImage() {
		final int w = palmMuteMarker.getWidth();
		final int h = palmMuteMarker.getHeight();
		final BufferedImage scaledPalmMuteImage = new BufferedImage(noteWidth, noteHeight, BufferedImage.TYPE_INT_ARGB);
		final AffineTransform at = new AffineTransform();
		at.scale(1.0 * noteWidth / w, 1.0 * noteHeight / h);
		final AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		scaleOp.filter(palmMuteMarker, scaledPalmMuteImage);
		return scaledPalmMuteImage;
	}

	protected BufferedImage defineMuteImage() {
		final int w = muteMarker.getWidth();
		final int h = muteMarker.getHeight();
		final BufferedImage scaledMuteImage = new BufferedImage(noteWidth, noteHeight, BufferedImage.TYPE_INT_ARGB);
		final AffineTransform at = new AffineTransform();
		at.scale(1.0 * noteWidth / w, 1.0 * noteHeight / h);
		final AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		scaleOp.filter(muteMarker, scaledMuteImage);
		return scaledMuteImage;
	}

	protected Color applyWrongLink(final Color color) {
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), 128);
	}

	protected Color getNoteColor(final EditorNoteDrawingData note) {
		final Color color = note.mute != Mute.FULL ? noteColors[note.string] : ColorLabel.NOTE_FULL_MUTE.color();
		return note.wrongLink ? applyWrongLink(color) : color;
	}

	protected void addNormalNoteShape(final int y, final EditorNoteDrawingData note) {
		if (note.linkPrevious && !note.wrongLink) {
			return;
		}

		final ShapePositionWithSize position = new ShapePositionWithSize(note.x, y, noteWidth, noteHeight)//
				.centered();

		notes.add(filledRectangle(position, getNoteColor(note)));
		if (note.accent) {
			final Color accentColor = noteAccentColors[note.string];
			notes.add(strokedRectangle(position.resized(0, 0, -1, -1), accentColor));
			notes.add(strokedRectangle(position.resized(-2, -2, 3, 3), accentColor));
		}

		if (note.selected) {
			selects.add(strokedRectangle(position, selectColor, 2));
		}
	}

	protected void addHammerOnShape(final int y, final EditorNoteDrawingData note) {
		final ShapePositionWithSize position = new ShapePositionWithSize(note.x, y, noteWidth, noteHeight)//
				.centered();

		final Position2D a = new Position2D(note.x, y - noteHeight / 2);
		final Position2D b = new Position2D(note.x - noteWidth / 2, y + noteHeight / 2);
		final Position2D c = new Position2D(note.x + noteWidth / 2, y + noteHeight / 2);
		notes.add(filledTriangle(a, b, c, getNoteColor(note)));

		if (note.accent) {
			final Color accentColor = noteAccentColors[note.string];
			notes.add(strokedRectangle(position.resized(0, 0, -1, -1), accentColor));
			notes.add(strokedRectangle(position.resized(-2, -2, 3, 3), accentColor));
		}

		if (note.selected) {
			selects.add(strokedRectangle(position, selectColor, 2));
		}
	}

	protected void addPullOffShape(final int y, final EditorNoteDrawingData note) {
		final ShapePositionWithSize position = new ShapePositionWithSize(note.x, y, noteWidth, noteHeight)//
				.centered();

		final Position2D a = new Position2D(note.x, y + noteHeight / 2);
		final Position2D b = new Position2D(note.x - noteWidth / 2, y - noteHeight / 2);
		final Position2D c = new Position2D(note.x + noteWidth / 2, y - noteHeight / 2);
		notes.add(filledTriangle(a, b, c, getNoteColor(note)));

		if (note.accent) {
			final Color accentColor = noteAccentColors[note.string];
			notes.add(strokedRectangle(position.resized(0, 0, -1, -1), accentColor));
			notes.add(strokedRectangle(position.resized(-2, -2, 3, 3), accentColor));
		}

		if (note.selected) {
			selects.add(strokedRectangle(position, selectColor, 2));
		}
	}

	protected void addTapShape(final int y, final EditorNoteDrawingData note) {
		final ShapePositionWithSize position = new ShapePositionWithSize(note.x, y, noteWidth, noteHeight)//
				.centered();

		final int x0 = note.x - noteWidth / 2;
		final int x1 = x0 + noteWidth / 2;
		final int x3 = note.x + noteWidth / 2;
		final int x2 = x3 - noteWidth / 2;

		final int y0 = y - noteHeight / 2;
		final int y1 = y;
		final int y2 = y + noteHeight / 2;

		final List<Position2D> positions = new ArrayList2<>(//
				new Position2D(x2, y0), //
				new Position2D(x3, y0), //
				new Position2D(x1, y1), //
				new Position2D(x3, y2), //
				new Position2D(x2, y2), //
				new Position2D(x0, y1));

		notes.add(filledPolygon(positions, getNoteColor(note)));

		if (note.accent) {
			final Color accentColor = noteAccentColors[note.string];
			notes.add(strokedRectangle(position.resized(0, 0, -1, -1), accentColor));
			notes.add(strokedRectangle(position.resized(-2, -2, 3, 3), accentColor));
		}

		if (note.selected) {
			selects.add(strokedRectangle(position, selectColor, 2));
		}
	}

	protected void addHarmonicShape(final int y, final EditorNoteDrawingData note) {
		final ShapePositionWithSize harmonicPosition = new ShapePositionWithSize(note.x, y - 1, noteWidth + 5,
				noteWidth + 5).centered();
		notes.add(filledOval(harmonicPosition, new Color(224, 224, 224)));
		notes.add(filledOval(harmonicPosition.resized(5, 5, -10, -10), noteColors[note.string]));
	}

	protected void addPinchHarmonicShape(final int y, final EditorNoteDrawingData note) {
		final ShapePositionWithSize position = new ShapePositionWithSize(note.x, y, noteWidth, noteHeight)//
				.centered();

		final Color laneColor = getStringBasedColor(StringColorLabelType.LANE, note.string, strings);

		notes.add(filledOval(position.resized(-2, -2, 3, 3), ColorLabel.BASE_BG_1.color()));
		notes.add(lineHorizontal(note.x - noteWidth / 2 - 2, note.x + noteWidth / 2 + 2, y, laneColor));
		notes.add(filledOval(position.resized(2, 2, -5, -5), noteColors[note.string]));
		notes.add(filledOval(position.resized(5, 5, -11, -11), ColorLabel.BASE_BG_1.color()));
		notes.add(lineHorizontal(note.x - noteWidth / 2 + 5, note.x + noteWidth / 2 - 5, y, laneColor));
	}

	protected void addPalmMute(final EditorNoteDrawingData note, final int y) {
		notes.add(centeredImage(new Position2D(note.x, y), palmMuteImage));
	}

	protected void addMute(final EditorNoteDrawingData note, final int y) {
		notes.add(centeredImage(new Position2D(note.x, y), muteImage));
	}

	protected IntRange getDefaultTailTopBottom(final int y) {
		final int topY = y - tailHeight / 3;
		final int bottomY = y + tailHeight / 3 + 1;
		return new IntRange(topY, bottomY);
	}

	protected void addSlideCommon(final EditorNoteDrawingData note, final int y, final Color backgroundColor,
			final Color fretColor) {
		IntRange topBottom = getDefaultTailTopBottom(y);
		topBottom = new IntRange(topBottom.min - 1, topBottom.max);
		final Position2D a = new Position2D(note.x, topBottom.min);
		final Position2D b = new Position2D(note.x, topBottom.max);
		final int tailEndY = note.slideTo < note.fretNumber ? topBottom.max : topBottom.min;
		final Position2D c = new Position2D(note.x + note.length, tailEndY);
		final Color color = noteTailColors[note.string];

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
				noteTails.add(clippedShapes(
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

					noteTails.add(filledPolygon(color, //
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

				noteTails.add(filledTriangle(new Position2D(x - 40, middleY - 2 * height / 3),
						new Position2D(x - 40, middleY + height / 3), new Position2D(note.x + note.length, tailEndY),
						color));
			}
		} else {
			noteTails.add(filledTriangle(a, b, c, color));
		}

		slideFrets.add(centeredTextWithBackground(c, note.slideTo + "", backgroundColor, fretColor));
		if (note.selected) {
			noteTailSelects.add(strokedTriangle(a, b, c, selectColor));
		}
	}

	protected void addSlideNoteTailShape(final EditorNoteDrawingData note, final int y) {
		addSlideCommon(note, y, ColorLabel.SLIDE_NORMAL_FRET_BG.color(), ColorLabel.SLIDE_NORMAL_FRET_TEXT.color());
	}

	protected void addUnpitchedSlideNoteTailShape(final EditorNoteDrawingData note, final int y) {
		addSlideCommon(note, y, ColorLabel.SLIDE_UNPITCHED_FRET_BG.color(),
				ColorLabel.SLIDE_UNPITCHED_FRET_TEXT.color());
	}

	protected void addNormalNoteTailShape(final EditorNoteDrawingData note, final int y) {
		final IntRange topBottom = getDefaultTailTopBottom(y);
		final int x = note.x - 1;
		final int length = note.length + 1;
		final Color color = noteTailColors[note.string];

		if (note.vibrato || note.tremolo) {
			if (note.vibrato && note.tremolo) {
				final int vibratoSpeed = (int) (Zoom.zoom * 100);
				final int vibratoLineHeight = tailHeight / 2;
				final int vibratoAmplitude = tailHeight - vibratoLineHeight - 1;
				final int vibratoOffset = (vibratoAmplitude - tailHeight) / 2;
				for (int i = 0; i < note.length + 2; i++) {
					final int segmentY = y
							+ (int) (vibratoOffset - vibratoAmplitude * sin(i * Math.PI / vibratoSpeed) / 2);

					noteTails.add(lineVertical(x + i, segmentY, y, color));
				}

				int fragmentX = x;
				final int y0 = y + tailHeight / 4;
				final int y1 = y + tailHeight / 2;
				while (fragmentX <= x + length - 40) {
					noteTails.add(filledPolygon(color, //
							new Position2D(fragmentX, y), //
							new Position2D(fragmentX + 40, y), //
							new Position2D(fragmentX + 40, y1), //
							new Position2D(fragmentX + 20, y0), //
							new Position2D(fragmentX, y1)));
					fragmentX += 40;
				}

				noteTails.add(filledPolygon(color, //
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

					noteTails.add(lineVertical(x + i, segmentY, segmentY + vibratoLineHeight, color));
				}
			} else {
				int fragmentX = x;
				final int y0 = y + tailHeight / 2;
				final int y1 = y + tailHeight / 4;
				final int y2 = y - tailHeight / 4;
				final int y3 = y - tailHeight / 2;
				while (fragmentX <= x + length - 40) {
					noteTails.add(filledPolygon(color, //
							new Position2D(fragmentX, y0), //
							new Position2D(fragmentX + 20, y1), //
							new Position2D(fragmentX + 40, y0), //
							new Position2D(fragmentX + 40, y2), //
							new Position2D(fragmentX + 20, y3), //
							new Position2D(fragmentX, y2)));
					fragmentX += 40;
				}

				noteTails.add(filledPolygon(color, //
						new Position2D(fragmentX, y0), //
						new Position2D(x + length, y1), //
						new Position2D(x + length, y3), //
						new Position2D(fragmentX, y2)));
			}
		} else {
			final ShapePositionWithSize position = new ShapePositionWithSize(x, topBottom.min, length,
					topBottom.max - topBottom.min);
			noteTails.add(filledRectangle(position, color));
		}

		if (note.selected) {
			final ShapePositionWithSize position = new ShapePositionWithSize(x, topBottom.min - 1, length,
					topBottom.max - topBottom.min + 1);
			noteTailSelects.add(strokedRectangle(position, selectColor));
		}
	}

	protected String formatBendValue(final BigDecimal bendValue) {
		if (bendValue == null) {
			return "0";
		}

		final int value = (int) round(bendValue.doubleValue() * 4);
		final int fullSteps = value / 4;
		final int quarterSteps = value % 4;

		if (fullSteps == 0) {
			if (quarterSteps == 0) {
				return "0";
			}
			if (quarterSteps == 1) {
				return "¼";
			}
			if (quarterSteps == 2) {
				return "½";
			}
			if (quarterSteps == 3) {
				return "¾";
			}
		}

		String text = fullSteps + "";
		if (quarterSteps == 1) {
			text += " ¼";
		}
		if (quarterSteps == 2) {
			text += " ½";
		}
		if (quarterSteps == 3) {
			text += " ¾";
		}

		return text;
	}

	protected int getBendLineY(final int y, BigDecimal bendValue) {
		if (bendValue == null) {
			bendValue = BigDecimal.ZERO;
		}
		if (bendValue.compareTo(new BigDecimal(maxBendValue)) > 0) {
			bendValue = new BigDecimal(maxBendValue);
		}

		final int bendOffset = bendValue.multiply(new BigDecimal(tailHeight * 2 / 3))
				.divide(new BigDecimal(3), RoundingMode.HALF_UP).intValue();
		return y + tailHeight / 3 - bendOffset;
	}

	protected void addBendValues(final EditorNoteDrawingData note, final int y) {
		Position2D lastBendLinePosition = new Position2D(note.x, getBendLineY(y, BigDecimal.ZERO));

		for (final BendValue bendValue : note.bendValues) {
			final int bendX = timeToX(note.position + bendValue.position(), time);
			final int bendY = bendX > note.x + noteWidth ? y : y - 26;

			final Position2D lineTo = new Position2D(bendX, getBendLineY(y, bendValue.bendValue));

			final Position2D position = new Position2D(bendX, bendY);
			final String text = "ノ" + formatBendValue(bendValue.bendValue);

			noteTails.add(line(lastBendLinePosition, lineTo, Color.WHITE));
			bendValues.add(centeredTextWithBackground(position, text, Color.BLACK, Color.WHITE));

			lastBendLinePosition = lineTo;
		}

		noteTails.add(
				line(lastBendLinePosition, new Position2D(note.x + note.length, lastBendLinePosition.y), Color.WHITE));
	}

	protected void addNoteTail(final EditorNoteDrawingData note, final int y) {
		if (note.slideTo != null) {
			if (note.unpitchedSlide) {
				addUnpitchedSlideNoteTailShape(note, y);
			} else {
				addSlideNoteTailShape(note, y);
			}
		} else {
			addNormalNoteTailShape(note, y);
		}

		if (note.bendValues != null && !note.bendValues.isEmpty()) {
			addBendValues(note, y);
		}
	}

	protected void addNoteShape(final EditorNoteDrawingData note, final int y) {
		if (note.hopo == HOPO.NONE) {
			addNormalNoteShape(y, note);
		} else if (note.hopo == HOPO.HAMMER_ON) {
			addHammerOnShape(y, note);
		} else if (note.hopo == HOPO.PULL_OFF) {
			addPullOffShape(y, note);
		} else if (note.hopo == HOPO.TAP) {
			addTapShape(y, note);
		}

		if (note.harmonic == Harmonic.NORMAL) {
			addHarmonicShape(y, note);
		} else if (note.harmonic == Harmonic.PINCH) {
			addPinchHarmonicShape(y, note);
		}

		if (!note.linkPrevious || note.wrongLink) {
			if (note.mute == Mute.PALM) {
				addPalmMute(note, y);
			} else if (note.mute == Mute.FULL) {
				addMute(note, y);
			}

			noteFrets.add(centeredTextWithBackground(new Position2D(note.x, y), note.fret, Color.WHITE,
					note.mute == Mute.FULL ? Color.GRAY : Color.BLACK));
		}

		if (note.length > 0) {
			addNoteTail(note, y);
		}
	}

	protected void addSimpleNote(final EditorNoteDrawingData note) {
		if (note.string >= stringPositions.length) {
			return;
		}

		final int y = stringPositions[note.string];
		addNoteShape(note, y);
	}

	@Override
	public void addNote(final Note note, final int x, final boolean selected, final boolean lastWasLinkNext,
			final boolean wrongLinkNext) {
		addSimpleNote(fromNote(x, note, selected, lastWasLinkNext, wrongLinkNext));
	}

	@Override
	public void addChord(final Chord chord, final ChordTemplate chordTemplate, final int x, final int length,
			final boolean selected, final boolean lastWasLinkNext, final boolean wrongLinkNext, final boolean ctrl) {
		for (final EditorNoteDrawingData noteData : fromChord(chord, chordTemplate, x, selected, lastWasLinkNext,
				wrongLinkNext, ctrl)) {
			addSimpleNote(noteData);
		}

		String chordName = chordTemplate.chordName;
		if (showChordIds) {
			chordName = (chordName == null || chordName.isBlank()) ? "[" + chord.templateId() + "]"
					: chordName + " [" + chord.templateId() + "]";
		}
		if (chordName != null) {
			chordNames.add(text(new Position2D(x + 2, lanesTop - 1), chordName, ColorLabel.BASE_DARK_TEXT));
		}
	}

	@Override
	public void addAnchor(final Anchor anchor, final int x, final boolean selected) {
		anchors.add(lineVertical(x, anchorY, lanesBottom, ColorLabel.ANCHOR));
		final String anchorText = anchor.width == 4 ? anchor.fret + "" : anchor.fret + " - " + anchor.topFret();
		anchors.add(text(new Position2D(x + 4, anchorTextY), anchorText, ColorLabel.ANCHOR));

		if (selected) {
			final int top = anchorY - 1;
			final int bottom = lanesBottom + 1;
			final ShapePositionWithSize anchorPosition = new ShapePositionWithSize(x - 1, top, 2, bottom - top);
			selects.add(strokedRectangle(anchorPosition, selectColor));
		}
	}

	@Override
	public void addHandShape(final int x, final int length, final boolean selected, final HandShape handShape,
			final ChordTemplate chordTemplate) {
		final ShapePositionWithSize position = new ShapePositionWithSize(x, lanesBottom, length, 10);
		final ColorLabel fillColor = chordTemplate.arpeggio ? ColorLabel.HAND_SHAPE_ARPEGGIO : ColorLabel.HAND_SHAPE;
		handShapes.add(filledRectangle(position, fillColor));

		if (selected) {
			selects.add(strokedRectangle(position, selectColor));
		}

		String chordName = chordTemplate.chordName;
		if (showChordIds) {
			chordName = (chordName == null || chordName.isBlank()) ? "[" + handShape.templateId + "]"
					: chordName + " [" + handShape.templateId + "]";
		}
		if (chordName != null) {
			chordNames.add(text(new Position2D(x + 2, lanesBottom + 21), chordName, ColorLabel.BASE_DARK_TEXT));
		}
	}

	@Override
	public void addToneChange(final ToneChange toneChange, final int x, final boolean selected) {
		toneChanges.add(lineVertical(x, toneChangeY, lanesBottom, ColorLabel.TONE_CHANGE));
		toneChanges.add(textWithBackground(new Position2D(x + 4, toneChangeY + 12), "" + toneChange.toneName,
				ColorLabel.TONE_CHANGE, ColorLabel.BASE_TEXT));

		if (selected) {
			final int top = toneChangeY - 1;
			final int bottom = lanesBottom + 1;
			final ShapePositionWithSize toneChangePosition = new ShapePositionWithSize(x - 1, top, 2, bottom - top);
			selects.add(strokedRectangle(toneChangePosition, selectColor));
		}
	}

	@Override
	public void draw(final Graphics g) {
		g.setFont(anchorFont);
		toneChanges.draw(g);
		anchors.draw(g);
		chordNames.draw(g);
		noteTails.draw(g);

		noteTailSelects.draw(g);
		notes.draw(g);

		slideFrets.draw(g);

		g.setFont(bendValueFont);
		bendValues.draw(g);

		g.setFont(fretFont);
		noteFrets.draw(g);

		handShapes.draw(g);
		selects.draw(g);

		g.setFont(bendValueFont);
		noteIds.draw(g);
	}
}
