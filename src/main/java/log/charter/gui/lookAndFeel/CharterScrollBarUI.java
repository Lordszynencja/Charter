package log.charter.gui.lookAndFeel;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicScrollBarUI;

import log.charter.data.config.ChartPanelColors.ColorLabel;

public class CharterScrollBarUI extends BasicScrollBarUI {
	public static ComponentUI createUI(final JComponent c) {
		return new CharterScrollBarUI();
	}

	@Override
	public void installUI(final JComponent c) {
		super.installUI(c);
		if (c instanceof JScrollPane) {
			final JScrollPane scrollPane = (JScrollPane) c;
			scrollPane.getVerticalScrollBar().setUI(this);
			scrollPane.getHorizontalScrollBar().setUI(this);

			scrollPane.setOpaque(false);
		}
	}

	@Override
	protected void paintThumb(final Graphics g, final JComponent c, final Rectangle thumbBounds) {
		final Graphics2D g2d = (Graphics2D) g.create();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(ColorLabel.BASE_HIGHLIGHT.color());
		g2d.fill(thumbBounds);
		g2d.dispose();
	}

	@Override
	protected void paintTrack(final Graphics g, final JComponent c, final Rectangle trackBounds) {
		final Graphics2D g2d = (Graphics2D) g.create();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g2d.setColor(ColorLabel.BASE_BG_1.color());
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

	public static void install() {
		UIManager.put("ScrollBarUI", CharterScrollBarUI.class.getName());
	}
}
