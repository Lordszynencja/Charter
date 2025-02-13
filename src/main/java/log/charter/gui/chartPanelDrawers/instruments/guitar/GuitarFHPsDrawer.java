package log.charter.gui.chartPanelDrawers.instruments.guitar;

import static log.charter.util.CollectionUtils.lastBeforeEqual;
import static log.charter.util.ScalingUtils.positionToX;
import static log.charter.util.ScalingUtils.xToPosition;

import java.util.List;
import java.util.Set;

import log.charter.data.song.FHP;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.types.PositionType;
import log.charter.gui.chartPanelDrawers.data.FrameData;
import log.charter.gui.chartPanelDrawers.data.HighlightData;
import log.charter.gui.chartPanelDrawers.data.HighlightData.HighlightPosition;
import log.charter.gui.chartPanelDrawers.instruments.guitar.highway.HighwayDrawer;

public class GuitarFHPsDrawer {
	private static FHP findCurrentFHP(final FrameData frameData, final double edgeTime) {
		final FractionalPosition edgePosition = FractionalPosition.fromTime(frameData.beats, edgeTime);

		return lastBeforeEqual(frameData.level.fhps, edgePosition).find();
	}

	private static void drawCurrentFHP(final FrameData frameData, final HighwayDrawer highwayDrawer,
			final double edgeTime, final int nextFHPX) {
		final FHP fhp = findCurrentFHP(frameData, edgeTime);
		if (fhp == null) {
			return;
		}

		highwayDrawer.addCurrentFHP(frameData.g, fhp, nextFHPX);
	}

	private static void drawCurrentFHP(final FrameData frameData, final HighwayDrawer highwayDrawer,
			final double edgeTime) {
		final FHP fhp = findCurrentFHP(frameData, edgeTime);
		if (fhp == null) {
			return;
		}

		highwayDrawer.addCurrentFHP(frameData.g, fhp);
	}

	private static void drawHighlightedPositions(final HighwayDrawer highwayDrawer, final double time,
			final HighlightData highlightData) {
		if (highlightData.type != PositionType.FHP) {
			return;
		}

		for (final HighlightPosition highlightPosition : highlightData.highlightedNonIdPositions) {
			final int x = positionToX(highlightPosition.position, time);
			highwayDrawer.addFHPHighlight(x);
		}
	}

	public static void addFHPs(final FrameData frameData, final int panelWidth, final HighwayDrawer highwayDrawer) {
		final int highlightId = frameData.highlightData.getId(PositionType.FHP);
		final double edgeTime = xToPosition(0, frameData.time);
		final List<FHP> fhps = frameData.level.fhps;
		final Set<Integer> selectedIds = frameData.selection.getSelectedIdsSet(PositionType.FHP);

		boolean currentFHPDrawn = false;
		for (int i = 0; i < fhps.size(); i++) {
			final FHP fhp = fhps.get(i);
			final int x = positionToX(fhp.position(frameData.beats), frameData.time);
			if (x < 0) {
				continue;
			}
			if (x > panelWidth) {
				break;
			}

			final boolean selected = selectedIds.contains(i);
			final boolean highlighted = i == highlightId;
			highwayDrawer.addFHP(fhp, x, selected, highlighted);
			if (!currentFHPDrawn) {
				drawCurrentFHP(frameData, highwayDrawer, edgeTime, x);
				currentFHPDrawn = true;
			}
		}
		if (!currentFHPDrawn) {
			drawCurrentFHP(frameData, highwayDrawer, edgeTime);
		}

		drawHighlightedPositions(highwayDrawer, frameData.time, frameData.highlightData);
	}
}
