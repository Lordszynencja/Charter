package log.charter.gui.handlers;

import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import log.charter.gui.ChartEventsHandler;

public class CharterFrameWindowFocusListener implements WindowFocusListener {

	private final ChartEventsHandler handler;

	public CharterFrameWindowFocusListener(final ChartEventsHandler handler) {
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
