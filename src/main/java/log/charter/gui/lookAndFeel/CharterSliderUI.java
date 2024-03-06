package log.charter.gui.lookAndFeel;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JSlider;
import javax.swing.plaf.metal.MetalSliderUI;

import log.charter.gui.ChartPanelColors.ColorLabel;

public class CharterSliderUI extends MetalSliderUI {
	@Override
	protected void calculateThumbSize() {
		thumbRect.setSize(11, 11);
	}

	private void setupGraphics(final Graphics2D g2d) {
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	}

	@Override
	public void paintThumb(final Graphics g) {
		if (slider.getOrientation() != JSlider.HORIZONTAL) {
			super.paintThumb(g);
			return;
		}

		final Graphics2D g2d = (Graphics2D) g;
		setupGraphics(g2d);

		// thumb color
		g2d.setColor(ColorLabel.BASE_HIGHLIGHT.color());
		g2d.fillOval(thumbRect.x, thumbRect.y, thumbRect.width, thumbRect.height);
	}

	@Override
	public void paintTrack(final Graphics g) {
		final Graphics2D g2d = (Graphics2D) g;
		setupGraphics(g2d);

		final int y = (int) (trackRect.getCenterY() - 1);
		final int x0 = trackRect.x;
		final int x1 = thumbRect.x;
		final int x2 = x0 + trackRect.width;

		// before thumb
		g2d.setColor(ColorLabel.BASE_HIGHLIGHT.color());
		g2d.fillRect(x0, y, x1 - x0, 3);

		// after thumb
		g2d.setColor(ColorLabel.BASE_BORDER.color());
		g2d.fillRect(x1, y, x2 - x1, 3);
	}
}
