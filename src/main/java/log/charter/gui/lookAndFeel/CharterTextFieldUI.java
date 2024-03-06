package log.charter.gui.lookAndFeel;

import log.charter.gui.ChartPanelColors;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTextFieldUI;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class CharterTextFieldUI extends BasicTextFieldUI {

    private static final String CHARTER_TEXT_FIELD_UI = "CharterTextFieldUI";

    private static Color backgroundColor;
    private static Color disabledBackgroundColor;
    private static Color borderColor;
    private static Color textColor;

    static {
        updateColors();
    }

    public static ComponentUI createUI(JComponent c) {
        return new CharterTextFieldUI((JTextField) c);
    }

    public static void updateColors() {
        backgroundColor = ChartPanelColors.ColorLabel.BASE_BG_INPUT.color();
        disabledBackgroundColor = ChartPanelColors.ColorLabel.BASE_BG_2.color();
        borderColor = ChartPanelColors.ColorLabel.BASE_BORDER.color();
        textColor = ChartPanelColors.ColorLabel.BASE_TEXT_INPUT.color();
    }

    private CharterTextFieldUI(JTextField textField) {
        if (textField.getClientProperty(CHARTER_TEXT_FIELD_UI) == null) {
            textField.setUI(this);
            textField.putClientProperty(CHARTER_TEXT_FIELD_UI, Boolean.TRUE);
        }
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        JTextField textField = (JTextField) c;
        textField.setOpaque(false);
        textField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        textField.setForeground(textColor);
        textField.setCaretColor(textColor);
    }

    @Override
    protected void paintSafely(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        setupGraphics(g2d, getComponent());

        // Textfield border
        g2d.setColor(borderColor);
        g2d.drawRoundRect(0, 0, getComponent().getWidth() - 1, getComponent().getHeight() - 1, 6, 6);

        // Textfield fill
        RoundRectangle2D.Double roundedRectangle = new RoundRectangle2D.Double(1, 1, getComponent().getWidth() - 2, getComponent().getHeight() - 2, 5, 5);
        if (!getComponent().isEnabled()) {
            g2d.setColor(disabledBackgroundColor);
        } else {
            g2d.setColor(backgroundColor);
        }
        g2d.fill(roundedRectangle);

        super.paintSafely(g);

        g2d.dispose();
    }

    private void setupGraphics(Graphics2D g2d, JComponent c) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }
    public static void install() {
        UIManager.put("TextFieldUI", CharterTextFieldUI.class.getName());
    }
}
