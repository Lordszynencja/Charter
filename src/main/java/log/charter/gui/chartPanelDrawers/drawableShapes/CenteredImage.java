package log.charter.gui.chartPanelDrawers.drawableShapes;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import log.charter.util.data.Position2D;

public class CenteredImage implements DrawableShape {
	private final Position2D position;
	private final BufferedImage image;
	private final Composite composite;

	public CenteredImage(final Position2D position, final BufferedImage image) {
		this.position = position;
		this.image = image;
		composite = null;
	}

	public CenteredImage(final Position2D position, final BufferedImage image, final float alpha) {
		this.position = position;
		this.image = image;
		composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
	}

	public CenteredImage(final Position2D position, final BufferedImage image, final Composite composite) {
		this.position = position;
		this.image = image;
		this.composite = composite;
	}

	@Override
	public void draw(final Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		final int x = position.x - image.getWidth() / 2;
		final int y = position.y - image.getHeight() / 2;

		if (composite != null) {
			final Composite previousComposite = g.getComposite();
			g.setComposite(composite);
			g.drawImage(image, x, y, null);
			g.setComposite(previousComposite);
		} else {
			g.drawImage(image, x, y, null);
		}
	}

}
