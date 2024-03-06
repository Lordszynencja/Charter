package log.charter.gui.lookAndFeel;

import log.charter.gui.ChartPanelColors;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class CharterScrollBarUI extends BasicScrollBarUI {

    private static Color trackColor;
    private static Color thumbColor;
    private static final int SCROLLBAR_WIDTH = 17;

    static {
        updateColors();
    }

    public static ComponentUI createUI(JComponent c) {
        return new CharterScrollBarUI();
    }

    public static void updateColors() {
        trackColor = ChartPanelColors.ColorLabel.BASE_BG_1.color();
        thumbColor = ChartPanelColors.ColorLabel.BASE_HIGHLIGHT.color();
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        if (c instanceof JScrollPane) {
            JScrollPane scrollPane = (JScrollPane) c;
            scrollPane.getVerticalScrollBar().setUI(this);
            scrollPane.getHorizontalScrollBar().setUI(this);

            scrollPane.setOpaque(false);
        }
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        super.paintThumb(g, c, thumbBounds);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(thumbColor);

        int thumbWidth = (scrollbar.getOrientation() == JScrollBar.VERTICAL) ? SCROLLBAR_WIDTH : thumbBounds.width;
        int thumbHeight = (scrollbar.getOrientation() == JScrollBar.VERTICAL) ? thumbBounds.height : SCROLLBAR_WIDTH;

        // thumb
        RoundRectangle2D thumbRect = new RoundRectangle2D.Double(thumbBounds.x, thumbBounds.y, thumbWidth, thumbHeight, 0, 0);
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

    public static void install() {
        UIManager.put("ScrollBarUI", CharterScrollBarUI.class.getName());
    }
}
