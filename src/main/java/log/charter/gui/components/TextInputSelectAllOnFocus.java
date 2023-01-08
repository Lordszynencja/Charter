package log.charter.gui.components;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;

public class TextInputSelectAllOnFocus implements FocusListener {
	public static void addSelectTextOnFocus(final JTextField input) {
		input.addFocusListener(new TextInputSelectAllOnFocus(input));
	}

	private final JTextField input;

	private TextInputSelectAllOnFocus(final JTextField input) {
		this.input = input;
	}

	@Override
	public void focusGained(final FocusEvent e) {
		input.selectAll();
	}

	@Override
	public void focusLost(final FocusEvent e) {
	}

}
