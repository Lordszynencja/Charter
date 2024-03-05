package log.charter.gui.lookAndFeel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalScrollBarUI;

import log.charter.gui.ChartPanelColors;

public class CharterScrollBarUI extends MetalScrollBarUI {
	private static final Color trackColor = ChartPanelColors.ColorLabel.BASE_BUTTON.color();
	private static final Color thumbColor = ChartPanelColors.ColorLabel.BASE_HIGHLIGHT.color();
	private static final int SCROLLBAR_WIDTH = 12;

	@Override
	protected void paintThumb(final Graphics g, final JComponent c, final Rectangle thumbBounds) {
		super.paintThumb(g, c, thumbBounds);
		final Graphics2D g2d = (Graphics2D) g.create();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g2d.setColor(thumbColor);

		final int thumbWidth = (scrollbar.getOrientation() == JScrollBar.VERTICAL) ? SCROLLBAR_WIDTH
				: thumbBounds.width;
		final int thumbHeight = (scrollbar.getOrientation() == JScrollBar.VERTICAL) ? thumbBounds.height
				: SCROLLBAR_WIDTH;

		// thumb
		final RoundRectangle2D thumbRect = new RoundRectangle2D.Double(thumbBounds.x, thumbBounds.y, thumbWidth,
				thumbHeight, 10, 10);
		g2d.fill(thumbRect);

		g2d.dispose();
	}

	@Override
	protected void paintTrack(final Graphics g, final JComponent c, final Rectangle trackBounds) {
		super.paintTrack(g, c, trackBounds);
		final Graphics2D g2d = (Graphics2D) g.create();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g2d.setColor(trackColor);

		g2d.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);

		g2d.dispose();
	}

	@Override
	protected JButton createDecreaseButton(final int orientation) {
		return createZeroButton();
	}

	@Override
	protected JButton createIncreaseButton(final int orientation) {
		return createZeroButton();
	}

	private JButton createZeroButton() {
		final JButton button = new JButton();
		button.setPreferredSize(new Dimension(0, 0));
		button.setMinimumSize(new Dimension(0, 0));
		button.setMaximumSize(new Dimension(0, 0));
		return button;
	}

	static void install() {
		UIManager.put("ScrollBarUI", CharterScrollBarUI.class.getName());
	}
}
