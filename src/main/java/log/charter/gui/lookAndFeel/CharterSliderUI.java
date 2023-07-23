package log.charter.gui.lookAndFeel;

import java.awt.Graphics;

import javax.swing.JSlider;
import javax.swing.plaf.metal.MetalSliderUI;

import log.charter.gui.ChartPanelColors.ColorLabel;

public class CharterSliderUI extends MetalSliderUI {
	@Override
	protected void calculateThumbSize() {
		thumbRect.setSize(15, 18);
	}

	@Override
	public void paintThumb(final Graphics g) {
		if (slider.getOrientation() != JSlider.HORIZONTAL) {
			super.paintThumb(g);
			return;
		}

		final int x = thumbRect.x + thumbRect.width / 2;
		final int y0 = thumbRect.y + 1;
		final int y1 = thumbRect.y + thumbRect.height + 1;

		g.setColor(ColorLabel.BASE_BG_4.color());
		g.drawLine(x, y0, x, y1);

		g.setColor(ColorLabel.BASE_BG_1.color());
		g.drawLine(x - 1, y0 + 1, x - 1, y1 - 1);
		g.drawLine(x + 1, y0 + 1, x + 1, y1 - 1);
	}
}
