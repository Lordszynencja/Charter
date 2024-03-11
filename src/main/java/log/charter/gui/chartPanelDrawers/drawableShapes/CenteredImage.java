package log.charter.gui.chartPanelDrawers.drawableShapes;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import log.charter.util.data.Position2D;

public class CenteredImage implements DrawableShape {
	private final Position2D position;
	private final BufferedImage image;

	public CenteredImage(final Position2D position, final BufferedImage image) {
		this.position = position;
		this.image = image;
	}

	@Override
	public void draw(final Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		final int x = position.x - image.getWidth() / 2;
		final int y = position.y - image.getHeight() / 2;
		g.drawImage(image, x, y, null);
	}

}
