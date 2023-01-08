package log.charter.gui.handlers;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import log.charter.gui.CharterFrame;

public class CharterFrameWindowListener implements WindowListener {
	private final CharterFrame frame;

	public CharterFrameWindowListener(final CharterFrame frame) {
		this.frame = frame;
	}

	@Override
	public void windowActivated(final WindowEvent e) {
	}

	@Override
	public void windowClosed(final WindowEvent e) {
	}

	@Override
	public void windowClosing(final WindowEvent e) {
		frame.exit();
	}

	@Override
	public void windowDeactivated(final WindowEvent e) {
	}

	@Override
	public void windowDeiconified(final WindowEvent e) {
	}

	@Override
	public void windowIconified(final WindowEvent e) {
	}

	@Override
	public void windowOpened(final WindowEvent e) {
	}
}
