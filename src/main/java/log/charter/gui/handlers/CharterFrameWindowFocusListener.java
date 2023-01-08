package log.charter.gui.handlers;

import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import log.charter.gui.CharterFrame;

public class CharterFrameWindowFocusListener implements WindowFocusListener {
	private final CharterFrame frame;

	public CharterFrameWindowFocusListener(final CharterFrame frame) {
		this.frame = frame;
	}

	@Override
	public void windowGainedFocus(final WindowEvent e) {
	}

	@Override
	public void windowLostFocus(final WindowEvent e) {
		frame.cancelAllActions();
	}
}
