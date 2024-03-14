package log.charter.gui.chartPanelDrawers.instruments.guitar;

import static log.charter.util.CollectionUtils.filter;
import static log.charter.util.ScalingUtils.timeToX;
import static log.charter.util.ScalingUtils.xToTime;

import java.awt.Graphics2D;
import java.util.List;
import java.util.Map;
import java.util.Set;

import log.charter.data.song.EventPoint;
import log.charter.data.song.Phrase;
import log.charter.data.types.PositionType;
import log.charter.gui.chartPanelDrawers.data.HighlightData;
import log.charter.gui.chartPanelDrawers.data.HighlightData.HighlightPosition;
import log.charter.gui.chartPanelDrawers.instruments.guitar.highway.HighwayDrawer;

public class GuitarEventPointsDrawer {
	private static EventPoint findCurrentSection(final List<EventPoint> eventPoints, final int time) {
		final List<EventPoint> sections = filter(eventPoints, //
				eventPoint -> eventPoint.section != null && eventPoint.position() < time);

		return sections.isEmpty() ? null : sections.get(sections.size() - 1);
	}

	private static void drawCurrentSection(final Graphics2D g, final HighwayDrawer highwayDrawer,
			final List<EventPoint> eventPoints, final int time, final int nextEventPointX) {
		final EventPoint section = findCurrentSection(eventPoints, time);
		if (section == null) {
			return;
		}

		highwayDrawer.addCurrentSection(g, section.section, nextEventPointX);
	}

	private static void drawCurrentSection(final Graphics2D g, final HighwayDrawer highwayDrawer,
			final List<EventPoint> eventPoints, final int time) {
		final EventPoint section = findCurrentSection(eventPoints, time);
		if (section == null) {
			return;
		}

		highwayDrawer.addCurrentSection(g, section.section);
	}

	private static EventPoint findCurrentPhrase(final List<EventPoint> eventPoints, final int time) {
		final List<EventPoint> sections = filter(eventPoints, //
				eventPoint -> eventPoint.hasPhrase() && eventPoint.position() < time);

		return sections.isEmpty() ? null : sections.get(sections.size() - 1);
	}

	private static void drawCurrentPhrase(final Graphics2D g, final HighwayDrawer highwayDrawer,
			final Map<String, Phrase> phrases, final List<EventPoint> eventPoints, final int time,
			final int nextEventPointX) {
		final EventPoint section = findCurrentPhrase(eventPoints, time);
		if (section == null) {
			return;
		}

		highwayDrawer.addCurrentPhrase(g, phrases.get(section.phrase), section.phrase, nextEventPointX);
	}

	private static void drawCurrentPhrase(final Graphics2D g, final HighwayDrawer highwayDrawer,
			final Map<String, Phrase> phrases, final List<EventPoint> eventPoints, final int time) {
		final EventPoint section = findCurrentPhrase(eventPoints, time);
		if (section == null) {
			return;
		}

		highwayDrawer.addCurrentPhrase(g, phrases.get(section.phrase), section.phrase);
	}

	private static void drawHighlightedPositions(final HighwayDrawer highwayDrawer, final int time,
			final HighlightData highlightData) {
		if (highlightData.type != PositionType.EVENT_POINT) {
			return;
		}

		for (final HighlightPosition highlightPosition : highlightData.highlightedNonIdPositions) {
			final int x = timeToX(highlightPosition.position, time);
			highwayDrawer.addEventPointHighlight(x);
		}
	}

	public static void addEventPoints(final Graphics2D g, final int panelWidth, final HighwayDrawer highwayDrawer,
			final List<EventPoint> eventPoints, final Map<String, Phrase> phrases, final int time,
			final Set<Integer> selectedIds, final HighlightData highlightData) {
		final int highlightId = highlightData.getId(PositionType.EVENT_POINT);
		final int leftScreenEdgeTime = xToTime(0, time);

		boolean currentSectionDrawn = false;
		boolean currentPhraseDrawn = false;
		for (int i = 0; i < eventPoints.size(); i++) {
			final EventPoint eventPoint = eventPoints.get(i);
			final int x = timeToX(eventPoint.position(), time);
			if (x < 0) {
				continue;
			}
			if (x > panelWidth) {
				break;
			}

			final boolean selected = selectedIds.contains(i);
			final boolean highlighted = i == highlightId;
			highwayDrawer.addEventPoint(g, eventPoint, phrases.get(eventPoint.phrase), x, selected, highlighted);
			if (eventPoint.section != null && !currentSectionDrawn) {
				drawCurrentSection(g, highwayDrawer, eventPoints, leftScreenEdgeTime, x);
				currentSectionDrawn = true;
			}
			if (eventPoint.phrase != null && !currentPhraseDrawn) {
				drawCurrentPhrase(g, highwayDrawer, phrases, eventPoints, leftScreenEdgeTime, x);
				currentPhraseDrawn = true;
			}
		}
		if (!currentSectionDrawn) {
			drawCurrentSection(g, highwayDrawer, eventPoints, leftScreenEdgeTime);
		}
		if (!currentPhraseDrawn) {
			drawCurrentPhrase(g, highwayDrawer, phrases, eventPoints, leftScreenEdgeTime);
		}

		drawHighlightedPositions(highwayDrawer, time, highlightData);
	}
}
