package log.charter.gui.chartPanelDrawers.instruments.guitar.highway;

import static java.lang.Math.round;
import static java.lang.Math.sin;
import static log.charter.data.config.Config.maxBendValue;
import static log.charter.data.config.Config.showChordIds;
import static log.charter.data.config.GraphicalConfig.anchorInfoHeight;
import static log.charter.data.config.GraphicalConfig.handShapesHeight;
import static log.charter.data.config.GraphicalConfig.noteHeight;
import static log.charter.data.config.GraphicalConfig.noteWidth;
import static log.charter.gui.ChartPanelColors.getStringBasedColor;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.anchorY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.eventNamesY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.getLaneY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesBottom;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesTop;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.phraseNamesY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.sectionNamesY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.tailHeight;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.toneChangeY;
import static log.charter.gui.chartPanelDrawers.drawableShapes.CenteredTextWithBackgroundAndBorder.getExpectedSize;
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
import static log.charter.util.CollectionUtils.map;
import static log.charter.util.FileUtils.imagesFolder;
import static log.charter.util.Utils.getStringPosition;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import log.charter.data.config.Zoom;
import log.charter.data.song.Anchor;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.EventPoint;
import log.charter.data.song.EventType;
import log.charter.data.song.HandShape;
import log.charter.data.song.Phrase;
import log.charter.data.song.SectionType;
import log.charter.data.song.ToneChange;
import log.charter.data.song.enums.Mute;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.ChartPanelColors.StringColorLabelType;
import log.charter.gui.chartPanelDrawers.data.EditorNoteDrawingData;
import log.charter.gui.chartPanelDrawers.data.EditorNoteDrawingData.EditorBendValueDrawingData;
import log.charter.gui.chartPanelDrawers.data.HighlightData.HighlightLine;
import log.charter.gui.chartPanelDrawers.drawableShapes.CenteredTextWithBackground;
import log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape;
import log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShapeList;
import log.charter.gui.chartPanelDrawers.drawableShapes.Line;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapePositionWithSize;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapeSize;
import log.charter.gui.chartPanelDrawers.drawableShapes.StrokedTriangle;
import log.charter.gui.chartPanelDrawers.drawableShapes.Text;
import log.charter.gui.chartPanelDrawers.drawableShapes.TextWithBackground;
import log.charter.util.ImageUtils;
import log.charter.util.RW;
import log.charter.util.Utils;
import log.charter.util.collections.ArrayList2;
import log.charter.util.data.IntRange;
import log.charter.util.data.Position2D;

public class DefaultHighwayDrawer implements HighwayDrawer {
	public static BufferedImage loadImage(final String path) {
		final BufferedImage image = ImageUtils.loadSafe(path, //
				new File(RW.getProgramDirectory(), path), //
				new File(path));
		return image == null ? new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR) : image;
	}

	protected static final BufferedImage palmMuteMarker = loadImage(imagesFolder + "palmMute.png");
	protected static final BufferedImage muteMarker = loadImage(imagesFolder + "mute.png");

	protected final Color selectColor = ColorLabel.SELECT.color();
	protected final Color[] noteColors;
	protected final Color[] noteAccentColors;
	protected final Color[] noteTailColors;

	protected final Font anchorFont;
	protected final Font bendValueFont;
	protected final Font fretFont;
	protected final Font handShapesFont;

	protected final BufferedImage palmMuteImage;
	protected final BufferedImage muteImage;

	protected final int strings;
	protected final int bendStepSize;
	protected final double time;
	protected final int[] stringPositions;

	protected final DrawableShapeList sectionsAndPhrases;
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

	protected final Graphics2D g;

	public DefaultHighwayDrawer(final Graphics2D g, final int strings, final double time) {
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
		handShapesFont = new Font(Font.SANS_SERIF, Font.BOLD, handShapesHeight);

		palmMuteImage = definePalmMuteImage();
		muteImage = defineMuteImage();

		sectionsAndPhrases = new DrawableShapeList();
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

		this.g = g;
	}

	protected Font defineAnchorFont() {
		return new Font(Font.DIALOG, Font.BOLD, anchorInfoHeight);
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

	private void addEventPointTextIfOnScreen(final TextWithBackground text) {
		if (text.getPositionWithSize(g).getRightX() < 0) {
			return;
		}

		sectionsAndPhrases.add(text);
	}

	private void addSection(final Graphics2D g, final SectionType section, final int x) {
		final TextWithBackground text = new TextWithBackground(new Position2D(x, sectionNamesY), anchorFont,
				section.label, ColorLabel.SECTION_NAME_BG, ColorLabel.BASE_DARK_TEXT, ColorLabel.BASE_BORDER.color());

		addEventPointTextIfOnScreen(text);
	}

	private void addPhrase(final Phrase phrase, final String phraseName, final int x) {
		final String phraseLabel = phraseName + " (" + phrase.maxDifficulty + ")"//
				+ (phrase.solo ? "[Solo]" : "");
		final TextWithBackground text = new TextWithBackground(new Position2D(x, phraseNamesY), anchorFont, phraseLabel,
				ColorLabel.PHRASE_NAME_BG, ColorLabel.BASE_DARK_TEXT, ColorLabel.BASE_BORDER);

		addEventPointTextIfOnScreen(text);
	}

	private void addEvents(final List<EventType> events, final int x) {
		final String eventsName = String.join(", ", map(events, event -> event.label));
		final TextWithBackground text = new TextWithBackground(new Position2D(x, eventNamesY), anchorFont, eventsName,
				ColorLabel.EVENT_BG, ColorLabel.BASE_DARK_TEXT, ColorLabel.BASE_BORDER.color());

		addEventPointTextIfOnScreen(text);
	}

	private void addEventPointBox(final int x, final ColorLabel color) {
		final int top = sectionNamesY - 1;
		final int bottom = lanesBottom + 1;
		final ShapePositionWithSize beatPosition = new ShapePositionWithSize(x - 1, top, 3, bottom - top);
		selects.add(filledRectangle(beatPosition, color));
	}

	@Override
	public void addCurrentSection(final Graphics2D g, final SectionType section) {
	}

	@Override
	public void addCurrentSection(final Graphics2D g, final SectionType section, final int nextSectionX) {
	}

	@Override
	public void addCurrentPhrase(final Graphics2D g, final Phrase phrase, final String phraseName,
			final int nextEventPointX) {
	}

	@Override
	public void addCurrentPhrase(final Graphics2D g, final Phrase phrase, final String phraseName) {
	}

	@Override
	public void addEventPoint(final Graphics2D g, final EventPoint eventPoint, final Phrase phrase, final int x,
			final boolean selected, final boolean highlighted) {
		if (eventPoint.section != null) {
			addSection(g, eventPoint.section, x);
		}
		if (eventPoint.phrase != null) {
			addPhrase(phrase, eventPoint.phrase, x);
		}
		if (!eventPoint.events.isEmpty()) {
			addEvents(eventPoint.events, x);
		}

		if (highlighted) {
			addEventPointBox(x, ColorLabel.HIGHLIGHT);
		} else if (selected) {
			addEventPointBox(x, ColorLabel.SELECT);
		}
	}

	@Override
	public void addEventPointHighlight(final int x) {
		selects.add(lineVertical(x, sectionNamesY, lanesBottom, ColorLabel.HIGHLIGHT));
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

		if (note.highlighted) {
			selects.add(strokedRectangle(position, ColorLabel.HIGHLIGHT, 2));
		} else if (note.selected) {
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

		if (note.highlighted) {
			selects.add(strokedRectangle(position, ColorLabel.HIGHLIGHT, 2));
		} else if (note.selected) {
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

		if (note.highlighted) {
			selects.add(strokedRectangle(position, ColorLabel.HIGHLIGHT, 2));
		} else if (note.selected) {
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

		if (note.highlighted) {
			selects.add(strokedRectangle(position, ColorLabel.HIGHLIGHT, 2));
		} else if (note.selected) {
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

	protected void addFullMute(final EditorNoteDrawingData note, final int y) {
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

		slideFrets.add(
				centeredTextWithBackground(c, fretFont, note.slideTo + "", fretColor, backgroundColor, Color.BLACK));

		if (note.highlighted) {
			noteTailSelects.add(new StrokedTriangle(a, b, c, ColorLabel.HIGHLIGHT));
		} else if (note.selected) {
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

		if (note.highlighted) {
			final ShapePositionWithSize position = new ShapePositionWithSize(x, topBottom.min - 1, length,
					topBottom.max - topBottom.min + 1);
			noteTailSelects.add(strokedRectangle(position, ColorLabel.HIGHLIGHT));
		} else if (note.selected) {
			final ShapePositionWithSize position = new ShapePositionWithSize(x, topBottom.min - 1, length,
					topBottom.max - topBottom.min + 1);
			noteTailSelects.add(strokedRectangle(position, selectColor));
		}
	}

	protected String formatBendValue(final BigDecimal bendValue) {
		if (bendValue == null) {
			return "0";
		}

		return Utils.formatBendValue((int) round(bendValue.doubleValue() * 2));
	}

	protected int getBendLineY(final int y, BigDecimal bendValue) {
		if (bendValue == null) {
			bendValue = BigDecimal.ZERO;
		}
		if (bendValue.compareTo(new BigDecimal(maxBendValue)) > 0) {
			bendValue = new BigDecimal(maxBendValue);
		}

		final int bendOffset = bendValue.multiply(new BigDecimal(tailHeight * 2 / 3))
				.divide(new BigDecimal(maxBendValue), RoundingMode.HALF_UP).intValue();
		return y + tailHeight / 3 - bendOffset;
	}

	private void addBendValueIcon(final int x, final int y, final BigDecimal bendValue, final boolean linked) {
		if (linked) {
			return;
		}

		final String text = "ノ" + formatBendValue(bendValue);
		final ShapeSize expectedIconSize = getExpectedSize(g, bendValueFont, text);
		final int minBendXAfterHead = noteWidth / 2 + expectedIconSize.width / 2;

		Position2D iconPosition;
		if (x > minBendXAfterHead) {
			iconPosition = new Position2D(x, y);
		} else {
			final int bendY = y - noteHeight / 2 - expectedIconSize.height / 2;
			iconPosition = new Position2D(x, bendY);
		}
		final DrawableShape bendValueIcon = new CenteredTextWithBackground(iconPosition, bendValueFont, text,
				Color.WHITE, Color.BLACK);
		bendValues.add(bendValueIcon);
	}

	protected void addBendValues(final EditorNoteDrawingData note, final int y) {
		if (note.bendValues == null || note.bendValues.isEmpty()) {
			return;
		}

		Position2D lastBendLinePosition = new Position2D(note.x, getBendLineY(y, BigDecimal.ZERO));
		for (final EditorBendValueDrawingData bendValue : note.bendValues) {
			final Position2D lineTo = new Position2D(bendValue.x, getBendLineY(y, bendValue.bendValue));
			noteTails.add(line(lastBendLinePosition, lineTo, Color.WHITE));

			addBendValueIcon(bendValue.x, y, bendValue.bendValue, note.linkPrevious);

			lastBendLinePosition = lineTo;
		}

		noteTails.add(
				line(lastBendLinePosition, new Position2D(note.x + note.length, lastBendLinePosition.y), Color.WHITE));
	}

	protected void addNoteTail(final EditorNoteDrawingData note, final int y) {
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

	protected void addNoteShape(final EditorNoteDrawingData note, final int y) {
		switch (note.hopo) {
			case HAMMER_ON:
				addHammerOnShape(y, note);
				break;
			case PULL_OFF:
				addPullOffShape(y, note);
				break;
			case TAP:
				addTapShape(y, note);
				break;
			case NONE:
			default:
				addNormalNoteShape(y, note);
				break;
		}

		switch (note.harmonic) {
			case NORMAL:
				addHarmonicShape(y, note);
				break;
			case PINCH:
				addPinchHarmonicShape(y, note);
				break;
			case NONE:
			default:
				break;
		}

		if (!note.linkPrevious || note.wrongLink) {
			if (note.mute == Mute.PALM) {
				addPalmMute(note, y);
			} else if (note.mute == Mute.FULL) {
				addFullMute(note, y);
			}

			noteFrets.add(centeredTextWithBackground(new Position2D(note.x, y), fretFont, note.fret,
					note.mute == Mute.FULL ? Color.GRAY : Color.BLACK, Color.WHITE, Color.BLACK));
		}

		addNoteTail(note, y);
		addBendValues(note, y);
	}

	@Override
	public void addNote(final EditorNoteDrawingData note) {
		if (note.string >= stringPositions.length) {
			return;
		}

		final int y = stringPositions[note.string];
		addNoteShape(note, y);
	}

	@Override
	public void addChordName(final int x, final String chordName) {
		chordNames.add(new Text(new Position2D(x + 2, lanesTop - 1), fretFont, chordName, ColorLabel.BASE_DARK_TEXT));
	}

	private void addNoteHighlight(final int x, final int length, final int string) {
		final int y = getLaneY(getStringPosition(string, strings));

		if (length > 0) {
			final IntRange topBottom = getDefaultTailTopBottom(y);
			final ShapePositionWithSize position = new ShapePositionWithSize(x, topBottom.min - 1, length,
					topBottom.max - topBottom.min + 1);
			noteTailSelects.add(strokedRectangle(position, ColorLabel.HIGHLIGHT));
		}

		final ShapePositionWithSize notePosition = new ShapePositionWithSize(x, y, noteWidth, noteHeight)//
				.centered().resized(-1, -1, 1, 1);
		notes.add(strokedRectangle(notePosition, ColorLabel.HIGHLIGHT));
	}

	@Override
	public void addSoundHighlight(final int x, final int length, final Optional<ChordOrNote> originalSound,
			final Optional<ChordTemplate> template, final int string, final boolean drawOriginalStrings) {
		originalSound.map(sound -> sound.noteWithFrets(string, template.orElse(null)))//
				.ifPresentOrElse(note -> addNoteHighlight(x, length, note.get().string()), //
						() -> addNoteHighlight(x, 0, string));
	}

	@Override
	public void addNoteAdditionLine(final HighlightLine line) {
		notes.add(new Line(line.lineStart, line.lineEnd, ColorLabel.NOTE_ADD_LINE));
	}

	@Override
	public void addCurrentAnchor(final Graphics2D g, final Anchor anchor) {
	}

	@Override
	public void addCurrentAnchor(final Graphics2D g, final Anchor anchor, final int nextAnchorX) {
	}

	protected void addAnchorLine(final int x) {
		anchors.add(lineVertical(x, anchorY, lanesBottom, ColorLabel.ANCHOR));
	}

	protected void addAnchorText(final Anchor anchor, final int x) {
		final String anchorText = anchor.width == 4 ? anchor.fret + "" : anchor.fret + " - " + anchor.topFret();
		anchors.add(new Text(new Position2D(x + 4, anchorY + 1), anchorFont, anchorText, ColorLabel.ANCHOR));
	}

	protected void addAnchorBox(final int x, final ColorLabel color) {
		final int top = anchorY - 1;
		final int bottom = lanesBottom + 1;
		final ShapePositionWithSize anchorPosition = new ShapePositionWithSize(x - 1, top, 2, bottom - top);
		selects.add(strokedRectangle(anchorPosition, color));
	}

	@Override
	public void addAnchor(final Anchor anchor, final int x, final boolean selected, final boolean highlighted) {
		addAnchorLine(x);
		addAnchorText(anchor, x);

		if (highlighted) {
			addAnchorBox(x, ColorLabel.HIGHLIGHT);
		} else if (selected) {
			addAnchorBox(x, ColorLabel.SELECT);
		}
	}

	@Override
	public void addAnchorHighlight(final int x) {
		anchors.add(lineVertical(x, anchorY, lanesBottom, ColorLabel.HIGHLIGHT.color()));
	}

	@Override
	public void addHandShape(final int x, final int length, final boolean selected, final boolean highlighted,
			final HandShape handShape, final ChordTemplate chordTemplate) {
		final ShapePositionWithSize position = new ShapePositionWithSize(x, lanesBottom + 1, length, handShapesHeight);
		final ColorLabel fillColor = chordTemplate.arpeggio ? ColorLabel.HAND_SHAPE_ARPEGGIO : ColorLabel.HAND_SHAPE;
		handShapes.add(filledRectangle(position, fillColor));

		if (highlighted) {
			selects.add(strokedRectangle(position, ColorLabel.HIGHLIGHT));
		} else if (selected) {
			selects.add(strokedRectangle(position, selectColor));
		}

		String chordName = chordTemplate.chordName;
		if (showChordIds) {
			final String templateIdString = handShape.templateId == null ? "-" : (handShape.templateId + "");

			chordName = (chordName == null || chordName.isBlank()) ? "[" + templateIdString + "]"
					: chordName + " [" + templateIdString + "]";
		}
		if (chordName != null) {
			handShapes.add(new Text(new Position2D(x + 2, lanesBottom + 1), handShapesFont, chordName,
					ColorLabel.BASE_DARK_TEXT));
		}
	}

	@Override
	public void addHandShapeHighlight(final int x, final int length) {
		final ShapePositionWithSize position = new ShapePositionWithSize(x, lanesBottom + 1, length - 1,
				handShapesHeight - 1);
		handShapes.add(strokedRectangle(position, ColorLabel.HIGHLIGHT));
	}

	@Override
	public void addCurrentTone(final Graphics2D g, final String tone) {
	}

	@Override
	public void addCurrentTone(final Graphics2D g, final String tone, final int nextToneChangeX) {
	}

	private void addToneChangeBox(final int x, final ColorLabel color) {
		final int top = toneChangeY - 1;
		final int bottom = lanesBottom + 1;
		final ShapePositionWithSize toneChangePosition = new ShapePositionWithSize(x - 1, top, 2, bottom - top);
		toneChanges.add(strokedRectangle(toneChangePosition, color));
	}

	@Override
	public void addToneChange(final ToneChange toneChange, final int x, final boolean selected,
			final boolean highlighted) {
		toneChanges.add(lineVertical(x, toneChangeY, lanesBottom, ColorLabel.TONE_CHANGE));
		toneChanges.add(new TextWithBackground(new Position2D(x, toneChangeY), anchorFont, "" + toneChange.toneName,
				ColorLabel.TONE_CHANGE, ColorLabel.BASE_TEXT, 2, ColorLabel.BASE_BORDER.color()));

		if (highlighted) {
			addToneChangeBox(x, ColorLabel.HIGHLIGHT);
		} else if (selected) {
			addToneChangeBox(x, ColorLabel.SELECT);
		}
	}

	@Override
	public void addToneChangeHighlight(final int x) {
		anchors.add(lineVertical(x, toneChangeY, lanesBottom, ColorLabel.HIGHLIGHT));
	}

	@Override
	public void draw(final Graphics2D g) {
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
