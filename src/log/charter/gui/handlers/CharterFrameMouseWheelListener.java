package log.charter.gui.handlers;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import log.charter.data.ChartData;
import log.charter.data.Zoom;
import log.charter.gui.ChartKeyboardHandler;

public class CharterFrameMouseWheelListener implements MouseWheelListener {

	private final ChartData data;
	private final ChartKeyboardHandler chartKeyboardHandler;

	public CharterFrameMouseWheelListener(final ChartData data, final ChartKeyboardHandler handler) {
		this.data = data;
		chartKeyboardHandler = handler;
	}

	@Override
	public void mouseWheelMoved(final MouseWheelEvent e) {
		final int rot = e.getWheelRotation();
		if (chartKeyboardHandler.ctrl()) {
			Zoom.addZoom(rot * (chartKeyboardHandler.isShift() ? 10 : 1));
		} else {// TODO
//			if (handler.data.currentInstrument.type.isVocalsType()) {
//				handler.data.changeLyricLength(rot);
//			} else {
//				handler.data.changeNoteLength(rot);
//			}
			data.setChanged();
		}
		e.consume();
	}

}
