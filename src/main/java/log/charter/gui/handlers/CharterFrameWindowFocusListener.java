package log.charter.gui.handlers;

import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import log.charter.gui.handlers.mouseAndKeyboard.KeyboardHandler;

public class CharterFrameWindowFocusListener implements WindowFocusListener {
	private final KeyboardHandler keyboardHandler;

	public CharterFrameWindowFocusListener(final KeyboardHandler keyboardHandler) {
		this.keyboardHandler = keyboardHandler;
	}

	@Override
	public void windowGainedFocus(final WindowEvent e) {
	}

	@Override
	public void windowLostFocus(final WindowEvent e) {
		keyboardHandler.clearKeys();
	}
}
