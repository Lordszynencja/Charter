package log.charter.gui.chartPanelDrawers.drawableShapes;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import log.charter.util.MultiComposite;
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

	private boolean tryDrawingWithComposite(final Graphics2D g, final BufferedImage img, final Composite c, final int x,
			final int y) {
		final Composite previousComposite = g.getComposite();
		boolean success = true;
		try {
			g.setComposite(c);
			g.drawImage(img, x, y, null);
		} catch (final Exception | Error e) {
			success = false;
		}
		g.setComposite(previousComposite);

		return success;
	}

	private BufferedImage getImageWithModifiedColors(final MultiComposite c) {
		final int w = image.getWidth();
		final int h = image.getHeight();
		final BufferedImage colored = new BufferedImage(w, h, image.getType());
		final Graphics2D g = (Graphics2D) colored.getGraphics();

		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, c.colorMultiply() / 255f));
		g.drawImage(image, 0, 0, null);

		final int colorAdd = c.colorAdd();
		if (colorAdd > 0) {
			g.setColor(Color.WHITE);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, colorAdd / 255f));
			g.fillRect(0, 0, w, h);
		} else if (colorAdd < 0) {
			g.setColor(Color.BLACK);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, colorAdd / -255f));
			g.fillRect(0, 0, w, h);
		}

		return colored;
	}

	@Override
	public void draw(final Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		final int x = position.x - image.getWidth() / 2;
		final int y = position.y - image.getHeight() / 2;

		if (composite != null) {
			if (tryDrawingWithComposite(g, image, composite, x, y)) {
				return;
			}

			if (composite instanceof MultiComposite) {
				final MultiComposite mc = (MultiComposite) composite;
				final BufferedImage colored = getImageWithModifiedColors(mc);
				final Composite alphaOnlyComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
						mc.alpha() / 255f);
				if (tryDrawingWithComposite(g, colored, alphaOnlyComposite, x, y)) {
					return;
				}
			}
		}

		g.drawImage(image, x, y, null);
	}

}
