package log.charter.gui.chartPanelDrawers.instruments.guitar;

import static log.charter.util.CollectionUtils.filter;
import static log.charter.util.ScalingUtils.timeToX;
import static log.charter.util.ScalingUtils.xToTime;

import java.awt.Graphics2D;
import java.util.List;
import java.util.Set;

import log.charter.data.song.ToneChange;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.types.PositionType;
import log.charter.gui.chartPanelDrawers.data.FrameData;
import log.charter.gui.chartPanelDrawers.data.HighlightData;
import log.charter.gui.chartPanelDrawers.data.HighlightData.HighlightPosition;
import log.charter.gui.chartPanelDrawers.instruments.guitar.highway.HighwayDrawer;

public class GuitarToneChangeDrawer {
	private static String findCurrentTone(final String baseTone, final List<ToneChange> toneChanges,
			final FractionalPosition time) {
		final List<ToneChange> tones = filter(toneChanges, //
				eventPoint -> eventPoint.compareTo(time) < 0);

		return tones.isEmpty() ? baseTone : tones.get(tones.size() - 1).toneName;
	}

	private static void drawCurrentTone(final Graphics2D g, final HighwayDrawer highwayDrawer, final String baseTone,
			final List<ToneChange> toneChanges, final FractionalPosition time, final int nextEventPointX) {
		final String tone = findCurrentTone(baseTone, toneChanges, time);
		highwayDrawer.addCurrentTone(g, tone, nextEventPointX);
	}

	private static void drawCurrentTone(final Graphics2D g, final HighwayDrawer highwayDrawer, final String baseTone,
			final List<ToneChange> toneChanges, final FractionalPosition time) {
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

	public static void addToneChanges(final FrameData frameData, final int panelWidth,
			final HighwayDrawer highwayDrawer) {
		final Set<Integer> selectedIds = frameData.selection.getSelectedIdsSet(PositionType.TONE_CHANGE);
		final int highlightId = frameData.highlightData.getId(PositionType.TONE_CHANGE);
		final FractionalPosition leftScreenEdgeTime = FractionalPosition.fromTime(frameData.beats,
				xToTime(0, frameData.time));
		final String baseTone = frameData.arrangement.baseTone;
		final List<ToneChange> toneChanges = frameData.arrangement.toneChanges;

		boolean currentToneDrawn = false;
		for (int i = 0; i < toneChanges.size(); i++) {
			final ToneChange toneChange = toneChanges.get(i);
			final int x = timeToX(toneChange.position(frameData.beats), frameData.time);
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
				drawCurrentTone(frameData.g, highwayDrawer, baseTone, toneChanges, leftScreenEdgeTime, x);
				currentToneDrawn = true;
			}
		}
		if (!currentToneDrawn) {
			drawCurrentTone(frameData.g, highwayDrawer, baseTone, toneChanges, leftScreenEdgeTime);
		}

		drawHighlightedPositions(highwayDrawer, frameData.time, frameData.highlightData);
	}

}
