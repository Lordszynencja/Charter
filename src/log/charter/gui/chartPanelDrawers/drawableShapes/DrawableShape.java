package log.charter.gui.chartPanelDrawers.drawableShapes;

import java.awt.Color;
import java.awt.Graphics;

public interface DrawableShape {
	public void draw(final Graphics g);

	// Lines
	public static DrawableShape line(final ShapePositionWithSize startPosition, final ShapePositionWithSize endPosition,
			final Color color) {
		return new Line(startPosition, endPosition, color);
	}

	public static DrawableShape lineVertical(final int x, final int y0, final int y1, final Color color) {
		return new Line(new ShapePosition(x, y0), new ShapePosition(x, y1), color);
	}

	public static DrawableShape lineHorizontal(final int x0, final int x1, final int y, final Color color) {
		return new Line(new ShapePosition(x0, y), new ShapePosition(x1, y), color);
	}

	// Rectangles
	public static DrawableShape filledRectangle(final ShapePositionWithSize position, final Color color) {
		return new FilledRectangle(position, color);
	}

	public static DrawableShape strokedRectangle(final ShapePositionWithSize position, final Color color) {
		return new StrokedRectangle(position, color);
	}

	// Arcs
	public static DrawableShape filledArc(final ShapePositionWithSize position, final int startAngle,
			final int arcAngle, final Color color) {
		return new FilledArc(position, startAngle, arcAngle, color);
	}

	// Texts
	public static DrawableShape text(final ShapePosition position, final String text, final Color color) {
		return new Text(position, text, color);
	}

	public static DrawableShape centeredTextWithBackground(final ShapePosition position, final String text,
			final Color backgroundColor, final Color textColor) {
		return new CenteredTextWithBackground(position, text, backgroundColor, textColor);
	}
}
