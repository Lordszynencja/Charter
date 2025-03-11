package log.charter.gui.chartPanelDrawers.instruments.guitar;

import static log.charter.util.CollectionUtils.filter;
import static log.charter.util.ScalingUtils.positionToX;
import static log.charter.util.ScalingUtils.xToPosition;

import java.awt.Graphics2D;
import java.util.List;
import java.util.Map;
import java.util.Set;

import log.charter.data.song.EventPoint;
import log.charter.data.song.Phrase;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.types.PositionType;
import log.charter.gui.chartPanelDrawers.data.FrameData;
import log.charter.gui.chartPanelDrawers.data.HighlightData;
import log.charter.gui.chartPanelDrawers.data.HighlightData.HighlightPosition;
import log.charter.gui.chartPanelDrawers.instruments.guitar.highway.HighwayDrawer;

public class GuitarEventPointsDrawer {
	private static EventPoint findCurrentSection(final List<EventPoint> eventPoints, final FractionalPosition time) {
		final List<EventPoint> sections = filter(eventPoints, //
				eventPoint -> eventPoint.section != null && eventPoint.compareTo(time) < 0);

		return sections.isEmpty() ? null : sections.get(sections.size() - 1);
	}

	private static void drawCurrentSection(final Graphics2D g, final HighwayDrawer highwayDrawer,
			final List<EventPoint> eventPoints, final FractionalPosition time, final int nextEventPointX) {
		final EventPoint section = findCurrentSection(eventPoints, time);
		if (section == null) {
			return;
		}

		highwayDrawer.addCurrentSection(g, section.section, nextEventPointX);
	}

	private static void drawCurrentSection(final Graphics2D g, final HighwayDrawer highwayDrawer,
			final List<EventPoint> eventPoints, final FractionalPosition time) {
		final EventPoint section = findCurrentSection(eventPoints, time);
		if (section == null) {
			return;
		}

		highwayDrawer.addCurrentSection(g, section.section);
	}

	private static EventPoint findCurrentPhrase(final List<EventPoint> eventPoints, final FractionalPosition time) {
		final List<EventPoint> sections = filter(eventPoints, //
				eventPoint -> eventPoint.hasPhrase() && eventPoint.compareTo(time) < 0);

		return sections.isEmpty() ? null : sections.get(sections.size() - 1);
	}

	private static void drawCurrentPhrase(final Graphics2D g, final HighwayDrawer highwayDrawer,
			final Map<String, Phrase> phrases, final List<EventPoint> eventPoints, final FractionalPosition time,
			final int nextEventPointX) {
		final EventPoint section = findCurrentPhrase(eventPoints, time);
		if (section == null) {
			return;
		}

		highwayDrawer.addCurrentPhrase(g, phrases.get(section.phrase), section.phrase, nextEventPointX);
	}

	private static void drawCurrentPhrase(final Graphics2D g, final HighwayDrawer highwayDrawer,
			final Map<String, Phrase> phrases, final List<EventPoint> eventPoints, final FractionalPosition time) {
		final EventPoint section = findCurrentPhrase(eventPoints, time);
		if (section == null) {
			return;
		}

		highwayDrawer.addCurrentPhrase(g, phrases.get(section.phrase), section.phrase);
	}

	private static void drawHighlightedPositions(final HighwayDrawer highwayDrawer, final double time,
			final HighlightData highlightData) {
		if (highlightData.type != PositionType.EVENT_POINT) {
			return;
		}

		for (final HighlightPosition highlightPosition : highlightData.highlightedNonIdPositions) {
			final int x = positionToX(highlightPosition.position, time);
			highwayDrawer.addEventPointHighlight(x);
		}
	}

	public static void addEventPoints(final FrameData frameData, final int panelWidth,
			final HighwayDrawer highwayDrawer) {
		final List<EventPoint> eventPoints = frameData.arrangement.eventPoints;
		final Map<String, Phrase> phrases = frameData.arrangement.phrases;
		final Set<Integer> selectedIds = frameData.selection.getSelectedIdsSet(PositionType.EVENT_POINT);
		final int highlightId = frameData.highlightData.getId(PositionType.EVENT_POINT);
		final FractionalPosition leftScreenEdgeTime = FractionalPosition.fromTime(frameData.beats,
				xToPosition(0, frameData.time));

		boolean currentSectionDrawn = false;
		boolean currentPhraseDrawn = false;
		for (int i = 0; i < frameData.arrangement.eventPoints.size(); i++) {
			final EventPoint eventPoint = eventPoints.get(i);
			final int x = positionToX(eventPoint.position(frameData.beats), frameData.time);
			if (x < 0) {
				highwayDrawer.addEvents(frameData.g, eventPoint, x);
				continue;
			}
			if (x > panelWidth) {
				break;
			}

			final boolean selected = selectedIds.contains(i);
			final boolean highlighted = i == highlightId;
			highwayDrawer.addEventPoint(frameData.g, eventPoint, phrases.get(eventPoint.phrase), x, selected,
					highlighted);
			if (eventPoint.section != null && !currentSectionDrawn) {
				drawCurrentSection(frameData.g, highwayDrawer, eventPoints, leftScreenEdgeTime, x);
				currentSectionDrawn = true;
			}
			if (eventPoint.phrase != null && !currentPhraseDrawn) {
				drawCurrentPhrase(frameData.g, highwayDrawer, phrases, eventPoints, leftScreenEdgeTime, x);
				currentPhraseDrawn = true;
			}
		}
		if (!currentSectionDrawn) {
			drawCurrentSection(frameData.g, highwayDrawer, eventPoints, leftScreenEdgeTime);
		}
		if (!currentPhraseDrawn) {
			drawCurrentPhrase(frameData.g, highwayDrawer, phrases, eventPoints, leftScreenEdgeTime);
		}

		drawHighlightedPositions(highwayDrawer, frameData.time, frameData.highlightData);
	}
}
