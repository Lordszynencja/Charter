package log.charter.gui.chartPanelDrawers.instruments.guitar;

import static java.lang.Math.max;
import static java.lang.Math.round;
import static java.lang.Math.sin;
import static log.charter.data.config.Config.maxBendValue;
import static log.charter.data.config.Config.maxStrings;
import static log.charter.data.config.Config.showChordIds;
import static log.charter.data.config.GraphicalConfig.anchorInfoHeight;
import static log.charter.data.config.GraphicalConfig.handShapesHeight;
import static log.charter.data.config.GraphicalConfig.noteHeight;
import static log.charter.data.config.GraphicalConfig.toneChangeHeight;
import static log.charter.gui.ChartPanelColors.getStringBasedColor;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.anchorY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.getLaneY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesBottom;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesTop;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.tailHeight;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.toneChangeY;
import static log.charter.gui.chartPanelDrawers.data.EditorNoteDrawingData.fromChord;
import static log.charter.gui.chartPanelDrawers.data.EditorNoteDrawingData.fromNote;
import static log.charter.gui.chartPanelDrawers.drawableShapes.CenteredTextWithBackgroundAndBorder.getExpectedSize;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.centeredImage;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.centeredTextWithBackground;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.clippedShapes;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.filledPolygon;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.filledRectangle;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.filledTriangle;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.lineVertical;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.strokedRectangle;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.strokedTriangle;
import static log.charter.util.ScalingUtils.timeToX;
import static log.charter.util.ScalingUtils.xToTimeLength;
import static log.charter.util.Utils.getStringPosition;
import static log.charter.util.Utils.stringId;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import log.charter.data.config.Zoom;
import log.charter.gui.ChartPanelColors;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.ChartPanelColors.StringColorLabelType;
import log.charter.gui.chartPanelDrawers.data.EditorNoteDrawingData;
import log.charter.gui.chartPanelDrawers.drawableShapes.CenteredImage;
import log.charter.gui.chartPanelDrawers.drawableShapes.CenteredText;
import log.charter.gui.chartPanelDrawers.drawableShapes.CenteredTextWithBackground;
import log.charter.gui.chartPanelDrawers.drawableShapes.CenteredTextWithBackgroundAndBorder;
import log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape;
import log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShapeList;
import log.charter.gui.chartPanelDrawers.drawableShapes.Line;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapePositionWithSize;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapeSize;
import log.charter.gui.chartPanelDrawers.drawableShapes.Text;
import log.charter.gui.chartPanelDrawers.drawableShapes.TextWithBackground;
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
import log.charter.util.IntRange;
import log.charter.util.Position2D;

class ModernHighwayDrawer implements HighwayDrawer {
	private static BufferedImage noteIcons[] = new BufferedImage[maxStrings];
	private static BufferedImage noteSelectIcon = null;
	private static BufferedImage harmonicNoteIcons[] = new BufferedImage[maxStrings];
	private static BufferedImage harmonicNoteSelectIcon = null;

	private static BufferedImage hammerOnIcon = null;
	private static BufferedImage pullOffIcon = null;
	private static BufferedImage tapIcon = null;
	private static BufferedImage slapIcon = null;
	private static BufferedImage popIcon = null;

	private static BufferedImage palmMuteIcon = null;
	private static BufferedImage fullMuteIcon = null;

	private static Font toneChangeFont = new Font(Font.SANS_SERIF, Font.ITALIC, toneChangeHeight);
	private static Font anchorFont = new Font(Font.SANS_SERIF, Font.BOLD, anchorInfoHeight - 2);
	private static Font fretFont = new Font(Font.SANS_SERIF, Font.BOLD, 12);
	private static Font smallFretFont = new Font(Font.SANS_SERIF, Font.BOLD, 8);
	private static Font handShapesFont = new Font(Font.SANS_SERIF, Font.BOLD, 12);

	private static BufferedImage generateNoteIcon(final int string) {
		final int size = noteHeight;
		final Color borderColor = ChartPanelColors.getStringBasedColor(StringColorLabelType.LANE, string, maxStrings);
		final Color innerColor = borderColor.darker().darker();

		final BufferedImage icon = new BufferedImage(size, size, BufferedImage.TYPE_4BYTE_ABGR);
		final Graphics2D graphics = (Graphics2D) icon.getGraphics();

		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		final int borderSize = max(2, size / 15);
		final Ellipse2D inner = new Ellipse2D.Double(borderSize, borderSize, size - 2 * borderSize,
				size - 2 * borderSize);
		graphics.setColor(innerColor);
		graphics.fill(inner);

		final Ellipse2D outer = new Ellipse2D.Double(0, 0, size, size);
		final Area area = new Area(outer);
		area.subtract(new Area(inner));
		graphics.setColor(borderColor);
		graphics.fill(area);

		return icon;
	}

	private static BufferedImage generateHarmonicNoteIcon(final int string) {
		final int size = noteHeight;
		final Color borderColor = ChartPanelColors.getStringBasedColor(StringColorLabelType.LANE, string, maxStrings);
		final Color innerColor = borderColor.darker().darker();

		final BufferedImage icon = new BufferedImage(size, size, BufferedImage.TYPE_4BYTE_ABGR);
		final Graphics2D g = (Graphics2D) icon.getGraphics();

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g.setColor(innerColor);
		g.fill(new Polygon(new int[] { 1, size / 2, size - 1, size / 2 }, new int[] { size / 2, size - 1, size / 2, 1 },
				4));

		final Area area = new Area(
				new Polygon(new int[] { 0, size / 2, size, size / 2 }, new int[] { size / 2, size, size / 2, 0 }, 4));
		area.subtract(new Area(new Polygon(new int[] { 2, size / 2, size - 2, size / 2 },
				new int[] { size / 2, size - 2, size / 2, 2 }, 4)));
		g.setColor(borderColor);
		g.fill(area);

		return icon;
	}

	private static BufferedImage generateNoteSelectionIcon() {
		final int size = noteHeight;
		final Color color = ColorLabel.SELECT.color();

		final BufferedImage icon = new BufferedImage(size, size, BufferedImage.TYPE_4BYTE_ABGR);
		final Graphics2D graphics = (Graphics2D) icon.getGraphics();

		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		final Ellipse2D outer = new Ellipse2D.Double(0, 0, size, size);
		final Ellipse2D inner = new Ellipse2D.Double(2, 2, size - 4, size - 4);
		final Area area = new Area(outer);
		area.subtract(new Area(inner));
		graphics.setColor(color);
		graphics.fill(area);

		return icon;
	}

	private static BufferedImage generateHarmonicNoteSelectionIcon() {
		final int size = noteHeight;
		final Color color = ColorLabel.SELECT.color();

		final BufferedImage icon = new BufferedImage(size, size, BufferedImage.TYPE_4BYTE_ABGR);
		final Graphics2D g = (Graphics2D) icon.getGraphics();

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		final Area area = new Area(
				new Polygon(new int[] { 0, size / 2, size, size / 2 }, new int[] { size / 2, size, size / 2, 0 }, 4));
		area.subtract(new Area(new Polygon(new int[] { 2, size / 2, size - 2, size / 2 },
				new int[] { size / 2, size - 2, size / 2, 2 }, 4)));
		g.setColor(color);
		g.fill(area);

		return icon;
	}

	private static BufferedImage generateHammerOnIcon() {
		final int w = noteHeight / 2;
		final int h = noteHeight * 2 / 5;
		final BufferedImage icon = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
		final Graphics2D g = (Graphics2D) icon.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g.setColor(Color.WHITE);
		g.fill(new Polygon(new int[] { 1, w / 2, w - 1 }, new int[] { 1, h - 1, 1 }, 3));

		final Area area = new Area(new Polygon(new int[] { 0, w / 2, w }, new int[] { 0, h, 0 }, 3));
		area.subtract(new Area(new Polygon(new int[] { 2, w / 2, w - 2 }, new int[] { 1, h - 2, 1 }, 3)));
		g.setColor(Color.BLACK);
		g.fill(area);

		return icon;
	}

	private static BufferedImage generatePullOffIcon() {
		final int w = noteHeight / 2;
		final int h = noteHeight * 2 / 5;
		final BufferedImage icon = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
		final Graphics2D g = (Graphics2D) icon.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g.setColor(Color.WHITE);
		g.fill(new Polygon(new int[] { 1, w / 2, w - 1 }, new int[] { h - 1, 1, h - 1 }, 3));

		final Area area = new Area(new Polygon(new int[] { 0, w / 2, w }, new int[] { h, 0, h }, 3));
		area.subtract(new Area(new Polygon(new int[] { 2, w / 2, w - 2 }, new int[] { h - 1, 2, h - 1 }, 3)));
		g.setColor(Color.BLACK);
		g.fill(area);

		return icon;
	}

	private static BufferedImage generateTapIcon() {
		final int w = noteHeight / 2;
		final int h = noteHeight * 2 / 5;
		final BufferedImage icon = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
		final Graphics2D g = (Graphics2D) icon.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g.setColor(Color.BLACK);
		g.fill(new Polygon(new int[] { 1, w / 2, w - 1 }, new int[] { 1, h - 1, 1 }, 3));

		final Area area = new Area(new Polygon(new int[] { 0, w / 2, w }, new int[] { 0, h, 0 }, 3));
		area.subtract(new Area(new Polygon(new int[] { 2, w / 2, w - 2 }, new int[] { 1, h - 2, 1 }, 3)));
		g.setColor(Color.LIGHT_GRAY);
		g.fill(area);
		return icon;
	}

	private static BufferedImage generateSingleLetterIcon(final String letter, final Color color) {
		final int w = noteHeight / 2;
		final int h = noteHeight * 2 / 5;
		final BufferedImage icon = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
		final Graphics2D g = (Graphics2D) icon.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g.setColor(color);
		g.setFont(new Font(Font.DIALOG, Font.BOLD, h + 1));
		g.drawString(letter, 0, h - 1);

		return icon;
	}

	private static Polygon generateX(final int size, final int space) {
		return new Polygon(new int[] { //
				0, size / 2 - space, 0, //
				space, size / 2, size - space - 1, //
				size - 1, size / 2 + space, size - 1, //
				size - space - 1, size / 2, space,//
		}, //
				new int[] { //
						space, size / 2, size - space - 1, //
						size - 1, size / 2 + space, size - 1, //
						size - space - 1, size / 2, space, //
						0, size / 2 - space, 0, //
				}, 12);
	}

	private static BufferedImage generatePalmMuteIcon() {
		final int size = max(16, noteHeight);
		final int space = max(2, size / 8);
		final int borderWidth = max(1, space / 3);
		final Color borderColor = Color.GRAY;
		final Color innerColor = Color.BLACK.brighter().brighter().brighter();

		final BufferedImage icon = new BufferedImage(size, size, BufferedImage.TYPE_4BYTE_ABGR);
		final Graphics2D g = (Graphics2D) icon.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		final Polygon inner = generateX(size - 2, space - 1);
		inner.translate(1, 1);
		g.setColor(innerColor);
		g.fill(inner);

		final Polygon outer = generateX(size, space);
		final Polygon outerSubtract = generateX(size - 4, space - borderWidth);
		outerSubtract.translate(2, 2);
		final Area borderArea = new Area(outer);
		borderArea.subtract(new Area(outerSubtract));
		g.setColor(borderColor);
		g.fill(borderArea);

		return icon;
	}

	private static BufferedImage generateFullMuteIcon() {
		final int size = max(16, noteHeight);
		final int space = max(2, size / 8);
		final int borderWidth = max(1, space / 3);
		final Color borderColor = Color.GRAY;
		final Color innerColor = Color.WHITE;

		final BufferedImage icon = new BufferedImage(size, size, BufferedImage.TYPE_4BYTE_ABGR);
		final Graphics2D g = (Graphics2D) icon.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		final Polygon inner = generateX(size - 2, space - 1);
		inner.translate(1, 1);
		g.setColor(innerColor);
		g.fill(inner);

		final Polygon outer = generateX(size, space);
		final Polygon outerSubtract = generateX(size - 4, space - borderWidth);
		outerSubtract.translate(2, 2);
		final Area borderArea = new Area(outer);
		borderArea.subtract(new Area(outerSubtract));
		g.setColor(borderColor);
		g.fill(borderArea);

		return icon;
	}

	public static void reloadSizes() {
		for (int string = 0; string < maxStrings; string++) {
			final int stringId = stringId(string, maxStrings);
			noteIcons[stringId] = generateNoteIcon(string);
			harmonicNoteIcons[stringId] = generateHarmonicNoteIcon(string);
		}

		noteSelectIcon = generateNoteSelectionIcon();
		harmonicNoteSelectIcon = generateHarmonicNoteSelectionIcon();

		hammerOnIcon = generateHammerOnIcon();
		pullOffIcon = generatePullOffIcon();
		tapIcon = generateTapIcon();
		slapIcon = generateSingleLetterIcon("S", Color.BLACK);
		popIcon = generateSingleLetterIcon("P", Color.BLACK);

		palmMuteIcon = generatePalmMuteIcon();
		fullMuteIcon = generateFullMuteIcon();

		toneChangeFont = new Font(Font.SANS_SERIF, Font.ITALIC, toneChangeHeight);
		anchorFont = new Font(Font.SANS_SERIF, Font.BOLD, anchorInfoHeight - 2);
		fretFont = new Font(Font.SANS_SERIF, Font.BOLD, noteHeight * 2 / 3);
		smallFretFont = new Font(Font.SANS_SERIF, Font.BOLD, noteHeight / 2);
		handShapesFont = new Font(Font.SANS_SERIF, Font.BOLD, handShapesHeight);
	}

	static {
		reloadSizes();
	}

	protected final Color selectColor = ColorLabel.SELECT.color();
	protected final Color[] noteTailColors;

	protected final Font bendValueFont = new Font(Font.DIALOG, Font.BOLD, 12);

	protected final int strings;
	protected final int bendStepSize;
	protected final int time;
	protected final int[] stringPositions;

	protected final DrawableShapeList toneChanges;
	protected final DrawableShapeList anchors;
	protected final DrawableShapeList bendValues;
	protected final DrawableShapeList chordNames;
	protected final DrawableShapeList handShapes;
	protected final DrawableShapeList noteTails;
	protected final DrawableShapeList noteTailSelects;
	protected final DrawableShapeList notes;
	protected final DrawableShapeList noteIds;
	protected final DrawableShapeList slideFrets;

	protected final Graphics g;

	public ModernHighwayDrawer(final Graphics g, final int strings, final int time) {
		this.strings = strings;
		this.time = time;

		stringPositions = new int[strings];
		noteTailColors = new Color[strings];
		for (int i = 0; i < strings; i++) {
			stringPositions[i] = getLaneY(getStringPosition(i, strings));
			noteTailColors[i] = getStringBasedColor(StringColorLabelType.NOTE_TAIL, i, strings);
		}

		bendStepSize = tailHeight / 3;

		toneChanges = new DrawableShapeList();
		anchors = new DrawableShapeList();
		bendValues = new DrawableShapeList();
		chordNames = new DrawableShapeList();
		handShapes = new DrawableShapeList();
		noteTails = new DrawableShapeList();
		noteTailSelects = new DrawableShapeList();
		notes = new DrawableShapeList();
		noteIds = new DrawableShapeList();
		slideFrets = new DrawableShapeList();

		this.g = g;
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
		toneChanges.add(new TextWithBackground(new Position2D(x, toneChangeY), toneChangeFont, "" + toneChange.toneName,
				ColorLabel.TONE_CHANGE, ColorLabel.BASE_TEXT, 2));

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

	private void addAnchorLine(final int x) {
		anchors.add(lineVertical(x, anchorY, lanesBottom, ColorLabel.ANCHOR));
	}

	private void addAnchorText(final Anchor anchor, final int x) {
		final String anchorText = anchor.width == 4 ? anchor.fret + "" : anchor.fret + " - " + anchor.topFret();
		anchors.add(new Text(new Position2D(x + 4, anchorY + 1), anchorFont, anchorText, ColorLabel.ANCHOR));
	}

	private void addAnchorBox(final int x, final ColorLabel color) {
		final int top = anchorY - 1;
		final int bottom = lanesBottom + 1;
		final ShapePositionWithSize anchorPosition = new ShapePositionWithSize(x - 1, top, 2, bottom - top);
		anchors.add(strokedRectangle(anchorPosition, color));
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
		anchors.add(lineVertical(x, anchorY, lanesBottom, ColorLabel.HIGHLIGHT));
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

	private String formatBendValue(final BigDecimal bendValue) {
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

	private int getBendLineY(final int y, BigDecimal bendValue) {
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

	private void addBendValueIcon(final EditorNoteDrawingData note, final int position, final int x, final int y,
			final BigDecimal bendValue) {
		if (note.linkPrevious) {
			return;
		}

		final String text = "ノ" + formatBendValue(bendValue);
		final ShapeSize expectedIconSize = getExpectedSize(g, bendValueFont, text);
		final int minBendPositionAfterHead = xToTimeLength(noteHeight / 2 + expectedIconSize.width / 2);

		Position2D iconPosition;
		if (position > minBendPositionAfterHead) {
			iconPosition = new Position2D(x, y - tailHeight / 2);
		} else {
			final int bendY = y - noteHeight / 2 - expectedIconSize.height / 2;
			iconPosition = new Position2D(x, bendY);
		}

		final Color backgroundColor = getStringBasedColor(StringColorLabelType.LANE, note.string, strings);
		final DrawableShape bendValueIcon = new CenteredTextWithBackground(iconPosition, bendValueFont, text,
				Color.WHITE, backgroundColor);
		bendValues.add(bendValueIcon);
	}

	private void addBendValues(final EditorNoteDrawingData note, final int y) {
		if (note.bendValues == null || note.bendValues.isEmpty()) {
			return;
		}

		boolean linesDrawn = false;
		Position2D lastBendLinePosition = new Position2D(note.x, getBendLineY(y, BigDecimal.ZERO));
		for (final BendValue bendValue : note.bendValues) {
			final int x = timeToX(note.position + bendValue.position(), time);

			final Position2D lineTo = new Position2D(x, getBendLineY(y, bendValue.bendValue));
			if (bendValue.position() == 0) {
				if (!note.linkPrevious) {
					noteTails.add(new Line(lastBendLinePosition, lineTo, Color.WHITE, 2));
					addBendValueIcon(note, bendValue.position(), x, lineTo.y, bendValue.bendValue);
				}
				lastBendLinePosition = lineTo.move(1, 0);

				continue;
			}

			noteTails.add(new Line(lastBendLinePosition, lineTo, Color.WHITE, 2));
			addBendValueIcon(note, bendValue.position(), x, lineTo.y, bendValue.bendValue);
			linesDrawn = true;
			lastBendLinePosition = lineTo.move(1, 0);
		}

		if (linesDrawn) {
			noteTails.add(new Line(lastBendLinePosition,
					new Position2D(note.x + note.length - 2, lastBendLinePosition.y), Color.WHITE, 2));
		}
	}

	private void addNoteTail(final EditorNoteDrawingData note, final int y) {
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

	private void addHopoOrBassTechIcon(final EditorNoteDrawingData note, final int noteY) {
		BufferedImage img = switch (note.bassPickingTech) {
			case POP -> popIcon;
			case SLAP -> img = slapIcon;
			default -> null;
		};
		if (img != null) {
			notes.add(centeredImage(new Position2D(note.x + noteHeight / 2, noteY - (int) (noteHeight / 3)), img));
			return;
		}

		img = switch (note.hopo) {
			case HAMMER_ON -> img = hammerOnIcon;
			case PULL_OFF -> img = pullOffIcon;
			case TAP -> img = tapIcon;
			default -> null;
		};
		if (img == null) {
			return;
		}

		final int iconX = note.x - noteHeight / 2;
		final int iconY = noteY - (note.hopo == HOPO.PULL_OFF ? noteHeight / 2 : noteHeight / 3);
		notes.add(centeredImage(new Position2D(iconX, iconY), img));
	}

	private void addNoteSelection(final EditorNoteDrawingData note, final int y) {
		final BufferedImage icon = switch (note.harmonic) {
			case NORMAL -> harmonicNoteSelectIcon;
			case PINCH -> harmonicNoteSelectIcon;
			default -> noteSelectIcon;
		};

		notes.add(new CenteredImage(new Position2D(note.x, y), icon));
	}

	private void addNoteHeadShape(final EditorNoteDrawingData note, final int y) {
		final int stringId = stringId(note.string, strings);
		final BufferedImage icon = switch (note.harmonic) {
			case NORMAL -> harmonicNoteIcons[stringId];
			case PINCH -> harmonicNoteIcons[stringId];
			default -> noteIcons[stringId];
		};

		notes.add(new CenteredImage(new Position2D(note.x, y), icon));
		if (note.harmonic == Harmonic.PINCH) {
			final int x0 = note.x - noteHeight / 2;
			final int y0 = y - noteHeight / 2;
			final int y1 = y + noteHeight / 2;
			final Color color = getStringBasedColor(StringColorLabelType.LANE, note.string, strings);
			notes.add(new Line(new Position2D(x0, y0), new Position2D(x0, y1), color, 3));
		}

		if (note.selected) {
			addNoteSelection(note, y);
		}
	}

	private void addMuteIcon(final EditorNoteDrawingData note, final int y) {
		final BufferedImage icon = switch (note.mute) {
			case FULL -> fullMuteIcon;
			case PALM -> palmMuteIcon;
			default -> null;
		};

		if (icon == null) {
			return;
		}

		notes.add(new CenteredImage(new Position2D(note.x, y), icon));
	}

	private void addFretNumber(final EditorNoteDrawingData note, final int y) {
		final Font font = note.fretNumber < 10 ? fretFont : smallFretFont;

		if (note.mute == Mute.FULL) {
			notes.add(new CenteredTextWithBackgroundAndBorder(new Position2D(note.x, y), font, note.fretNumber + "",
					Color.WHITE, Color.GRAY, Color.LIGHT_GRAY));
		} else {
			notes.add(new CenteredText(new Position2D(note.x, y), font, note.fretNumber + "", Color.WHITE));
		}
	}

	private void addNoteShape(final EditorNoteDrawingData note, final int y) {
		final ShapePositionWithSize position = new ShapePositionWithSize(note.x, y, noteHeight, noteHeight)//
				.centered();

		addNoteTail(note, y);
		addBendValues(note, y);

		if (note.linkPrevious && !note.wrongLink) {
			return;
		}

		addNoteHeadShape(note, y);
		addMuteIcon(note, y);
		addFretNumber(note, y);
		addHopoOrBassTechIcon(note, y);

		if (note.accent) {
			final Color accentColor = getStringBasedColor(StringColorLabelType.NOTE_ACCENT, note.string, strings);
			notes.add(strokedRectangle(position.resized(-2, -2, 3, 3), accentColor, 1));
		}
	}

	private void addSimpleNote(final EditorNoteDrawingData note) {
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
			chordNames
					.add(new Text(new Position2D(x + 2, lanesTop - 1), fretFont, chordName, ColorLabel.BASE_DARK_TEXT));
		}
	}

	@Override
	public void addHandShape(final int x, final int length, final boolean selected, final boolean highlighted,
			final HandShape handShape, final ChordTemplate chordTemplate) {
		final ShapePositionWithSize position = new ShapePositionWithSize(x, lanesBottom + 1, length, handShapesHeight);
		final ColorLabel fillColor = chordTemplate.arpeggio ? ColorLabel.HAND_SHAPE_ARPEGGIO : ColorLabel.HAND_SHAPE;
		handShapes.add(filledRectangle(position, fillColor));

		if (highlighted) {
			handShapes.add(strokedRectangle(position.resized(-1, -1, 1, 1), ColorLabel.HIGHLIGHT));
		} else if (selected) {
			handShapes.add(strokedRectangle(position.resized(-1, -1, 1, 1), ColorLabel.SELECT));
		}

		String chordName = chordTemplate.chordName;
		if (showChordIds) {
			chordName = (chordName == null || chordName.isBlank()) ? "[" + handShape.templateId + "]"
					: chordName + " [" + handShape.templateId + "]";
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
	public void draw(final Graphics g) {
		g.setFont(anchorFont);
		toneChanges.draw(g);
		anchors.draw(g);
		chordNames.draw(g);

		noteTails.draw(g);
		noteTailSelects.draw(g);
		notes.draw(g);
		slideFrets.draw(g);
		bendValues.draw(g);

		handShapes.draw(g);

		g.setFont(bendValueFont);
		noteIds.draw(g);
	}
}