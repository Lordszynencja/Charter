package log.charter.gui.lookAndFeel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JCheckBox;
import javax.swing.UIManager;

import log.charter.gui.ChartPanelColors.ColorLabel;

class CharterCheckBox {

    private static class CheckBoxIcon extends SimpleIcon {
        private final Color backgroundColor;
        private final Color disabledBackgroundColor;
        private final Color borderColor;
        private final Color selectColor;
        
        public CheckBoxIcon(Color backgroundColor, Color disabledBackgroundColor, Color edgeColor, Color selectColor) {
        	super(14, 14);
            this.backgroundColor = backgroundColor;
            this.disabledBackgroundColor = disabledBackgroundColor;
            this.borderColor = edgeColor;
            this.selectColor = selectColor;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            JCheckBox checkBox = (JCheckBox) c;
            Graphics2D g2d = (Graphics2D) g;
            setupGraphics(g2d);

            // check box fill
            RoundRectangle2D.Double roundedRectangle = new RoundRectangle2D.Double(x, y, width - 1, height - 1, 5, 5);
            if (!checkBox.isEnabled()) {
                g2d.setColor(disabledBackgroundColor);
                g2d.fill(new RoundRectangle2D.Double(x, y, width - 1, height - 1, 5, 5));
            } else {
                g2d.setColor(checkBox.isSelected() ? selectColor : backgroundColor);
                g2d.fill(new RoundRectangle2D.Double(x, y, width - 1, height - 1, 5, 5));
            }

            // check box border
            g2d.setColor(checkBox.isSelected() ? selectColor : borderColor);
            g2d.draw(roundedRectangle);

            // check box icon
            if (checkBox.isSelected()) {
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new java.awt.BasicStroke(1.5f));

                Path2D.Double checkmark = createCheckmark(x, y);
                g2d.draw(checkmark);
            }
        }

        private void setupGraphics(Graphics2D g2d) {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }

        private Path2D.Double createCheckmark(int x, int y) {
            Path2D.Double checkmark = new Path2D.Double();
            checkmark.moveTo(x + 3, y + 7);
            checkmark.lineTo(x + 6, y + 10);
            checkmark.lineTo(x + 11, y + 5);
            return checkmark;
        }
    }

    static void install() {
        final Color backgroundColor = ColorLabel.BASE_BG_1.color();
        final Color disabledBackgroundColor = ColorLabel.BASE_BG_2.color();
        final Color borderColor = ColorLabel.BASE_BORDER.color();
        final Color selectColor  = ColorLabel.BASE_HIGHLIGHT.color();

        UIManager.put("CheckBox.icon", new CheckBoxIcon(backgroundColor, disabledBackgroundColor, borderColor, selectColor));
    }
}
