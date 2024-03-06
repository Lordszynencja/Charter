package log.charter.gui.lookAndFeel;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalTabbedPaneUI;

import log.charter.gui.ChartPanelColors;
import log.charter.gui.ChartPanelColors.ColorLabel;

public class CharterTabbedPaneUI extends MetalTabbedPaneUI {

	private static Color getBackgroundColor() {
		return ColorLabel.BASE_BG_2.color();
	};

	private static final CharterTabbedPaneUI tabbedPaneUI = new CharterTabbedPaneUI();

	public static ComponentUI createUI(final JComponent c) {
		return tabbedPaneUI;
	}

	@Override
	public void installUI(final JComponent c) {
		super.installUI(c);
		final JTabbedPane tabbedPane = (JTabbedPane) c;
		tabbedPane.setOpaque(false);
	}

	@Override
	protected void paintTabBackground(final Graphics g, final int tabPlacement, final int tabIndex, final int x,
			final int y, final int w, final int h, final boolean isSelected) {
		g.setColor(isSelected ? ColorLabel.BASE_BG_3.color() : getBackgroundColor());
		g.fillRect(x, y, w, h);
	}

	@Override
	protected void paintTab(final Graphics g, final int tabPlacement, final Rectangle[] rects, final int tabIndex,
			final Rectangle iconRect, final Rectangle textRect) {
		super.paintTab(g, tabPlacement, rects, tabIndex, iconRect, textRect);

		if (tabIndex == tabPane.getSelectedIndex()) {
			final int bottomY = rects[tabIndex].y + rects[tabIndex].height - 1;
			g.setColor(ColorLabel.BASE_HIGHLIGHT.color());
			g.fillRect(rects[tabIndex].x, bottomY - 3, rects[tabIndex].width, 3);
		}
	}

	@Override
	protected void paintTabArea(final Graphics g, final int tabPlacement, final int selectedIndex) {
		final Insets insets = tabPane.getInsets();
		final int x = insets.left;
		final int y = insets.top;
		final int w = tabPane.getWidth() - insets.right - insets.left;
		final int h = calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);

		g.setColor(getBackgroundColor());
		g.fillRect(x, y, w, h);
		super.paintTabArea(g, tabPlacement, selectedIndex);
	}

	@Override
	protected void paintText(final Graphics g, final int tabPlacement, final Font font, final FontMetrics metrics,
			final int tabIndex, final String title, final Rectangle textRect, final boolean isSelected) {
		if (isSelected) {
			g.setColor(ChartPanelColors.ColorLabel.BASE_TEXT.color());
		} else {
			g.setColor(ChartPanelColors.ColorLabel.BASE_DARK_TEXT.color());
		}

		final Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		// label font
		final Font plainFont = new Font(font.getName(), Font.PLAIN, font.getSize());
		g2d.setFont(plainFont);

		g.drawString(title, textRect.x, textRect.y + metrics.getAscent());
	}

	@Override
	protected void paintTabBorder(final Graphics g, final int tabPlacement, final int tabIndex, final int x,
			final int y, final int w, final int h, final boolean isSelected) {
	}

	@Override
	protected void paintContentBorder(final Graphics g, final int tabPlacement, final int selectedIndex) {
		final int tw = tabPane.getBounds().width;
		final int th = tabPane.getBounds().height;

		final Insets insets = tabPane.getInsets();

		int x = insets.left;
		int y = insets.top;
		int w = tw - insets.right - insets.left;
		int h = th - insets.top - insets.bottom;

		switch (tabPlacement) {
			case LEFT:
				x += calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth);
				w -= (x - insets.left);
				break;
			case RIGHT:
				w -= calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth);
				break;
			case BOTTOM:
				h -= calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
				break;
			case TOP:
			default:
				y += calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
				h -= (y - insets.top);
		}

		if (tabPane.getTabCount() > 0 && selectedIndex >= 0) {
			final Rectangle selectedRect = getTabBounds(selectedIndex, calcRect);
			if (tabPlacement == TOP) {
				y = selectedRect.y + selectedRect.height;
			} else if (tabPlacement == BOTTOM) {
				h = selectedRect.y - y;
			} else if (tabPlacement == LEFT) {
				x = selectedRect.x + selectedRect.width;
			} else {
				w = selectedRect.x - x;
			}
		}

		g.setColor(getBackgroundColor());
		g.fillRect(x, y, w, h);
	}

	@Override
	protected void paintFocusIndicator(final Graphics g, final int tabPlacement, final Rectangle[] rects,
			final int tabIndex, final Rectangle iconRect, final Rectangle textRect, final boolean isSelected) {
	}

	@Override
	protected int calculateTabWidth(final int tabPlacement, final int tabIndex, final FontMetrics metrics) {
		return super.calculateTabWidth(tabPlacement, tabIndex, metrics) + 20;
	}

	@Override
	protected int calculateTabHeight(final int tabPlacement, final int tabIndex, final int fontHeight) {
		return super.calculateTabHeight(tabPlacement, tabIndex, fontHeight) + 10;
	}

	static void install() {
		UIManager.put("TabbedPaneUI", CharterTabbedPaneUI.class.getName());
	}
}
