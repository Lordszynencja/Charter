package log.charter.gui.chartPanelDrawers.instruments.guitar.highway;

import static log.charter.data.config.GraphicalConfig.anchorInfoHeight;
import static log.charter.data.config.GraphicalConfig.noteHeight;
import static log.charter.data.config.GraphicalConfig.noteWidth;
import static log.charter.gui.ChartPanelColors.getStringBasedColor;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.centeredImage;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.filledOval;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.filledPolygon;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.filledRectangle;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.filledTriangle;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.line;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.strokedRectangle;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.List;

import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.ChartPanelColors.StringColorLabelType;
import log.charter.gui.chartPanelDrawers.data.EditorNoteDrawingData;
import log.charter.gui.chartPanelDrawers.drawableShapes.CenteredTextWithBackgroundAndBorder;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapePositionWithSize;
import log.charter.util.collections.ArrayList2;
import log.charter.util.data.IntRange;
import log.charter.util.data.Position2D;

class SquareHighwayDrawer extends DefaultHighwayDrawer {
	public SquareHighwayDrawer(final Graphics2D g, final int strings, final double time) {
		super(g, strings, time);
	}

	@Override
	protected Font defineAnchorFont() {
		return new Font(Font.DIALOG, Font.BOLD, anchorInfoHeight);
	}

	@Override
	protected Font defineBendFont() {
		return new Font(Font.DIALOG, Font.BOLD, 12);
	}

	@Override
	protected Font defineFretFont() {
		return new Font(Font.SANS_SERIF, Font.BOLD, 12);
	}

	@Override
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

	@Override
	protected BufferedImage defineMuteImage() {
		final int w = muteMarker.getWidth();
		final int h = muteMarker.getHeight();
		final BufferedImage scaledMuteImage = new BufferedImage(noteWidth - noteWidth / 4, noteHeight,
				BufferedImage.TYPE_INT_ARGB);
		final AffineTransform at = new AffineTransform();
		at.scale(1.0 * (noteWidth - noteWidth / 4) / w, 1.0 * noteHeight / h);
		final AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		scaleOp.filter(muteMarker, scaledMuteImage);
		return scaledMuteImage;
	}

	@Override
	protected Color getNoteColor(final EditorNoteDrawingData note) {
		final Color color = noteColors[note.string];
		return note.wrongLink ? applyWrongLink(color) : color;
	}

	@Override
	protected void addNoteShape(final EditorNoteDrawingData note, final int y) {
		final ShapePositionWithSize position = new ShapePositionWithSize(note.x, y, noteWidth, noteHeight)//
				.centered();

		addNoteTail(note, y);
		addBendValues(note, y);

		if (note.linkPrevious && !note.wrongLink) {
			return;
		}

		addNormalNoteShape(y, note);

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

		switch (note.mute) {
			case PALM:
				addPalmMute(note, y);
				break;
			case FULL:
				addFullMute(note, y);
				break;
			case NONE:
			default:
				break;
		}

		noteFrets.add(new CenteredTextWithBackgroundAndBorder(new Position2D(note.x + 1, y), fretFont, note.fret,
				Color.BLACK, Color.WHITE, Color.BLACK));

		if (note.accent) {
			final Color accentColor = getStringBasedColor(StringColorLabelType.NOTE_ACCENT, note.string, strings);
			notes.add(strokedRectangle(position.resized(-2, -2, 3, 3), accentColor, 1));
		}

		if (note.highlighted) {
			selects.add(strokedRectangle(position.resized(0, 0, -1, -1), ColorLabel.HIGHLIGHT, 2));
		} else if (note.selected) {
			selects.add(strokedRectangle(position.resized(0, 0, -1, -1), selectColor, 2));
		}
	}

	@Override
	protected void addNormalNoteShape(final int y, final EditorNoteDrawingData note) {
		final ShapePositionWithSize position = new ShapePositionWithSize(note.x, y, noteWidth, noteHeight)//
				.centered();

		notes.add(filledRectangle(position, getNoteColor(note)));
		notes.add(strokedRectangle(position.resized(1, 1, -2, -2), getNoteColor(note).brighter(), 2));
	}

	@Override
	protected void addHammerOnShape(final int y, final EditorNoteDrawingData note) {
		final ShapePositionWithSize position = new ShapePositionWithSize(note.x, y, noteWidth, noteHeight)//
				.centered();

		notes.add(filledRectangle(position.resized(2, 2, -4, -4), Color.BLACK));

		final Position2D a = new Position2D(note.x, y + noteHeight / 2);
		final Position2D b = new Position2D(note.x - noteWidth / 2, y - noteHeight / 2);
		final Position2D c = new Position2D(note.x + noteWidth / 2, y - noteHeight / 2);
		notes.add(filledTriangle(a, b, c, getNoteColor(note).brighter()));
		notes.add(filledTriangle(a.move(0, -3), b.move(3, 2), c.move(-3, 2), Color.WHITE));
	}

	@Override
	protected void addPullOffShape(final int y, final EditorNoteDrawingData note) {
		final ShapePositionWithSize position = new ShapePositionWithSize(note.x, y, noteWidth, noteHeight)//
				.centered();

		notes.add(filledRectangle(position.resized(2, 2, -4, -4), Color.BLACK));

		final Position2D a = new Position2D(note.x, y - noteHeight / 2);
		final Position2D b = new Position2D(note.x - noteWidth / 2, y + noteHeight / 2);
		final Position2D c = new Position2D(note.x + noteWidth / 2, y + noteHeight / 2);
		notes.add(filledTriangle(a, b, c, getNoteColor(note).brighter()));
		notes.add(filledTriangle(a.move(0, 3), b.move(3, -2), c.move(-3, -2), Color.WHITE));
	}

	@Override
	protected void addTapShape(final int y, final EditorNoteDrawingData note) {
		final List<Position2D> positions = new ArrayList2<>(//
				new Position2D(note.x - noteWidth / 2, y - noteHeight / 2), // top left
				new Position2D(note.x - noteWidth / 2 + 4, y - noteHeight / 2), // top left inset
				new Position2D(note.x, y + noteHeight / 2 - 4), // middle inset
				new Position2D(note.x + noteWidth / 2 - 4, y - noteHeight / 2), // top right inset
				new Position2D(note.x + noteWidth / 2, y - noteHeight / 2), // top right
				new Position2D(note.x, y + noteHeight / 2)); // middle

		notes.add(filledPolygon(positions, Color.BLACK));
	}

	@Override
	protected void addHarmonicShape(final int y, final EditorNoteDrawingData note) {
		final ShapePositionWithSize position = new ShapePositionWithSize(note.x, y, noteWidth, noteHeight)//
				.centered();

		notes.add(filledRectangle(position.resized(2, 2, -4, -4), Color.BLACK));

		final int harmonicSize = Math.min(noteWidth, noteHeight) + 2;
		final ShapePositionWithSize harmonicPosition = new ShapePositionWithSize(note.x, y, harmonicSize, harmonicSize)
				.centered();
		notes.add(filledOval(harmonicPosition, new Color(224, 224, 224)));
		notes.add(filledOval(harmonicPosition.resized(5, 5, -10, -10), Color.BLACK));
		notes.add(filledOval(harmonicPosition.resized(10, 10, -20, -20), new Color(224, 224, 224)));
	}

	@Override
	protected void addPinchHarmonicShape(final int y, final EditorNoteDrawingData note) {
		final ShapePositionWithSize position = new ShapePositionWithSize(note.x, y, noteWidth, noteHeight)//
				.centered();

		notes.add(filledOval(position, Color.BLACK));
		notes.add(filledOval(position.resized(1, 1, -2, -2), getNoteColor(note)));
		notes.add(filledOval(position.resized(3, 3, -6, -6), Color.BLACK));

		final int harmonicSize = Math.min(noteWidth, noteHeight) - 3;
		final ShapePositionWithSize harmonicPosition = new ShapePositionWithSize(note.x, y, harmonicSize, harmonicSize)
				.centered();

		notes.add(filledOval(harmonicPosition, getNoteColor(note)));
		notes.add(filledOval(harmonicPosition.resized(5, 5, -10, -10), Color.BLACK));
	}

	@Override
	protected void addPalmMute(final EditorNoteDrawingData note, final int y) {
		notes.add(centeredImage(new Position2D(note.x, y), palmMuteImage));
	}

	@Override
	protected void addFullMute(final EditorNoteDrawingData note, final int y) {
		final ShapePositionWithSize position = new ShapePositionWithSize(note.x, y, noteWidth, noteHeight)//
				.centered();

		notes.add(filledRectangle(position.resized(2, 2, -4, -4), Color.BLACK));
		notes.add(centeredImage(new Position2D(note.x, y), muteImage));
	}

	@Override
	protected void addSlideCommon(final EditorNoteDrawingData note, final int y, final Color outlineColor,
			final Color fretColor) {
		addNormalNoteTailShape(note, y);

		IntRange topBottom = getDefaultTailTopBottom(y);
		topBottom = new IntRange(topBottom.min - 1, topBottom.max);
		final int slideStartY = note.slideTo < note.fretNumber ? topBottom.min : topBottom.max;
		final int slideEndY = note.slideTo < note.fretNumber ? topBottom.max : topBottom.min;
		final Position2D slideStart = new Position2D(note.x + noteWidth / 2, slideStartY);
		final Position2D slideEnd = new Position2D(note.x + note.length, slideEndY);

		final int lineStartYOffset = note.slideTo < note.fretNumber ? 1 : -1;
		final int lineEndYOffset = -lineStartYOffset;
		notes.add(line(slideStart.move(0, lineStartYOffset), slideEnd.move(0, lineEndYOffset), Color.BLACK, 2));

		slideFrets.add(new CenteredTextWithBackgroundAndBorder(slideEnd, fretFont, note.slideTo + "", fretColor,
				outlineColor, Color.BLACK));
	}
}