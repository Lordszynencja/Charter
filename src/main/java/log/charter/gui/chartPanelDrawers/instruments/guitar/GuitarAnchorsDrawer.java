package log.charter.gui.chartPanelDrawers.instruments.guitar;

import static log.charter.util.CollectionUtils.lastBeforeEqual;
import static log.charter.util.ScalingUtils.positionToX;
import static log.charter.util.ScalingUtils.xToPosition;

import java.util.List;
import java.util.Set;

import log.charter.data.song.Anchor;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.types.PositionType;
import log.charter.gui.chartPanelDrawers.data.FrameData;
import log.charter.gui.chartPanelDrawers.data.HighlightData;
import log.charter.gui.chartPanelDrawers.data.HighlightData.HighlightPosition;
import log.charter.gui.chartPanelDrawers.instruments.guitar.highway.HighwayDrawer;

public class GuitarAnchorsDrawer {
	private static Anchor findCurrentAnchor(final FrameData frameData, final double edgeTime) {
		final FractionalPosition edgePosition = FractionalPosition.fromTime(frameData.beats, edgeTime);

		return lastBeforeEqual(frameData.level.anchors, edgePosition).find();
	}

	private static void drawCurrentAnchor(final FrameData frameData, final HighwayDrawer highwayDrawer,
			final double edgeTime, final int nextAnchorX) {
		final Anchor anchor = findCurrentAnchor(frameData, edgeTime);
		if (anchor == null) {
			return;
		}

		highwayDrawer.addCurrentAnchor(frameData.g, anchor, nextAnchorX);
	}

	private static void drawCurrentAnchor(final FrameData frameData, final HighwayDrawer highwayDrawer,
			final double edgeTime) {
		final Anchor anchor = findCurrentAnchor(frameData, edgeTime);
		if (anchor == null) {
			return;
		}

		highwayDrawer.addCurrentAnchor(frameData.g, anchor);
	}

	private static void drawHighlightedPositions(final HighwayDrawer highwayDrawer, final double time,
			final HighlightData highlightData) {
		if (highlightData.type != PositionType.ANCHOR) {
			return;
		}

		for (final HighlightPosition highlightPosition : highlightData.highlightedNonIdPositions) {
			final int x = positionToX(highlightPosition.position, time);
			highwayDrawer.addAnchorHighlight(x);
		}
	}

	public static void addAnchors(final FrameData frameData, final int panelWidth, final HighwayDrawer highwayDrawer) {
		final int highlightId = frameData.highlightData.getId(PositionType.ANCHOR);
		final double edgeTime = xToPosition(0, frameData.time);
		final List<Anchor> anchors = frameData.level.anchors;
		final Set<Integer> selectedIds = frameData.selection.getSelectedIdsSet(PositionType.ANCHOR);

		boolean currentAnchorDrawn = false;
		for (int i = 0; i < anchors.size(); i++) {
			final Anchor anchor = anchors.get(i);
			final int x = positionToX(anchor.position(frameData.beats), frameData.time);
			if (x < 0) {
				continue;
			}
			if (x > panelWidth) {
				break;
			}

			final boolean selected = selectedIds.contains(i);
			final boolean highlighted = i == highlightId;
			highwayDrawer.addAnchor(anchor, x, selected, highlighted);
			if (!currentAnchorDrawn) {
				drawCurrentAnchor(frameData, highwayDrawer, edgeTime, x);
				currentAnchorDrawn = true;
			}
		}
		if (!currentAnchorDrawn) {
			drawCurrentAnchor(frameData, highwayDrawer, edgeTime);
		}

		drawHighlightedPositions(highwayDrawer, frameData.time, frameData.highlightData);
	}
}
