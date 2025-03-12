package log.charter.gui.lookAndFeel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JCheckBox;
import javax.swing.UIManager;

import log.charter.data.config.ChartPanelColors.ColorLabel;

public class CharterCheckBox {
	public static class CheckBoxIcon extends SimpleIcon {
		private final Color backgroundColor;
		private final Color disabledBackgroundColor;
		private final Color borderColor;
		private final Color selectColor;
		private final Color iconColor;

		public CheckBoxIcon(final Color backgroundColor, final Color disabledBackgroundColor, final Color borderColor,
				final Color selectColor, final Color iconColor) {
			super(14, 14);
			this.backgroundColor = backgroundColor;
			this.disabledBackgroundColor = disabledBackgroundColor;
			this.borderColor = borderColor;
			this.selectColor = selectColor;
			this.iconColor = iconColor;
		}

		public CheckBoxIcon(final Color selectColor) {
			this(null, null, selectColor, selectColor, null);
		}

		public CheckBoxIcon() {
			this(null, null, null, null, null);
		}

		private Color getColor(final Color inputColor, final ColorLabel colorLabel) {
			return (inputColor != null) ? inputColor : colorLabel.color();
		}

		private void paintFill(final JCheckBox checkBox, final Graphics2D g, final int x, final int y) {
			if (!checkBox.isEnabled()) {
				g.setColor(getColor(disabledBackgroundColor, ColorLabel.BASE_BG_2));
			} else if (checkBox.isSelected()) {
				g.setColor(getColor(selectColor, ColorLabel.BASE_HIGHLIGHT));
			} else {
				g.setColor(getColor(backgroundColor, ColorLabel.BASE_BG_INPUT));
			}
			g.fill(new RoundRectangle2D.Double(x + 1, y + 1, width - 2, height - 2, 0, 0));
		}

		private void paintBorder(final JCheckBox checkBox, final Graphics2D g, final int x, final int y) {
			if (checkBox.isSelected()) {
				g.setColor(getColor(selectColor, ColorLabel.BASE_HIGHLIGHT));
			} else {
				g.setColor(getColor(borderColor, ColorLabel.BASE_BORDER));
			}

			final RoundRectangle2D.Double roundedRectangle = new RoundRectangle2D.Double(x, y, width - 1, height - 1, 5,
					5);
			g.draw(roundedRectangle);
		}

		private void drawCheckMark(final JCheckBox checkBox, final Graphics2D g, final int x, final int y) {
			if (!checkBox.isSelected()) {
				return;
			}

			g.setColor(getColor(iconColor, ColorLabel.BASE_TEXT_INPUT));
			g.setStroke(new BasicStroke(1.5f));

			final Path2D.Double checkmark = createCheckmark(x, y);
			g.draw(checkmark);

		}

		@Override
		public void paintIcon(final Component c, final Graphics g, final int x, final int y) {
			final JCheckBox checkBox = (JCheckBox) c;
			final Graphics2D g2d = (Graphics2D) g;
			setupGraphics(g2d);

			paintFill(checkBox, g2d, x, y);
			paintBorder(checkBox, g2d, x, y);
			drawCheckMark(checkBox, g2d, x, y);
		}

		private void setupGraphics(final Graphics2D g2d) {
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}

		private Path2D.Double createCheckmark(final int x, final int y) {
			final Path2D.Double checkmark = new Path2D.Double();
			checkmark.moveTo(x + 3, y + 7);
			checkmark.lineTo(x + 6, y + 10);
			checkmark.lineTo(x + 11, y + 5);

			return checkmark;
		}
	}

	static void install() {
		UIManager.put("CheckBox.icon", new CheckBoxIcon());
	}
}
