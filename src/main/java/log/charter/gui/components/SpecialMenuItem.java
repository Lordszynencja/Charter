package log.charter.gui.components;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.JMenuItem;

import log.charter.gui.ChartPanelColors.ColorLabel;

public class SpecialMenuItem extends JMenuItem {
	private static final long serialVersionUID = 1L;

	private final String shortcut;

	public SpecialMenuItem(final String label, final String shortcut) {
		super(label);
		this.shortcut = shortcut;

		final Dimension preferredSize = getPreferredSize();
		preferredSize.width += 30;
	}

	@Override
	public void paint(final Graphics g) {
		super.paint(g);

		final Font acceleratorFont = new Font(Font.DIALOG, Font.PLAIN, 10);
		final FontMetrics fontMetrics = getFontMetrics(acceleratorFont);
		g.setFont(acceleratorFont);
		g.setColor(ColorLabel.BASE_DARK_TEXT.color());
		g.drawString(shortcut, getWidth() - 30, 2 + fontMetrics.getAscent());
	}
}
