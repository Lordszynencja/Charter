package log.charter.gui.handlers;

import static java.lang.Math.abs;
import static log.charter.util.ScalingUtils.xToTime;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.text.Highlighter.Highlight;

import log.charter.data.ChartData;
import log.charter.data.Zoom;
import log.charter.gui.ChartKeyboardHandler;
import log.charter.gui.SelectionManager;
import log.charter.gui.chartPanelDrawers.common.DrawerUtils;

public class ChartPanelMouseListener implements MouseListener, MouseMotionListener, MouseWheelListener {
	private AudioHandler audioHandler;
	private ChartData data;
	private ChartKeyboardHandler chartKeyboardHandler;
	private SelectionManager selectionManager;

	private boolean clickCancelsRelease = false;
	private boolean releaseCancelled = false;
	private int mouseX = -1;
	private int mouseY = -1;

	private Highlight mouseRightPressHighlight;
	private int mousePressX = -1;
	private int mousePressY = -1;

	public void init(final AudioHandler audioHandler, final ChartData data,
			final ChartKeyboardHandler chartKeyboardHandler, final SelectionManager selectionManager) {
		this.audioHandler = audioHandler;
		this.data = data;
		this.chartKeyboardHandler = chartKeyboardHandler;
		this.selectionManager = selectionManager;
	}

	/*
	 * invoked only when it wasn't dragged
	 */
	@Override
	public void mouseClicked(final MouseEvent e) {
		mouseMoved(e);

		if (releaseCancelled) {
			clickCancelsRelease = false;
			return;
		}

		if (e.getButton() == MouseEvent.BUTTON1) {
			selectionManager.click(e.getX(), e.getY(), chartKeyboardHandler.ctrl(), chartKeyboardHandler.shift());
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
		if (clickCancelsRelease) {
			releaseCancelled = true;
			return;
		} else {
			clickCancelsRelease = true;
			releaseCancelled = false;
		}

		mousePressX = e.getX();
		mousePressY = e.getY();

		if (e.getButton() == MouseEvent.BUTTON1) {// TODO
//			if (isInTempos(y)) {
//				final Object[] tempoData = data.s.tempoMap.findOrCreateClosestTempo(data.xToTime(x));
//				if (tempoData != null) {
//					data.startTempoDrag((Tempo) tempoData[0], (Tempo) tempoData[1], (Tempo) tempoData[2],
//							(boolean) tempoData[3]);
//				}
//			} else if (isInLanes(y)) {
//				data.mousePressX = data.mx;
//				data.mousePressY = data.my;
//			}
		} else if (e.getButton() == MouseEvent.BUTTON3) {
			mouseRightPressHighlight = null;
//			if (isInLanes(y)) {
//				data.startNoteAdding(x, y);
//			}
		}
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		mouseMoved(e);

		if (data.isEmpty) {
			return;
		}

		clickCancelsRelease = false;
		if (releaseCancelled) {
			return;
		}

		switch (e.getButton()) {// TODO
		case MouseEvent.BUTTON1:
//			if (data.draggedTempo != null) {
//				data.stopTempoDrag();
//				setChanged();
//			} else if (data.isNoteDrag) {
//				data.endNoteDrag();
//			} else if ((data.my > (ChartPanel.sectionNamesY - 5)) && (data.my < ChartPanel.spY)) {
//				editSection(data.mx);
//			} else if (ChartPanel.isInLanes(data.my)) {
//				selectNotes(data.mx);
//			}
			break;
		case MouseEvent.BUTTON3:
			if (data.isNoteAdd) {
				data.endNoteAdding();
				data.setChanged();
			}
			break;
		default:
			break;
		}

		cancelAllActions();
	}

	@Override
	public void mouseDragged(final MouseEvent e) {
		mouseMoved(e);

		if (data.draggedBeatId != null) {
			final int newPos = xToTime(data.mx, data.time);
			data.songChart.beatsMap.moveBeat(data.draggedBeatId, newPos);
		}

		if (DrawerUtils.isInLanes(data.my) && (abs(mouseX - data.mousePressX) > 20)) {
			data.isNoteDrag = true;
		}
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
		data.softClearWithoutDeselect();
		audioHandler.stopMusic();
	}

	public void softClearWithoutDeselect() {// TODO
//		draggedTempoPrev = null;
//		draggedTempo = null;
//		draggedTempoNext = null;
		mousePressX = -1;
		mousePressY = -1;
//		isNoteAdd = false;
//		isNoteDrag = false;
	}

	@Override
	public void mouseWheelMoved(final MouseWheelEvent e) {
		int changeValue = e.getWheelRotation();
		if (chartKeyboardHandler.shift()) {
			changeValue *= 8;
		}

		if (chartKeyboardHandler.ctrl()) {
			Zoom.addZoom(changeValue);
			return;
		}

		{// TODO
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
