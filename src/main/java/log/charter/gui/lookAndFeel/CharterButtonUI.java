package log.charter.gui.lookAndFeel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicButtonUI;

import log.charter.gui.ChartPanelColors.ColorLabel;

public class CharterButtonUI extends BasicButtonUI {
    private static Color buttonFillColor;
    private static Color buttonHighlightColor;

    static {
        buttonFillColor = ColorLabel.BASE_BUTTON.color();
        buttonHighlightColor = ColorLabel.BASE_HIGHLIGHT.color();
    }
    @Override
    public void paint(Graphics g, JComponent c) {
        AbstractButton b = (AbstractButton) c;
        paintBackground(g, b, b.getModel().isPressed() ? 0 : 0);
        super.paint(g, c);
    }

    private void paintBackground(Graphics g, AbstractButton b, int yOffset) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = b.getWidth();
        int height = b.getHeight() - yOffset;

        // Draw gray background
        if (b.getModel().isPressed()) {
            // White on press
            g2d.setColor(buttonHighlightColor);
        } else {
            g2d.setColor(buttonFillColor);
        }
        g2d.fillRoundRect(0, yOffset, width, height, 5, 5);

        g2d.dispose();
    }
    
    @Override
    protected void paintText(Graphics g, AbstractButton b, java.awt.Rectangle textRect, String text) {
        if (b.getModel().isPressed()) {
            b.setForeground(Color.WHITE);
        } else {
            b.setForeground(Color.WHITE);
        }
        super.paintText(g, b, textRect, text);
    }
    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        AbstractButton button = (AbstractButton) c;
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setBorderPainted(false);
    }
}
