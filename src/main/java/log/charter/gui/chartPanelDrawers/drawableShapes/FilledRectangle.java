package log.charter.gui.chartPanelDrawers.drawableShapes;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import log.charter.gui.ChartPanelColors.ColorLabel;

public class FilledRectangle implements DrawableShape {
	private final ShapePositionWithSize position;
	private final Color color;
	private final boolean rounded;

	public FilledRectangle(final int x, final int y, final int width, final int height, final ColorLabel color) {
		this(new ShapePositionWithSize(x, y, width, height), color.color(), false);
	}

	public FilledRectangle(final int x, final int y, final int width, final int height, final Color color) {
		this(new ShapePositionWithSize(x, y, width, height), color, false);
	}

	public FilledRectangle(final ShapePositionWithSize position, final Color color) {
		this(position, color, false);
	}

	public FilledRectangle(final ShapePositionWithSize position, final Color color, final boolean rounded) {
		this.position = position;
		this.color = color;
		this.rounded = rounded;
	}

	@Override
	public void draw(final Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(color);

		if (rounded) {
			g.fillRoundRect(position.x - 2, position.y - 2, position.width + 4, position.height + 4, 5, 5);
		} else {
			g.fillRect(position.x, position.y, position.width, position.height);
		}
	}

	public FilledRectangle centered() {
		return new FilledRectangle(position.centered(), color, rounded);
	}
}