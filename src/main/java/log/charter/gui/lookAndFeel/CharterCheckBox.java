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

public class CharterCheckBox {
    public static Color backgroundColor = ColorLabel.BASE_BG_INPUT.color();
    public static Color disabledBackgroundColor = ColorLabel.BASE_BG_2.color();
    public static Color borderColor = ColorLabel.BASE_BORDER.color();
    public static Color selectColor = ColorLabel.BASE_HIGHLIGHT.color();
    public static Color iconColor = ColorLabel.BASE_TEXT_INPUT.color();

    public static class CheckBoxIcon extends SimpleIcon {
        private final Color backgroundColor;
        private final Color disabledBackgroundColor;
        private final Color borderColor;
        private final Color selectColor;
        private final Color iconColor;

        public CheckBoxIcon(Color backgroundColor, Color disabledBackgroundColor, Color edgeColor, Color selectColor, Color iconColor) {
            super(14, 14);
            this.backgroundColor = getDefault(backgroundColor, CharterCheckBox.backgroundColor);
            this.disabledBackgroundColor = getDefault(disabledBackgroundColor, CharterCheckBox.disabledBackgroundColor);
            this.borderColor = getDefault(edgeColor, CharterCheckBox.borderColor);
            this.selectColor = getDefault(selectColor, CharterCheckBox.selectColor);
            this.iconColor = iconColor;
        }

        public CheckBoxIcon(Color selectColor) {
            this(null, null, selectColor, selectColor, null);
        }

        private Color getDefault(Color inputColor, Color defaultColor) {
            return (inputColor != null) ? inputColor : defaultColor;
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
                g2d.fill(new RoundRectangle2D.Double(x + 1, y + 1, width - 2, height - 2, 0, 0));
            } else {
                g2d.setColor(checkBox.isSelected() ? selectColor : backgroundColor);
                g2d.fill(new RoundRectangle2D.Double(x + 1, y + 1, width - 2, height - 2, 0, 0));
            }

            // check box border
            g2d.setColor(checkBox.isSelected() ? selectColor : borderColor);
            g2d.draw(roundedRectangle);

            // check box icon
            if (checkBox.isSelected()) {
                g2d.setColor(iconColor);
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
        UIManager.put("CheckBox.icon", new CheckBoxIcon(backgroundColor, disabledBackgroundColor, borderColor, selectColor, iconColor));
    }
}
