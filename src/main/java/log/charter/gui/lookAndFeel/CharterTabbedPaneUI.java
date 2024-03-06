package log.charter.gui.lookAndFeel;

import log.charter.gui.ChartPanelColors;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalTabbedPaneUI;
import java.awt.*;

public class CharterTabbedPaneUI extends MetalTabbedPaneUI {

    private static Color backgroundColor;
    private static Color highlightColor;
    private static Color selectColor;

    static {
        updateColors();
    }

    private static final CharterTabbedPaneUI tabbedPaneUI = new CharterTabbedPaneUI();

    public static ComponentUI createUI(JComponent c) {
        return tabbedPaneUI;
    }

    public static void updateColors() {
        backgroundColor = ChartPanelColors.ColorLabel.BASE_BG_2.color();
        highlightColor = ChartPanelColors.ColorLabel.BASE_HIGHLIGHT.color();
        selectColor = ChartPanelColors.ColorLabel.BASE_BG_3.color();
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        JTabbedPane tabbedPane = (JTabbedPane) c;
        tabbedPane.setOpaque(false);
    }

    @Override
    protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
        g.setColor(isSelected ? selectColor : backgroundColor);
        g.fillRect(x, y, w, h);
    }

    @Override
    protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect) {
        super.paintTab(g, tabPlacement, rects, tabIndex, iconRect, textRect);

        if (tabIndex == tabPane.getSelectedIndex()) {
            int bottomY = rects[tabIndex].y + rects[tabIndex].height - 1;
            g.setColor(highlightColor);
            g.fillRect(rects[tabIndex].x, bottomY - 3, rects[tabIndex].width, 3);
        }
    }
    @Override
    protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {
        Insets insets = tabPane.getInsets();
        int x = insets.left;
        int y = insets.top;
        int w = tabPane.getWidth() - insets.right - insets.left;
        int h = calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);

        g.setColor(backgroundColor);
        g.fillRect(x, y, w, h);
        super.paintTabArea(g, tabPlacement, selectedIndex);
    }
    @Override
    protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title, Rectangle textRect, boolean isSelected) {
        if (isSelected) {
            g.setColor(ChartPanelColors.ColorLabel.BASE_TEXT.color());
        } else {
            g.setColor(ChartPanelColors.ColorLabel.BASE_DARK_TEXT.color());
        }

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // label font
        Font plainFont = new Font(font.getName(), Font.PLAIN, font.getSize());
        g2d.setFont(plainFont);

        g.drawString(title, textRect.x, textRect.y + metrics.getAscent());
    }
    @Override
    protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
    }

    @Override
    protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
        int tw = tabPane.getBounds().width;
        int th = tabPane.getBounds().height;

        Insets insets = tabPane.getInsets();

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
            Rectangle selectedRect = getTabBounds(selectedIndex, calcRect);
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

        g.setColor(backgroundColor);
        g.fillRect(x, y, w, h);
    }

    @Override
    protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
    }

    @Override
    protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
        return super.calculateTabWidth(tabPlacement, tabIndex, metrics) + 20;
    }

    @Override
    protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
        return super.calculateTabHeight(tabPlacement, tabIndex, fontHeight) + 10;
    }

    static void install() {
        UIManager.put("TabbedPaneUI", CharterTabbedPaneUI.class.getName());
    }
}
