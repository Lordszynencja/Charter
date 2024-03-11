package log.charter.gui.chartPanelDrawers.drawableShapes;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import log.charter.gui.components.preview3D.glUtils.Point2D;
import log.charter.util.data.Position2D;

public class CenteredTextWithBackground implements DrawableShape {
	public static ShapePositionWithSize getExpectedPositionAndSize(final Graphics2D g, final Position2D position,
			final Font font, final String text) {
		final ShapePositionWithSizeDouble expectedSize = CenteredText.getExpectedPositionAndSize(g, position, font,
				text);

		return expectedSize.asInteger().resized(-1, -1, 2, 4);
	}

	public static ShapeSize getExpectedSize(final Graphics2D g, final Font font, final String text) {
		final Point2D textSize = CenteredText.getExpectedSize(g, font, text);

		return new ShapeSize((int) textSize.x + 2, (int) textSize.y + 4);
	}

	final CenteredText centeredText;
	final Color backgroundColor;

	public CenteredTextWithBackground(final Position2D position, final Font font, final String text,
			final Color textColor, final Color backgroundColor) {
		centeredText = new CenteredText(position, font, text, textColor);
		this.backgroundColor = backgroundColor;
	}

	@Override
	public void draw(final Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		draw(g, getPositionAndSize(g));
	}

	public ShapePositionWithSize getPositionAndSize(final Graphics2D g) {
		return centeredText.getPositionWithSize(g).asInteger().resized(-1, -1, 2, 4);
	}

	public void draw(final Graphics2D g, final ShapePositionWithSize positionAndSize) {
		if (backgroundColor != null) {
			g.setColor(backgroundColor);
			g.fillRect(positionAndSize.x, positionAndSize.y, positionAndSize.width, positionAndSize.height);
		}

		centeredText.draw(g, positionAndSize.resized(1, 1, -2, -4).asDouble());
	}

}
