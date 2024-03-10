package log.charter.gui.chartPanelDrawers.instruments.guitar;

import static log.charter.util.CollectionUtils.filter;
import static log.charter.util.ScalingUtils.timeToX;
import static log.charter.util.ScalingUtils.xToTime;

import java.awt.Graphics2D;

import log.charter.data.song.Anchor;
import log.charter.data.types.PositionType;
import log.charter.gui.chartPanelDrawers.data.HighlightData;
import log.charter.gui.chartPanelDrawers.data.HighlightData.HighlightPosition;
import log.charter.gui.chartPanelDrawers.instruments.guitar.highway.HighwayDrawer;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashSet2;

public class GuitarAnchorsDrawer {
	private static Anchor findCurrentAnchor(final ArrayList2<Anchor> anchors, final int time) {
		final ArrayList2<Anchor> previousAnchors = filter(anchors, //
				anchor -> anchor.position() < time, ArrayList2::new);

		return previousAnchors.isEmpty() ? null : previousAnchors.getLast();
	}

	private static void drawCurrentAnchor(final Graphics2D g, final HighwayDrawer highwayDrawer,
			final ArrayList2<Anchor> anchors, final int time, final int nextAnchorX) {
		final Anchor anchor = findCurrentAnchor(anchors, time);
		if (anchor == null) {
			return;
		}

		highwayDrawer.addCurrentAnchor(g, anchor, nextAnchorX);
	}

	private static void drawCurrentAnchor(final Graphics2D g, final HighwayDrawer highwayDrawer,
			final ArrayList2<Anchor> anchors, final int time) {
		final Anchor anchor = findCurrentAnchor(anchors, time);
		if (anchor == null) {
			return;
		}

		highwayDrawer.addCurrentAnchor(g, anchor);
	}

	private static void drawHighlightedPositions(final HighwayDrawer highwayDrawer, final int time,
			final HighlightData highlightData) {
		if (highlightData.type != PositionType.ANCHOR) {
			return;
		}

		for (final HighlightPosition highlightPosition : highlightData.highlightedNonIdPositions) {
			final int x = timeToX(highlightPosition.position, time);
			highwayDrawer.addAnchorHighlight(x);
		}
	}

	public static void addAnchors(final Graphics2D g, final int panelWidth, final HighwayDrawer highwayDrawer,
			final ArrayList2<Anchor> anchors, final int time, final HashSet2<Integer> selectedIds,
			final HighlightData highlightData) {
		final int highlightId = highlightData.getId(PositionType.ANCHOR);
		final int leftScreenEdgeTime = xToTime(0, time);

		boolean currentAnchorDrawn = false;
		for (int i = 0; i < anchors.size(); i++) {
			final Anchor anchor = anchors.get(i);
			final int x = timeToX(anchor.position(), time);
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
				drawCurrentAnchor(g, highwayDrawer, anchors, leftScreenEdgeTime, x);
				currentAnchorDrawn = true;
			}
		}
		if (!currentAnchorDrawn) {
			drawCurrentAnchor(g, highwayDrawer, anchors, leftScreenEdgeTime);
		}

		drawHighlightedPositions(highwayDrawer, time, highlightData);
	}
}
