package log.charter.gui.lookAndFeel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.*;

import log.charter.data.config.Localization;
import log.charter.gui.ChartPanelColors.ColorLabel;

public class CharterRadioButton {

    public static class RadioIcon extends SimpleIcon {

        private final Color backgroundColor;
        private final Color disabledBackgroundColor;
        private final Color borderColor;
        private final Color selectColor;
        private final Color iconColor;

        public RadioIcon(final Color backgroundColor, final Color disabledBackgroundColor,
                         final Color borderColor, final Color selectColor, final Color iconColor) {
            super(14, 14);
            this.backgroundColor = getDefault(backgroundColor, CharterRadioButton.backgroundColor);
            this.disabledBackgroundColor = getDefault(disabledBackgroundColor, CharterRadioButton.disabledBackgroundColor);
            this.borderColor = getDefault(borderColor, CharterRadioButton.borderColor);
            this.selectColor = getDefault(selectColor, CharterRadioButton.selectColor);
            this.iconColor = iconColor;
        }

        public RadioIcon(final Color selectColor) {
            this(null, null, selectColor, selectColor, null);
        }


        private Color getDefault(Color inputColor, Color defaultColor) {
            return (inputColor != null) ? inputColor : defaultColor;
        }

        private Color getFillColor(final ButtonModel model) {
            if (!model.isEnabled()) {
                return disabledBackgroundColor;
            }

            return model.isSelected() ? selectColor : backgroundColor;
        }

        private Color getBorderColor(final ButtonModel model) {
            return model.isSelected() ? selectColor : borderColor;
        }

        @Override
        public void paintIcon(final Component c, final Graphics g, final int x, final int y) {
            final JRadioButton radioButton = (JRadioButton) c;
            final ButtonModel model = radioButton.getModel();

            final Color fillColor = getFillColor(model);
            final Color borderColor = getBorderColor(model);

            Graphics2D g2d = (Graphics2D) g;
            setupGraphics(g2d);

            // Radio button fill
            g2d.setColor(fillColor);
            g2d.fillOval(x + 1, y + 1, width - 2, height - 2);

            // Radio button border
            g2d.setColor(borderColor);
            g2d.drawOval(x, y, width - 1, height - 1);

            // Radio icon
            if (model.isSelected()) {
                g2d.setColor(iconColor);
                g2d.fillOval(x + 4, y + 4, 6, 6);
            }
        }

        private void setupGraphics(Graphics2D g2d) {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
    }

    public static Color backgroundColor = ColorLabel.BASE_BG_INPUT.color();
    public static Color disabledBackgroundColor = ColorLabel.BASE_BG_2.color();
    public static Color borderColor = ColorLabel.BASE_BORDER.color();
    public static Color selectColor = ColorLabel.BASE_HIGHLIGHT.color();
    public static Color iconColor  = ColorLabel.BASE_TEXT.color();

    static void install() {
        UIManager.put("RadioButton.icon", new RadioIcon(backgroundColor, disabledBackgroundColor, borderColor, selectColor, iconColor));
    }
}
