package log.charter.gui.components;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.JMenuItem;

import log.charter.data.config.Localization.Label;
import log.charter.gui.ChartPanelColors.ColorLabel;

public class SpecialMenuItem extends JMenuItem {
	private static final long serialVersionUID = 1L;

	private final String shortcut;

	public SpecialMenuItem(final Label label, final String shortcut, final Runnable onClick) {
		super(label.label());
		this.shortcut = shortcut;
		addActionListener(e -> onClick.run());

		if (shortcut != null) {
			final Dimension preferredSize = getPreferredSize();
			preferredSize.width += 60;
			setPreferredSize(preferredSize);
		}
	}

	public SpecialMenuItem(final Label label, final Runnable onClick) {
		this(label, null, onClick);
	}

	private void paintShortcut(final Graphics g) {
		if (shortcut == null) {
			return;
		}

		final Font shortcutFont = new Font(Font.DIALOG, Font.PLAIN, 10);
		final FontMetrics fontMetrics = getFontMetrics(shortcutFont);
		g.setFont(shortcutFont);
		g.setColor(ColorLabel.BASE_DARK_TEXT.color());
		g.drawString(shortcut, getWidth() - 60, 2 + fontMetrics.getAscent());
	}

	@Override
	public void paint(final Graphics g) {
		super.paint(g);

		paintShortcut(g);
	}
}
