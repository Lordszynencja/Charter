package log.charter.gui.chartPanelDrawers.instruments.guitar;

import static log.charter.util.CollectionUtils.filter;
import static log.charter.util.ScalingUtils.timeToX;
import static log.charter.util.ScalingUtils.xToTime;

import java.awt.Graphics2D;

import log.charter.data.types.PositionType;
import log.charter.gui.chartPanelDrawers.data.HighlightData;
import log.charter.gui.chartPanelDrawers.data.HighlightData.HighlightPosition;
import log.charter.gui.chartPanelDrawers.instruments.guitar.highway.HighwayDrawer;
import log.charter.song.ToneChange;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashSet2;

public class GuitarToneChangeDrawer {
	private static String findCurrentTone(final String baseTone, final ArrayList2<ToneChange> toneChanges,
			final int time) {
		final ArrayList2<ToneChange> tones = filter(ArrayList2::new, toneChanges, //
				eventPoint -> eventPoint.position() < time);

		return tones.isEmpty() ? baseTone : tones.getLast().toneName;
	}

	private static void drawCurrentTone(final Graphics2D g, final HighwayDrawer highwayDrawer, final String baseTone,
			final ArrayList2<ToneChange> toneChanges, final int time, final int nextEventPointX) {
		final String tone = findCurrentTone(baseTone, toneChanges, time);
		highwayDrawer.addCurrentTone(g, tone, nextEventPointX);
	}

	private static void drawCurrentTone(final Graphics2D g, final HighwayDrawer highwayDrawer, final String baseTone,
			final ArrayList2<ToneChange> toneChanges, final int time) {
		final String tone = findCurrentTone(baseTone, toneChanges, time);
		highwayDrawer.addCurrentTone(g, tone);
	}

	private static void drawHighlightedPositions(final HighwayDrawer highwayDrawer, final int time,
			final HighlightData highlightData) {
		if (highlightData.type != PositionType.TONE_CHANGE) {
			return;
		}

		for (final HighlightPosition highlightPosition : highlightData.highlightedNonIdPositions) {
			final int x = timeToX(highlightPosition.position, time);
			highwayDrawer.addToneChangeHighlight(x);
		}
	}

	public static void addToneChanges(final Graphics2D g, final int panelWidth, final HighwayDrawer highwayDrawer,
			final String baseTone, final ArrayList2<ToneChange> toneChanges, final int time,
			final HashSet2<Integer> selectedIds, final HighlightData highlightData) {
		final int highlightId = highlightData.getId(PositionType.TONE_CHANGE);
		final int leftScreenEdgeTime = xToTime(0, time);

		boolean currentToneDrawn = false;
		for (int i = 0; i < toneChanges.size(); i++) {
			final ToneChange toneChange = toneChanges.get(i);
			final int x = timeToX(toneChange.position(), time);
			if (x < 0) {
				continue;
			}
			if (x > panelWidth) {
				break;
			}

			final boolean selected = selectedIds.contains(i);
			final boolean highlighted = i == highlightId;
			highwayDrawer.addToneChange(toneChange, x, selected, highlighted);
			if (!currentToneDrawn) {
				drawCurrentTone(g, highwayDrawer, baseTone, toneChanges, leftScreenEdgeTime, x);
				currentToneDrawn = true;
			}
		}
		if (!currentToneDrawn) {
			drawCurrentTone(g, highwayDrawer, baseTone, toneChanges, leftScreenEdgeTime);
		}

		drawHighlightedPositions(highwayDrawer, time, highlightData);
	}

}
