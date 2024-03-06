package log.charter.gui.lookAndFeel;

import log.charter.gui.ChartPanelColors;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class CharterButtonUI extends BasicButtonUI {
    private static Color backgroundColor;
    private static Color disabledBackgroundColor;
    private static Color selectColor;

    static {
        updateColors();
    }

    public static final CharterButtonUI buttonUI = new CharterButtonUI();

    public static ComponentUI createUI(JComponent c) {
        return buttonUI;
    }

    public static void updateColors() {
        backgroundColor = ChartPanelColors.ColorLabel.BASE_BUTTON.color();
        disabledBackgroundColor = ChartPanelColors.ColorLabel.BASE_BG_2.color();
        selectColor = ChartPanelColors.ColorLabel.BASE_HIGHLIGHT.color();
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        AbstractButton button = (AbstractButton) c;
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(false);
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        AbstractButton button = (AbstractButton) c;
        Graphics2D g2d = (Graphics2D) g.create();
        setupGraphics(g2d, c);

        // button fill
        RoundRectangle2D.Double roundedRectangle = new RoundRectangle2D.Double(0, 0, c.getWidth() - 1, c.getHeight() - 1, 5, 5);
        if (!button.isEnabled()) {
            g2d.setColor(disabledBackgroundColor);
            g2d.fill(roundedRectangle);
        } else {
            g2d.setColor(button.getModel().isPressed() ? selectColor : backgroundColor);
            g2d.fill(roundedRectangle);
        }

        // button icon
        if (button.getIcon() != null) {
            Icon icon = button.getIcon();
            int iconX = (c.getWidth() - icon.getIconWidth()) / 2;
            int iconY = (c.getHeight() - icon.getIconHeight()) / 2;
            icon.paintIcon(c, g2d, iconX, iconY);
        }

        // button text
        if (button.getText() != null && !button.getText().isEmpty()) {
            if (button.isEnabled()) {
                g2d.setColor(button.getForeground());
            } else {
                g2d.setColor(button.getForeground().darker());
            }
            g2d.drawString(button.getText(), (int) (c.getWidth() - g2d.getFontMetrics().getStringBounds(button.getText(), g2d).getWidth()) / 2,
                    (int) ((c.getHeight() - g2d.getFontMetrics().getAscent() - g2d.getFontMetrics().getDescent()) / 2) + g2d.getFontMetrics().getAscent());
        }

        g2d.dispose();
    }

    private void setupGraphics(Graphics2D g2d, JComponent c) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // label font
        Font originalFont = g2d.getFont();
        Font plainFont = originalFont.deriveFont(Font.PLAIN);
        g2d.setFont(plainFont);
    }

    static void install() {
        UIManager.put("ButtonUI", CharterButtonUI.class.getName());
    }
}
