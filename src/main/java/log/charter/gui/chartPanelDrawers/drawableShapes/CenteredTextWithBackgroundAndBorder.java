package log.charter.gui.chartPanelDrawers.drawableShapes;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.util.Position2D;

public class CenteredTextWithBackgroundAndBorder implements DrawableShape {
	public static ShapePositionWithSize getExpectedPositionAndSize(final Graphics2D g, final Position2D position,
			final Font font, final String text) {
		return CenteredTextWithBackground.getExpectedPositionAndSize(g, position, font, text).resized(-1, -1, 2, 2);
	}

	public static ShapeSize getExpectedSize(final Graphics2D g, final Font font, final String text) {
		final ShapeSize innerSize = CenteredTextWithBackground.getExpectedSize(g, font, text);
		return new ShapeSize(innerSize.width + 2, innerSize.height + 2);
	}

	private final CenteredTextWithBackground centeredTextWithBackground;
	private final Color borderColor;

	public CenteredTextWithBackgroundAndBorder(final Position2D position, final Font font, final String text,
			final ColorLabel textColor, final ColorLabel backgroundColor, final ColorLabel borderColor) {
		this(position, font, text, textColor.color(), backgroundColor.color(), borderColor.color());
	}

	public CenteredTextWithBackgroundAndBorder(final Position2D position, final Font font, final String text,
			final Color textColor, final Color backgroundColor, final Color borderColor) {
		centeredTextWithBackground = new CenteredTextWithBackground(position, font, text, textColor, backgroundColor);
		this.borderColor = borderColor;
	}

	@Override
	public void draw(final Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		draw(g, getPositionAndSize(g));
	}

	public ShapePositionWithSize getPositionAndSize(final Graphics2D g) {
		return centeredTextWithBackground.getPositionAndSize(g).resized(-1, -1, 2, 2);
	}

	public void draw(final Graphics2D g, final ShapePositionWithSize positionAndSize) {
		if (borderColor != null) {
			g.setColor(borderColor);
			g.drawRect(positionAndSize.x, positionAndSize.y, positionAndSize.width - 1, positionAndSize.height - 1);
		}

		centeredTextWithBackground.draw(g, positionAndSize.resized(1, 1, -2, -2));
	}

}
