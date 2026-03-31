package log.charter.gui.lookAndFeel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTextFieldUI;

import log.charter.data.config.ChartPanelColors.ColorLabel;

public class CharterTextFieldUI extends BasicTextFieldUI {
	private static final String CHARTER_TEXT_FIELD_UI = "CharterTextFieldUI";

	public static class ColoredBorder extends EmptyBorder {
		private static final long serialVersionUID = -220246127806611325L;

		public final Color color;

		public ColoredBorder(final int top, final int left, final int bottom, final int right, final Color color) {
			super(top, left, bottom, right);
			this.color = color;
		}
	}

	public static Border defaultBorder(final Color color) {
		return new ColoredBorder(5, 2, 5, 2, color);
	}

	public static Border defaultBorder() {
		return defaultBorder(ColorLabel.BASE_BORDER.color());
	}

	private final JTextField textField;

	public static ComponentUI createUI(final JComponent c) {
		if (JTextField.class.isAssignableFrom(c.getClass())) {
			return new CharterTextFieldUI((JTextField) c);
		}

		throw new IllegalArgumentException("Can't install CharterTextFieldUI for " + c.getClass());
	}

	private CharterTextFieldUI(final JTextField textField) {
		this.textField = textField;

		if (textField.getClientProperty(CHARTER_TEXT_FIELD_UI) == null) {
			textField.setUI(this);
			textField.putClientProperty(CHARTER_TEXT_FIELD_UI, Boolean.TRUE);
		}
	}

	@Override
	public void installUI(final JComponent c) {
		super.installUI(c);
		final JTextField textField = (JTextField) c;
		textField.setOpaque(false);
		textField.setBorder(defaultBorder());

		textField.setForeground(ColorLabel.BASE_TEXT_INPUT.color());
		textField.setCaretColor(ColorLabel.BASE_TEXT_INPUT.color());
		textField.setBackground(ColorLabel.BASE_BG_INPUT.color());
	}

	private void paintBorder(final Graphics2D g) {
		final Border border = textField.getBorder();
		Color color = ColorLabel.BASE_BORDER.color();
		if (border instanceof ColoredBorder) {
			color = ((ColoredBorder) border).color;
		}

		g.setColor(color);
		g.drawRoundRect(0, 0, getComponent().getWidth() - 1, getComponent().getHeight() - 1, 6, 6);
	}

	private void paintFill(final Graphics2D g) {
		final RoundRectangle2D.Double roundedRectangle = new RoundRectangle2D.Double(1, 1,
				getComponent().getWidth() - 2, getComponent().getHeight() - 2, 5, 5);
		if (!getComponent().isEnabled()) {
			g.setColor(ColorLabel.BASE_BG_2.color());
		} else {
			g.setColor(textField.getBackground());
		}
		g.fill(roundedRectangle);
	}

	@Override
	protected void paintSafely(final Graphics g) {
		final Graphics2D g2d = (Graphics2D) g.create();
		setupGraphics(g2d, getComponent());

		paintBorder(g2d);
		paintFill(g2d);

		super.paintSafely(g);

		g2d.dispose();
	}

	private void setupGraphics(final Graphics2D g2d, final JComponent c) {
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	}

	public static void install() {
		UIManager.put("TextFieldUI", CharterTextFieldUI.class.getName());
	}
}
