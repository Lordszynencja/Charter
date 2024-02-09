package log.charter.gui.chartPanelDrawers.drawableShapes;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.components.preview3D.glUtils.Point2D;
import log.charter.util.Position2D;

public class CenteredText implements DrawableShape {
	public static ShapePositionWithSizeDouble getExpectedPositionAndSize(final Graphics g, final Position2D position,
			final Font font, final String text) {
		final Point2D expectedSize = getExpectedSize(g, font, text);

		return new ShapePositionWithSizeDouble(position.x - expectedSize.x / 2, position.y - expectedSize.y / 2,
				expectedSize.x, expectedSize.y);
	}

	public static Point2D getExpectedSize(final Graphics g, final Font font, final String text) {
		final Graphics2D g2 = (Graphics2D) g;
		g.setFont(font);

		final int width = g2.getFontMetrics().stringWidth(text);
		final int height = g2.getFontMetrics().getAscent() - g2.getFontMetrics().getDescent();

		return new Point2D(width, height);
	}

	final Position2D position;
	final Font font;
	final String text;
	final Color textColor;

	public CenteredText(final Position2D position, final Font font, final String text, final ColorLabel textColor) {
		this(position, font, text, textColor.color());
	}

	public CenteredText(final Position2D position, final Font font, final String text, final Color textColor) {
		this.position = position;
		this.font = font;
		this.text = text;
		this.textColor = textColor;
	}

	@Override
	public void draw(final Graphics g) {
		final Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		draw(g2, getPositionWithSize(g));
	}

	public ShapePositionWithSizeDouble getPositionWithSize(final Graphics g) {
		return getExpectedPositionAndSize(g, position, font, text);
	}

	public void draw(final Graphics2D g, final ShapePositionWithSizeDouble positionAndSize) {
		g.setFont(font);
		g.setColor(textColor);
		g.drawString(text, (float) (positionAndSize.x), (float) (positionAndSize.y + positionAndSize.height));
	}
}
