package log.charter.gui.lookAndFeel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.ButtonModel;
import javax.swing.JRadioButton;
import javax.swing.UIManager;

import log.charter.gui.ChartPanelColors.ColorLabel;

class CharterRadioButton {

    private static class RadioIcon extends SimpleIcon {

        private final Color backgroundColor;
        private final Color disabledBackgroundColor;
        private final Color borderColor;
        private final Color selectColor;

        public RadioIcon(final Color backgroundColor, final Color disabledBackgroundColor,
                final Color borderColor, final Color selectColor) {
            super(14, 14);
            this.backgroundColor = backgroundColor;
            this.disabledBackgroundColor = disabledBackgroundColor;
            this.borderColor = borderColor;
            this.selectColor = selectColor;
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
            g2d.fillOval(x, y, width - 1, height - 1);

            // Radio button border
            g2d.setColor(borderColor);
            g2d.drawOval(x, y, width - 1, height - 1);

            // Radio icon
            if (model.isSelected()) {
                g2d.setColor(Color.WHITE);
                g2d.fillOval(x + 4, y + 4, 6, 6); // Adjusted size
            }
        }

        private void setupGraphics(Graphics2D g2d) {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
    }

    static void install() {
        final Color backgroundColor = new Color(45, 45, 45);
        final Color disabledBackgroundColor = ColorLabel.BASE_BG_2.color();
        final Color borderColor = ColorLabel.BASE_BORDER.color();
        final Color selectColor = ColorLabel.BASE_HIGHLIGHT.color();

        UIManager.put("RadioButton.icon", new RadioIcon(backgroundColor, disabledBackgroundColor, borderColor, selectColor));
    }
}
