package log.charter.gui.handlers;

import static java.lang.Math.abs;
import static log.charter.gui.ChartPanel.isInLanes;
import static log.charter.song.TempoMap.calcBPM;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import log.charter.data.ChartData;

public class CharterFrameMouseMotionListener implements MouseMotionListener {
	private final ChartData data;

	public CharterFrameMouseMotionListener(final ChartData data) {
		this.data = data;
	}

	@Override
	public void mouseDragged(final MouseEvent e) {
		mouseMoved(e);

		if (data.draggedTempo != null) {
			data.draggedTempo.pos = data.xToTime(data.mx);
			if (data.draggedTempo.pos < (data.draggedTempoPrev.pos + 1)) {
				data.draggedTempo.pos = data.draggedTempoPrev.pos + 1;
			}
			calcBPM(data.draggedTempoPrev, data.draggedTempo);
			if (data.draggedTempoNext != null) {
				if (data.draggedTempo.pos > (data.draggedTempoNext.pos - 1)) {
					data.draggedTempo.pos = data.draggedTempoNext.pos - 1;
				}
				calcBPM(data.draggedTempo, data.draggedTempoNext);
			} else {
				data.draggedTempo.kbpm = data.draggedTempoPrev.kbpm;
			}
		}

		if (isInLanes(data.my) && (abs(data.mx - data.mousePressX) > 20)) {
			data.isNoteDrag = true;
		}
	}

	@Override
	public void mouseMoved(final MouseEvent e) {
		data.mx = e.getX();
		data.my = e.getY();
	}

}
