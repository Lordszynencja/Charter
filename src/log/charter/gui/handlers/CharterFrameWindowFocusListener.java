package log.charter.gui.handlers;

import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import log.charter.gui.ChartKeyboardHandler;

public class CharterFrameWindowFocusListener implements WindowFocusListener {

	private final ChartKeyboardHandler handler;

	public CharterFrameWindowFocusListener(final ChartKeyboardHandler handler) {
		this.handler = handler;
	}

	@Override
	public void windowGainedFocus(final WindowEvent e) {
	}

	@Override
	public void windowLostFocus(final WindowEvent e) {
		handler.clearKeys();
		handler.cancelAllActions();
	}

}
