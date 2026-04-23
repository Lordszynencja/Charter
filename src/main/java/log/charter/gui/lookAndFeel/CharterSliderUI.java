package log.charter.gui.lookAndFeel;

import static java.lang.Math.min;
import static log.charter.data.config.GraphicalConfig.inputSize;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.getAsOdd;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.plaf.metal.MetalSliderUI;

import log.charter.data.config.ChartPanelColors.ColorLabel;

public class CharterSliderUI extends MetalSliderUI {
	@Override
	protected void calculateThumbSize() {
		final int size = min(inputSize / 2, min(slider.getHeight(), slider.getWidth()));
		thumbRect.setSize(size, size);
	}

	private void setupGraphics(final Graphics2D g2d) {
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	}

	@Override
	public void installUI(final JComponent c) {
		super.installUI(c);
		c.setForeground(ColorLabel.BASE_HIGHLIGHT.color());
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
		g2d.setColor(slider.getForeground());
		g2d.fillOval(thumbRect.x, thumbRect.y, thumbRect.width, thumbRect.height);
	}

	private int trackWidth() {
		return getAsOdd(min(inputSize / 10, min(slider.getHeight() / 3, slider.getWidth() / 3)));
	}

	private void paintHorizontal(final Graphics2D g2d) {
		final int trackWidth = trackWidth();
		final int y = (int) (trackRect.getCenterY() - trackWidth / 2);
		final int x0 = trackRect.x;
		final int x1 = thumbRect.x + thumbRect.width / 2;
		final int x2 = x0 + trackRect.width;

		// before thumb
		g2d.setColor(slider.getForeground());
		g2d.fillRect(x0, y, x1 - x0, trackWidth);

		// after thumb
		g2d.setColor(ColorLabel.BASE_BORDER.color());
		g2d.fillRect(x1, y, x2 - x1, trackWidth);
	}

	private void paintVertical(final Graphics2D g2d) {
		final int trackWidth = trackWidth();
		final int x = (int) (trackRect.getCenterY() - trackWidth / 2);
		final int y0 = trackRect.y;
		final int y1 = thumbRect.y + thumbRect.height / 2;
		final int y2 = y0 + trackRect.height;

		// before thumb
		g2d.setColor(slider.getForeground());
		g2d.fillRect(x, y0, trackWidth, y1 - y0);

		// after thumb
		g2d.setColor(ColorLabel.BASE_BORDER.color());
		g2d.fillRect(x, y1, trackWidth, y2 - y1);
	}

	@Override
	public void paintTrack(final Graphics g) {
		final Graphics2D g2d = (Graphics2D) g;
		setupGraphics(g2d);

		if (slider.getOrientation() == JSlider.HORIZONTAL) {
			paintHorizontal(g2d);
		} else {
			paintVertical(g2d);
		}
	}
}
