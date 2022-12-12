package log.charter.gui.handlers;

import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import log.charter.gui.ChartKeyboardHandler;

public class CharterFrameWindowFocusListener implements WindowFocusListener {
	private final ChartKeyboardHandler chartKeyboardHandler;

	public CharterFrameWindowFocusListener(final ChartKeyboardHandler chartKeyboardHandler) {
		this.chartKeyboardHandler = chartKeyboardHandler;
	}

	@Override
	public void windowGainedFocus(final WindowEvent e) {
	}

	@Override
	public void windowLostFocus(final WindowEvent e) {
		chartKeyboardHandler.clearKeys();
		chartKeyboardHandler.cancelAllActions();
	}
}
