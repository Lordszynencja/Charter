package log.charter.gui.handlers;

import static java.lang.Math.abs;
import static log.charter.gui.ChartPanel.isInLanes;
import static log.charter.util.ScalingUtils.xToTime;

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

		if (data.draggedBeatId != null) {
			final int newPos = xToTime(data.mx, data.time);
			data.songChart.beatsMap.moveBeat(data.draggedBeatId, newPos);
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
