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
			super();
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

		private void setupGraphics(final Graphics2D g2d) {
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}

		private Color getColor(final Color inputColor, final ColorLabel colorLabel) {
			return (inputColor != null) ? inputColor : colorLabel.color();
		}

		private void paintBorder(final JCheckBox checkBox, final Graphics2D g, final int x, final int y,
				final int size) {
			if (checkBox.isSelected()) {
				g.setColor(getColor(selectColor, ColorLabel.BASE_HIGHLIGHT));
			} else {
				g.setColor(getColor(borderColor, ColorLabel.BASE_BORDER));
			}

			final RoundRectangle2D.Double roundedRectangle;
			if (checkBox.hasFocus()) {
				roundedRectangle = new RoundRectangle2D.Double(x - 1, y - 1, size + 2, size + 2, 5, 5);
			} else {
				roundedRectangle = new RoundRectangle2D.Double(x, y, size, size, 5, 5);
			}

			g.fill(roundedRectangle);
		}

		private void paintFill(final JCheckBox checkBox, final Graphics2D g, final int x, final int y, final int size) {
			if (!checkBox.isEnabled()) {
				g.setColor(getColor(disabledBackgroundColor, ColorLabel.BASE_BG_2));
			} else if (checkBox.isSelected()) {
				g.setColor(getColor(selectColor, ColorLabel.BASE_HIGHLIGHT));
			} else {
				g.setColor(getColor(backgroundColor, ColorLabel.BASE_BG_INPUT));
			}

			final int borderThickness = size / 10;
			g.fill(new RoundRectangle2D.Double(x + borderThickness, y + borderThickness, size - 2 * borderThickness,
					size - 2 * borderThickness, 0, 0));
		}

		private Path2D.Double createCheckmark(final int x, final int y, final int size) {
			final Path2D.Double checkmark = new Path2D.Double();
			final int x0 = x + size / 10;
			final int x1 = x + size * 2 / 5;
			final int x2 = x + size * 9 / 10;
			final int y0 = y + size * 4 / 5;
			final int y1 = y + size / 2;
			final int y2 = y + size / 5;

			checkmark.moveTo(x0, y1);
			checkmark.lineTo(x1, y0);
			checkmark.lineTo(x2, y2);

			return checkmark;
		}

		private void drawCheckMark(final JCheckBox checkBox, final Graphics2D g, final int x, final int y,
				final int size) {
			if (!checkBox.isSelected()) {
				return;
			}

			g.setColor(getColor(iconColor, ColorLabel.BASE_TEXT_INPUT));
			g.setStroke(new BasicStroke(checkBox.getWidth() / 10f));

			final Path2D.Double checkmark = createCheckmark(x, y, size);
			g.draw(checkmark);

		}

		@Override
		public void paintIcon(final Component c, final Graphics g, int x, int y) {
			final JCheckBox checkBox = (JCheckBox) c;
			final Graphics2D g2d = (Graphics2D) g;
			setupGraphics(g2d);

			final int w = c.getWidth() - x * 2;
			final int h = c.getHeight() - y * 2;
			final int size = Math.min(w, h);
			x = (c.getWidth() - size) / 2;
			y = (c.getHeight() - size) / 2;

			paintBorder(checkBox, g2d, x, y, size);
			paintFill(checkBox, g2d, x, y, size);
			drawCheckMark(checkBox, g2d, x, y, size);
		}
	}

	static void install() {
		UIManager.put("CheckBox.icon", new CheckBoxIcon());
	}
}
