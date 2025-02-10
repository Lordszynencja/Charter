package log.charter.gui.components.simple;

import java.awt.Graphics;
import java.awt.Shape;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.text.Document;

import log.charter.util.ColorUtils;

public class CharterTextField extends JTextField implements FocusListener {
	private static final long serialVersionUID = -5318311977371803230L;

	private JLabel test;
	private String hint;

	public CharterTextField() {
		super();

		test = new JLabel("aaaa");
		this.add(test);

		addFocusListener(this);
	}

	public CharterTextField(final Document doc, final String text, final int columns) {
		super(doc, text, columns);
	}

	public CharterTextField(final int columns) {
		super(columns);
	}

	public CharterTextField(final String text, final int columns) {
		super(text, columns);
	}

	public CharterTextField(final String text) {
		super(text);
	}

	public void setHint(final String hint) {
		this.hint = hint;
	}

	@Override
	public void paintAll(final Graphics g) {
		super.paintAll(g);
	}

	@Override
	public void paint(final Graphics g) {
		super.paint(g);

		if (shouldDrawHintOverText() && !isFocusOwner()) {
			final Shape previousClip = g.getClip();
			g.setClip(1, 1, getWidth() - 2, getHeight() - 2);
			g.setFont(getFont());
			g.setColor(ColorUtils.mix(getForeground(), getBackground(), 0.5));

			g.drawString(hint, 5, getHeight() - 5);
			g.setClip(previousClip);
		}
	}

	@Override
	protected void paintComponent(final Graphics g) {
		super.paintComponent(g);

		if (shouldDrawHintOverText() && !isFocusOwner()) {
			final Shape previousClip = g.getClip();
			g.setClip(1, 1, getWidth() - 2, getHeight() - 2);
			g.setFont(getFont());
			g.setColor(ColorUtils.mix(getForeground(), getBackground(), 0.5));

			g.drawString(hint, 5, getHeight() - 5);
			g.setClip(previousClip);
		}
	}

	private boolean shouldDrawHintOverText() {
		return hint != null && (getText() == null || getText().isEmpty());
	}

	@Override
	public void focusGained(final FocusEvent e) {
		repaint();
	}

	@Override
	public void focusLost(final FocusEvent e) {
		repaint();
	}
}
