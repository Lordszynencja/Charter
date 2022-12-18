package log.charter.gui.chartPanelDrawers.drawableShapes;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import log.charter.util.Position2D;

class CenteredImage implements DrawableShape {
	private final Position2D position;
	private final BufferedImage image;

	CenteredImage(final Position2D position, final BufferedImage image) {
		this.position = position;
		this.image = image;
	}

	@Override
	public void draw(final Graphics g) {
		g.drawImage(image, position.x - image.getWidth() / 2, position.y - image.getHeight() / 2, null);
	}

}
