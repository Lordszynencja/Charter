package log.charter.gui.chartPanelDrawers.drawableShapes;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import log.charter.data.config.ChartPanelColors.ColorLabel;
import log.charter.gui.components.preview3D.glUtils.Point2D;
import log.charter.util.data.Position2D;

public class CenteredText implements DrawableShape {
	public static ShapePositionWithSizeDouble getExpectedPositionAndSize(final Graphics2D g, final Position2D position,
			final Font font, final String text) {
		final Point2D expectedSize = getExpectedSize(g, font, text);

		return new ShapePositionWithSizeDouble(position.x - expectedSize.x / 2, position.y - expectedSize.y / 2,
				expectedSize.x, expectedSize.y);
	}

	public static Point2D getExpectedSize(final Graphics2D g, final Font font, final String text) {
		g.setFont(font);

		final int width = g.getFontMetrics().stringWidth(text);
		final int height = g.getFontMetrics().getAscent() - g.getFontMetrics().getDescent();

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
	public void draw(final Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		draw(g, getPositionWithSize(g));
	}

	public ShapePositionWithSizeDouble getPositionWithSize(final Graphics2D g) {
		return getExpectedPositionAndSize(g, position, font, text);
	}

	public void draw(final Graphics2D g, final ShapePositionWithSizeDouble positionAndSize) {
		g.setFont(font);
		g.setColor(textColor);
		g.drawString(text, (float) (positionAndSize.x), (float) (positionAndSize.y + positionAndSize.height));
	}
}
