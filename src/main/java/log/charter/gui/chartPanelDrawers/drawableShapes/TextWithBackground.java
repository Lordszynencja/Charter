package log.charter.gui.chartPanelDrawers.drawableShapes;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;

import log.charter.data.config.ChartPanelColors.ColorLabel;
import log.charter.util.data.Position2D;

public class TextWithBackground implements DrawableShape {
	public static ShapeSize getExpectedSize(final Graphics2D g, final Font font, final String text, final int space) {
		return Text.getExpectedSize(g, font, text).resizeBy(4 * space, 4 * space + 1);
	}

	private Position2D position;
	private final Text text;
	private Color backgroundColor;
	private int space;
	private int arcSize = 5;

	public TextWithBackground() {
		this(new Position2D(0, 0), new Font(Font.DIALOG, 0, 10), "", Color.BLACK, Color.WHITE, 2);
	}

	public TextWithBackground(final Position2D position, final Font font, final String text, final ColorLabel textColor,
			final ColorLabel backgroundColor) {
		this(position, font, text, textColor.color(), backgroundColor.color(), 1);
	}

	public TextWithBackground(final Position2D position, final Font font, final String text, final ColorLabel textColor,
			final ColorLabel backgroundColor, final int space) {
		this(position, font, text, textColor.color(), backgroundColor.color(), space);
	}

	public TextWithBackground(final Position2D position, final Font font, final String text, final Color textColor,
			final Color backgroundColor) {
		this(position, font, text, textColor, backgroundColor, 1);
	}

	public TextWithBackground(final Position2D position, final Font font, final String text, final Color textColor,
			final Color backgroundColor, final int space) {
		this.position = position;
		this.text = new Text(position.move(space, space), font, text, textColor);
		this.backgroundColor = backgroundColor;
		this.space = space;
	}

	public TextWithBackground position(final Position2D position) {
		this.position = position;
		text.position(position.move(space, space));
		return this;
	}

	public TextWithBackground font(final Font font) {
		text.font(font);
		return this;
	}

	public TextWithBackground text(final String text) {
		this.text.text(text);
		return this;
	}

	public TextWithBackground color(final Color color) {
		text.color(color);
		return this;
	}

	public TextWithBackground color(final ColorLabel color) {
		return color(color.color());
	}

	public TextWithBackground backgroundColor(final Color backgroundColor) {
		this.backgroundColor = backgroundColor;
		return this;
	}

	public TextWithBackground backgroundColor(final ColorLabel backgroundColor) {
		return backgroundColor(backgroundColor.color());
	}

	public TextWithBackground space(final int space) {
		this.space = space;
		text.position(position.move(space, space));
		return this;
	}

	public TextWithBackground arcSize(final int arcSize) {
		this.arcSize = arcSize;
		return this;
	}

	@Override
	public void draw(final Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		draw(g, getPositionWithSize(g));
	}

	public ShapePositionWithSize getPositionWithSize(final Graphics2D g) {
		final ShapePositionWithSize textSize = text.getPositionWithSize(g);
		return new ShapePositionWithSize(position.x, position.y, textSize.width + 4 * space,
				textSize.height + 4 * space + 1);
	}

	public void draw(final Graphics2D g, final ShapePositionWithSize positionAndSize) {
		final Shape roundedRect = new RoundRectangle2D.Double(positionAndSize.x, positionAndSize.y,
				positionAndSize.width, positionAndSize.height, arcSize, arcSize);

		g.setColor(backgroundColor);
		g.fill(roundedRect);

		text.draw(g, positionAndSize.resized(2 * space, 2 * space, -4 * space, -4 * space - 1));
	}
}
