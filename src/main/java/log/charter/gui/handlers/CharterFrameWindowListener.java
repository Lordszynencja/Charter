package log.charter.gui.handlers;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import log.charter.data.managers.CharterContext;

public class CharterFrameWindowListener implements WindowListener {
	private final CharterContext context;

	public CharterFrameWindowListener(final CharterContext context) {
		this.context = context;
	}

	@Override
	public void windowActivated(final WindowEvent e) {
	}

	@Override
	public void windowClosed(final WindowEvent e) {
	}

	@Override
	public void windowClosing(final WindowEvent e) {
		context.exit();
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
