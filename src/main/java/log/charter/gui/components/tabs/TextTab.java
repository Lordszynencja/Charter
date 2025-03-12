package log.charter.gui.components.tabs;

import java.awt.Graphics;

import javax.swing.JTextArea;

import log.charter.data.config.ChartPanelColors.ColorLabel;
import log.charter.gui.components.containers.CharterScrollPane;

public class TextTab extends CharterScrollPane {
	private static final long serialVersionUID = 1L;

	private final JTextArea textArea;

	public TextTab() {
		this(new JTextArea(1000, 1000));
	}

	private TextTab(final JTextArea textArea) {
		super(textArea);

		this.textArea = textArea;

		setColors();
	}

	private void setColors() {
		textArea.setBackground(ColorLabel.BASE_BG_2.color());
		textArea.setForeground(ColorLabel.BASE_TEXT.color());
		textArea.setCaretColor(ColorLabel.BASE_TEXT.color());
	}

	@Override
	public void paint(final Graphics g) {
		setColors();
		super.paint(g);
	}

	public String getText() {
		return textArea.getText();
	}

	public void setText(final String text) {
		textArea.setText(text);
	}
}
