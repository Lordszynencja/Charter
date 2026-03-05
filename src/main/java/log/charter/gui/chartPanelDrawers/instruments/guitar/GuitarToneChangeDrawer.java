package log.charter.gui.chartPanelDrawers.instruments.guitar;

import static log.charter.util.ScalingUtils.positionToX;

import java.util.List;
import java.util.Set;

import log.charter.data.song.ToneChange;
import log.charter.data.types.PositionType;
import log.charter.data.types.SpecialPositionType;
import log.charter.gui.chartPanelDrawers.data.FrameData;
import log.charter.gui.chartPanelDrawers.data.HighlightData;
import log.charter.gui.chartPanelDrawers.data.HighlightData.HighlightPosition;
import log.charter.gui.chartPanelDrawers.instruments.guitar.GuitarDrawerUtils.ItemWithDrawingPosition;
import log.charter.gui.chartPanelDrawers.instruments.guitar.highway.HighwayDrawer;

public class GuitarToneChangeDrawer {
	private static void drawPreviousTone(final FrameData frameData, final HighwayDrawer highwayDrawer,
			final GuitarDrawerUtils guitarDrawerUtils, final boolean highlight) {
		final ItemWithDrawingPosition<ToneChange> previousToneChange = guitarDrawerUtils
				.findPreviousToneChange(frameData.time, highwayDrawer);
		if (previousToneChange != null) {
			highwayDrawer.addTone(previousToneChange.item.toneName, previousToneChange.x0, highlight);
		}
	}

	private static void drawNextTone(final FrameData frameData, final HighwayDrawer highwayDrawer,
			final GuitarDrawerUtils guitarDrawerUtils, final boolean highlight) {
		final ItemWithDrawingPosition<ToneChange> nextToneChange = guitarDrawerUtils.findNextToneChange(frameData.time,
				highwayDrawer);
		if (nextToneChange != null) {
			highwayDrawer.addTone(nextToneChange.item.toneName, nextToneChange.x0, highlight);
		}
	}

	private static void drawHighlightedPositions(final HighwayDrawer highwayDrawer, final double time,
			final HighlightData highlightData) {
		if (highlightData.type != PositionType.TONE_CHANGE) {
			return;
		}

		for (final HighlightPosition highlightPosition : highlightData.highlightedNonIdPositions) {
			final int x = positionToX(highlightPosition.position, time);
			highwayDrawer.addToneChangeHighlight(x);
		}
	}

	public static void addToneChanges(final FrameData frameData, final int panelWidth,
			final HighwayDrawer highwayDrawer, final GuitarDrawerUtils guitarDrawerUtils) {
		final Set<Integer> selectedIds = frameData.selection.getSelectedIdsSet(PositionType.TONE_CHANGE);
		final int highlightId = frameData.highlightData.getId(PositionType.TONE_CHANGE);
		final List<ToneChange> toneChanges = frameData.arrangement.toneChanges;

		drawPreviousTone(frameData, highwayDrawer, guitarDrawerUtils,
				frameData.highlightData.specialType == SpecialPositionType.TONE_PREVIOUS);

		for (int i = 0; i < toneChanges.size(); i++) {
			final ToneChange toneChange = toneChanges.get(i);
			final int x = positionToX(toneChange.position(frameData.beats), frameData.time);
			if (x < 0) {
				continue;
			}
			if (x > panelWidth) {
				break;
			}

			final boolean selected = selectedIds.contains(i);
			final boolean highlighted = i == highlightId;
			highwayDrawer.addToneChange(toneChange, x, selected, highlighted);
		}

		drawNextTone(frameData, highwayDrawer, guitarDrawerUtils,
				frameData.highlightData.specialType == SpecialPositionType.TONE_NEXT);

		drawHighlightedPositions(highwayDrawer, frameData.time, frameData.highlightData);
	}

}
