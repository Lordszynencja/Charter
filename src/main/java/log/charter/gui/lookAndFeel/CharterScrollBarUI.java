package log.charter.gui.lookAndFeel;

import log.charter.gui.ChartPanelColors;

import javax.swing.*;
import javax.swing.plaf.metal.MetalScrollBarUI;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class CharterScrollBarUI extends MetalScrollBarUI {
    private static final Color trackColor = ChartPanelColors.ColorLabel.BASE_BUTTON.color();
    private static final Color thumbColor = ChartPanelColors.ColorLabel.BASE_HIGHLIGHT.color();
    private static final int SCROLLBAR_WIDTH = 12;

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        super.paintThumb(g, c, thumbBounds);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(thumbColor);

        int thumbWidth = (scrollbar.getOrientation() == JScrollBar.VERTICAL) ? SCROLLBAR_WIDTH : thumbBounds.width;
        int thumbHeight = (scrollbar.getOrientation() == JScrollBar.VERTICAL) ? thumbBounds.height : SCROLLBAR_WIDTH;

        // thumb
        RoundRectangle2D thumbRect = new RoundRectangle2D.Double(thumbBounds.x, thumbBounds.y, thumbWidth, thumbHeight, 10, 10);
        g2d.fill(thumbRect);

        g2d.dispose();
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        super.paintTrack(g, c, trackBounds);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(trackColor);

        g2d.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);

        g2d.dispose();
    }

    @Override
    protected JButton createDecreaseButton(int orientation) {
        return createZeroButton();
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return createZeroButton();
    }

    private JButton createZeroButton() {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(0, 0));
        button.setMinimumSize(new Dimension(0, 0));
        button.setMaximumSize(new Dimension(0, 0));
        return button;
    }
    static void install() {
        UIManager.put("ScrollBarUI", CharterScrollBarUI.class.getName());
    }
}
