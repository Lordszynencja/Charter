package log.charter.gui.handlers;

import static log.charter.gui.ChartPanel.isInLanes;
import static log.charter.gui.ChartPanel.isInTempos;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import log.charter.data.ChartData;

public class CharterFrameMouseListener implements MouseListener {
	private final AudioHandler audioHandler;
	private final ChartData data;

	private int mousePressX = -1;
	private int mousePressY = -1;

	private boolean clickCancelsRelease = false;
	private boolean releaseCancelled = false;

	public CharterFrameMouseListener(final AudioHandler audioHandler, final ChartData data) {
		this.audioHandler = audioHandler;
		this.data = data;
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
		if (releaseCancelled) {
			clickCancelsRelease = false;
			return;
		}

		if (e.getButton() == MouseEvent.BUTTON1) {// TODO

			final int y = e.getY();
			if (isInTempos(y)) {// TODO select tempo
//				int newTempoMeasures = -1;
//				final Object[] tempoData = data.s.tempoMap.findOrCreateClosestTempo(data.xToTime(data.mx));
//				while ((newTempoMeasures < 0) || (newTempoMeasures > 1000)) {
//					try {
//						final String value = JOptionPane.showInputDialog("Measures in beat",
//								"" + ((Tempo) tempoData[0]).beats);
//						if (value == null) {
//							return;
//						}
//						newTempoMeasures = Integer.valueOf(value);
//					} catch (final Exception exception) {
//					}
//				}
//
//				if (tempoData != null) {
//					data.changeTempoBeatsInMeasure((Tempo) tempoData[1], (boolean) tempoData[3], newTempoMeasures);
//					setChanged();
//				}
			}
		}
	}

	@Override
	public void mouseEntered(final MouseEvent e) {
	}

	@Override
	public void mouseExited(final MouseEvent e) {
	}

	@Override
	public void mousePressed(final MouseEvent e) {
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

		data.mx = e.getX();
		data.my = e.getY();

		final int x = e.getX();
		final int y = e.getY();
		if (e.getButton() == MouseEvent.BUTTON1) {// TODO
			if (isInTempos(y)) {
//				final Object[] tempoData = data.s.tempoMap.findOrCreateClosestTempo(data.xToTime(x));
//				if (tempoData != null) {
//					data.startTempoDrag((Tempo) tempoData[0], (Tempo) tempoData[1], (Tempo) tempoData[2],
//							(boolean) tempoData[3]);
//				}
			} else if (isInLanes(y)) {
				data.mousePressX = data.mx;
				data.mousePressY = data.my;
			}
		} else if (e.getButton() == MouseEvent.BUTTON3) {
			if (isInLanes(y)) {
				data.startNoteAdding(x, y);
			}
		}
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		if (data.isEmpty) {
			return;
		}

		clickCancelsRelease = false;
		if (releaseCancelled) {
			return;
		}

		data.mx = e.getX();
		data.my = e.getY();

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
}
