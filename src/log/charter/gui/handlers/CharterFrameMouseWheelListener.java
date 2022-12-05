package log.charter.gui.handlers;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import log.charter.gui.ChartEventsHandler;

public class CharterFrameMouseWheelListener implements MouseWheelListener {

	private final ChartEventsHandler handler;

	public CharterFrameMouseWheelListener(final ChartEventsHandler handler) {
		this.handler = handler;
	}

	@Override
	public void mouseWheelMoved(final MouseWheelEvent e) {
		final int rot = e.getWheelRotation();
		if (handler.isCtrl()) {
			handler.data.addZoom(rot * (handler.isShift() ? 10 : 1));
		} else {
			if (handler.data.currentInstrument.type.isVocalsType()) {
				handler.data.changeLyricLength(rot);
			} else {
				handler.data.changeNoteLength(rot);
			}
			handler.setChanged();
		}
		e.consume();
	}

}
