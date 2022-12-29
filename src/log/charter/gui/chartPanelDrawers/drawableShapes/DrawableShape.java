package log.charter.gui.chartPanelDrawers.drawableShapes;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.util.Position2D;

public interface DrawableShape {
	public void draw(final Graphics g);

	// Lines
	public static DrawableShape line(final Position2D startPosition, final Position2D endPosition, final Color color) {
		return new Line(startPosition, endPosition, color);
	}

	public static DrawableShape lineVertical(final int x, final int y0, final int y1, final Color color) {
		return new Line(new Position2D(x, y0), new Position2D(x, y1), color);
	}

	public static DrawableShape lineHorizontal(final int x0, final int x1, final int y, final Color color) {
		return new Line(new Position2D(x0, y), new Position2D(x1, y), color);
	}

	// Triangles
	public static DrawableShape filledTriangle(final Position2D a, final Position2D b, final Position2D c,
			final Color color) {
		return new FilledTriangle(a, b, c, color);
	}

	public static DrawableShape strokedTriangle(final Position2D a, final Position2D b, final Position2D c,
			final Color color) {
		return new StrokedTriangle(a, b, c, color);
	}

	// Rectangles
	public static DrawableShape filledRectangle(final ShapePositionWithSize position, final Color color) {
		return new FilledRectangle(position, color);
	}

	public static DrawableShape strokedRectangle(final ShapePositionWithSize position, final Color color) {
		return new StrokedRectangle(position, color);
	}

	// Diamonds
	public static DrawableShape filledDiamond(final Position2D position, final int radius, final Color color) {
		return new FilledDiamond(position, radius, color);
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
	public static DrawableShape text(final Position2D position, final String text, final ColorLabel color) {
		return text(position, text, color.color());
	}

	public static DrawableShape text(final Position2D position, final String text, final Color color) {
		return new Text(position, text, color);
	}

	public static DrawableShape textWithBackground(final Position2D position, final String text,
			final ColorLabel backgroundColor, final ColorLabel textColor) {
		return new TextWithBackground(position, text, backgroundColor.color(), textColor.color());
	}

	public static DrawableShape centeredTextWithBackground(final Position2D position, final String text,
			final Color backgroundColor, final Color textColor) {
		return new CenteredTextWithBackground(position, text, backgroundColor, textColor);
	}

	// Images
	public static DrawableShape centeredImage(final Position2D position, final BufferedImage image) {
		return new CenteredImage(position, image);
	}

}
