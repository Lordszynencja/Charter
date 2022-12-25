package log.charter.gui.handlers;

import static java.lang.Math.abs;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import log.charter.data.ChartData;
import log.charter.data.config.Zoom;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.gui.handlers.MouseButtonPressReleaseHandler.MouseButtonPressReleaseData;

public class MouseHandler implements MouseListener, MouseMotionListener, MouseWheelListener {
	private ChartData data;
	private KeyboardHandler keyboardHandler;
	private ModeManager modeManager;
	private MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler;
	private SelectionManager selectionManager;

	private boolean pressCancelsRelease = false;
	private boolean releaseCancelled = false;
	private int mouseX = -1;
	private int mouseY = -1;

	public void init(final AudioHandler audioHandler, final ChartData data, final KeyboardHandler keyboardHandler,
			final ModeManager modeManager, final MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler,
			final SelectionManager selectionManager) {
		this.data = data;
		this.keyboardHandler = keyboardHandler;
		this.modeManager = modeManager;
		this.mouseButtonPressReleaseHandler = mouseButtonPressReleaseHandler;
		this.selectionManager = selectionManager;
	}

	/*
	 * invoked only when it wasn't dragged
	 */
	@Override
	public void mouseClicked(final MouseEvent e) {
		mouseMoved(e);

		if (releaseCancelled) {
			pressCancelsRelease = false;
			return;
		}

		if (e.getButton() == MouseEvent.BUTTON1) {
			selectionManager.click(e.getX(), e.getY(), keyboardHandler.ctrl(), keyboardHandler.shift());
		}
	}

	@Override
	public void mouseEntered(final MouseEvent e) {
		mouseMoved(e);
	}

	@Override
	public void mouseExited(final MouseEvent e) {
		mouseMoved(e);
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		mouseMoved(e);

		if (data.isEmpty) {
			return;
		}

		cancelAllActions();
		if (pressCancelsRelease) {
			releaseCancelled = true;
			return;
		} else {
			pressCancelsRelease = true;
			releaseCancelled = false;
		}

		mouseButtonPressReleaseHandler.press(e);
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		mouseMoved(e);

		if (data.isEmpty) {
			return;
		}

		pressCancelsRelease = false;
		if (releaseCancelled) {
			return;
		}

		final MouseButtonPressReleaseData clickData = mouseButtonPressReleaseHandler.release(e);
		if (clickData == null) {
			return;
		}

		switch (clickData.button) {// TODO
		case LEFT_BUTTON:
			if (abs(clickData.releasePosition.x - clickData.pressPosition.x) > 5) {
				// TODO drag tempos/notes/other things if it moved more than few pixels
				// TODO drag the selected notes
//			if (data.draggedTempo != null) {
//				data.stopTempoDrag();
//				setChanged();
//			} else if (data.isNoteDrag) {
//				data.endNoteDrag();
//			} else if ((data.my > (ChartPanel.sectionNamesY - 5)) && (data.my < ChartPanel.spY)) {
//				editSection(data.mx);
//			} else if (ChartPanel.isInLanes(data.my)) {
//			}
			}
			break;
		case RIGHT_BUTTON:
			modeManager.getHandler().rightClick(clickData);
			break;
		default:
			break;
		}

		cancelAllActions();
	}

	@Override
	public void mouseDragged(final MouseEvent e) {
		mouseMoved(e);

//		if (data.draggedBeatId != null) {
//			final int newPos = xToTime(data.mx, data.time);
//			data.songChart.beatsMap.moveBeat(data.draggedBeatId, newPos);
//		}
	}

	@Override
	public void mouseMoved(final MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
	}

	public int getMouseX() {
		return mouseX;
	}

	public int getMouseY() {
		return mouseY;
	}

	public void cancelAllActions() {
		mouseButtonPressReleaseHandler.clear();
	}

	@Override
	public void mouseWheelMoved(final MouseWheelEvent e) {
		int changeValue = e.getWheelRotation();
		if (keyboardHandler.shift()) {
			changeValue *= 8;
		}

		if (keyboardHandler.ctrl()) {
			Zoom.addZoom(changeValue);
			return;
		}

		{// TODO
//			if (handler.data.currentInstrument.type.isVocalsType()) {
//				handler.data.changeLyricLength(rot);
//			} else {
//				handler.data.changeNoteLength(rot);
//			}
//			data.setChanged();
		}
		e.consume();
	}
}
