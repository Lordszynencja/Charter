package log.charter.gui.handlers;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import log.charter.data.config.Config;
import log.charter.gui.CharterFrame;

public class CharterFrameComponentListener implements ComponentListener {

	private final CharterFrame frame;

	public CharterFrameComponentListener(final CharterFrame frame) {
		this.frame = frame;
	}

	@Override
	public void componentHidden(final ComponentEvent e) {
	}

	@Override
	public void componentMoved(final ComponentEvent e) {
		Config.windowPosX = e.getComponent().getX();
		Config.windowPosY = e.getComponent().getY();
		Config.markChanged();
	}

	@Override
	public void componentResized(final ComponentEvent e) {
		frame.resize();
	}

	@Override
	public void componentShown(final ComponentEvent e) {
	}

}
