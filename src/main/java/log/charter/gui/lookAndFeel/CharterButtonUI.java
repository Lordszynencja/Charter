package log.charter.gui.lookAndFeel;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicButtonUI;

import log.charter.data.config.ChartPanelColors.ColorLabel;

public class CharterButtonUI extends BasicButtonUI {
	public static final CharterButtonUI buttonUI = new CharterButtonUI();

	public static ComponentUI createUI(final JComponent c) {
		return buttonUI;
	}

	@Override
	public void installUI(final JComponent c) {
		super.installUI(c);

		final AbstractButton button = (AbstractButton) c;
		button.setContentAreaFilled(false);
		button.setFocusPainted(false);
		button.setBorderPainted(false);
		button.setOpaque(false);
		button.setBackground(ColorLabel.BASE_BUTTON.color());
	}

	private Color getBackgroundColor(final Graphics2D g2d, final AbstractButton button) {
		if (!button.isEnabled()) {
			return ColorLabel.BASE_BG_2.color();
		}
		if (button.getModel().isPressed()) {
			return ColorLabel.BASE_HIGHLIGHT.color();
		}

		return button.getBackground();
	}

	@Override
	public void paint(final Graphics g, final JComponent c) {
		final AbstractButton button = (AbstractButton) c;
		final Graphics2D g2d = (Graphics2D) g.create();
		setupGraphics(g2d, c);

		// button fill
		final RoundRectangle2D.Double roundedRectangle = new RoundRectangle2D.Double(0, 0, c.getWidth() - 1,
				c.getHeight() - 1, 5, 5);
		g2d.setColor(getBackgroundColor(g2d, button));
		g2d.fill(roundedRectangle);

		// button icon
		if (button.getIcon() != null) {
			final Icon icon = button.getIcon();
			final int iconX = (c.getWidth() - icon.getIconWidth()) / 2;
			final int iconY = (c.getHeight() - icon.getIconHeight()) / 2;
			icon.paintIcon(c, g2d, iconX, iconY);
		}

		// button text
		if (button.getText() != null && !button.getText().isEmpty()) {
			if (button.isEnabled()) {
				g2d.setColor(button.getForeground());
			} else {
				g2d.setColor(button.getForeground().darker());
			}
			g2d.drawString(button.getText(),
					(int) (c.getWidth() - g2d.getFontMetrics().getStringBounds(button.getText(), g2d).getWidth()) / 2,
					(int) ((c.getHeight() - g2d.getFontMetrics().getAscent() - g2d.getFontMetrics().getDescent()) / 2)
							+ g2d.getFontMetrics().getAscent());
		}

		g2d.dispose();
	}

	private void setupGraphics(final Graphics2D g2d, final JComponent c) {
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// label font
		final Font originalFont = g2d.getFont();
		final Font plainFont = originalFont.deriveFont(Font.PLAIN);
		g2d.setFont(plainFont);
	}

	static void install() {
		UIManager.put("ButtonUI", CharterButtonUI.class.getName());
	}
}
