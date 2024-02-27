package log.charter.gui.lookAndFeel;

import log.charter.gui.ChartPanelColors;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTextFieldUI;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class CharterTextFieldUI extends BasicTextFieldUI {

    private static final String CHARTER_TEXT_FIELD_UI = "CharterTextFieldUI";

    private static final Color backgroundColor = ChartPanelColors.ColorLabel.BASE_BG_1.color();
    private static final Color borderColor = ChartPanelColors.ColorLabel.BASE_BORDER.color();

    public static ComponentUI createUI(JComponent c) {
        return new CharterTextFieldUI((JTextField) c);
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
    }

    @Override
    protected void paintSafely(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        setupGraphics(g2d, getComponent());

        // Textfield border
        g2d.setColor(borderColor);
        g2d.drawRoundRect(0, 0, getComponent().getWidth() - 1, getComponent().getHeight() - 1, 6, 6);

        // Textfield fill
        RoundRectangle2D.Double roundedRectangle = new RoundRectangle2D.Double(1, 1, getComponent().getWidth() - 3, getComponent().getHeight() - 3, 5, 5);
        g2d.setColor(backgroundColor);
        g2d.fill(roundedRectangle);

        // Textfield text
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
