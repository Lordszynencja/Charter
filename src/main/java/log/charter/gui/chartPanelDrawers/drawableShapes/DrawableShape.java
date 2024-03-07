package log.charter.gui.chartPanelDrawers.drawableShapes;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.util.Position2D;

public interface DrawableShape {
	public void draw(final Graphics2D g);

	// Lines
	public static DrawableShape line(final Position2D startPosition, final Position2D endPosition, final Color color) {
		return new Line(startPosition, endPosition, color);
	}

	public static DrawableShape line(final Position2D startPosition, final Position2D endPosition, final Color color,
			final int thickness) {
		return new Line(startPosition, endPosition, color, thickness);
	}

	public static DrawableShape lineVertical(final int x, final int y0, final int y1, final Color color) {
		return new Line(new Position2D(x, y0), new Position2D(x, y1), color);
	}

	public static DrawableShape lineVertical(final int x, final int y0, final int y1, final ColorLabel color) {
		return line(new Position2D(x, y0), new Position2D(x, y1), color.color());
	}

	public static DrawableShape lineHorizontal(final int x0, final int x1, final int y, final Color color) {
		return new Line(new Position2D(x0, y), new Position2D(x1, y), color);
	}

	// Triangles
	public static DrawableShape filledTriangle(final Position2D a, final Position2D b, final Position2D c,
			final Color color) {
		return new FilledTriangle(a, b, c, color);
	}

	public static DrawableShape filledTriangle(final Position2D a, final Position2D b, final Position2D c,
			final ColorLabel color) {
		return filledTriangle(a, b, c, color.color());
	}

	public static DrawableShape strokedTriangle(final Position2D a, final Position2D b, final Position2D c,
			final Color color) {
		return new StrokedTriangle(a, b, c, color);
	}

	public static DrawableShape strokedTriangle(final Position2D a, final Position2D b, final Position2D c,
			final Color color, final int thickness) {
		return new StrokedTriangle(a, b, c, color, thickness);
	}

	// Rectangles
	public static DrawableShape filledRectangle(final ShapePositionWithSize position, final Color color) {
		return new FilledRectangle(position, color);
	}

	public static DrawableShape filledRectangle(final ShapePositionWithSize position, final ColorLabel color) {
		return filledRectangle(position, color.color());
	}

	public static DrawableShape filledRectangle(final ShapePositionWithSize position, final Color color,
			final boolean rounded) {
		return new FilledRectangle(position, color, rounded);
	}

	public static DrawableShape filledRectangle(final ShapePositionWithSize position, final ColorLabel color,
			final boolean rounded) {
		return filledRectangle(position, color.color(), rounded);
	}

	public static DrawableShape strokedRectangle(final ShapePositionWithSize position, final Color color) {
		return new StrokedRectangle(position, color);
	}

	public static DrawableShape strokedRectangle(final ShapePositionWithSize position, final ColorLabel color) {
		return strokedRectangle(position, color.color());
	}

	public static DrawableShape strokedRectangle(final ShapePositionWithSize position, final ColorLabel color,
			final int thickness) {
		return strokedRectangle(position, color.color(), thickness);
	}

	public static DrawableShape strokedRectangle(final ShapePositionWithSize position, final Color color,
			final int thickness) {
		return new StrokedRectangle(position, color, thickness);
	}

	// Diamonds
	public static DrawableShape filledDiamond(final Position2D position, final int radius, final Color color) {
		return new FilledDiamond(position, radius, color);
	}

	// Polygons
	public static DrawableShape filledPolygon(final List<Position2D> positions, final Color color) {
		return new FilledPolygon(positions.toArray(new Position2D[0]), color);
	}

	public static DrawableShape filledPolygon(final Color color, final Position2D... positions) {
		return new FilledPolygon(positions, color);
	}

	public static DrawableShape strokedPolygon(final List<Position2D> positions, final Color color) {
		return new StrokedPolygon(positions.toArray(new Position2D[0]), color);
	}

	public static DrawableShape strokedPolygon(final Color color, final Position2D... positions) {
		return new StrokedPolygon(positions, color);
	}

	// Arcs
	public static DrawableShape filledArc(final ShapePositionWithSize position, final int startAngle,
			final int arcAngle, final Color color) {
		return new FilledArc(position, startAngle, arcAngle, color);
	}

	// Ovals
	public static DrawableShape filledOval(final ShapePositionWithSize position, final Color color) {
		return new FilledOval(position, color);
	}

	public static DrawableShape filledOval(final ShapePositionWithSize position, final ColorLabel color) {
		return filledOval(position, color.color());
	}

	// Texts
	public static CenteredTextWithBackgroundAndBorder centeredTextWithBackground(final Position2D position,
			final Font font, final String text, final Color textColor, final Color backgroundColor,
			final Color borderColor) {
		return new CenteredTextWithBackgroundAndBorder(position, font, text, backgroundColor, textColor, borderColor);
	}

	// Images
	public static DrawableShape centeredImage(final Position2D position, final BufferedImage image) {
		return new CenteredImage(position, image);
	}

	// Clip
	public static DrawableShape clippedShapes(final ShapePositionWithSize position, final List<DrawableShape> shapes) {
		return new ClippedShapes(position, shapes);
	}
}
