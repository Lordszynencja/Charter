package log.charter.gui.chartPanelDrawers.common;

import static log.charter.gui.chartPanelDrawers.instruments.VocalsDrawer.getVocalNotePosition;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.Map;

import log.charter.data.ChartData;
import log.charter.gui.ChartPanelColors;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.HighlightManager;
import log.charter.gui.PositionWithIdAndType;
import log.charter.gui.PositionWithIdAndType.PositionType;
import log.charter.gui.chartPanelDrawers.drawableShapes.StrokedRectangle;

public class HighlightDrawer {
	private static final Color highlightColor = ChartPanelColors.get(ColorLabel.HIGHLIGHT);

	private static interface HighlightTypeDrawer {
		void drawHighlight(Graphics g, PositionWithIdAndType highlight);
	}

	private ChartData data;
	private HighlightManager highlightManager;

	public void init(final ChartData data, final HighlightManager highlightManager) {
		this.data = data;
		this.highlightManager = highlightManager;
	}

	private void drawBeatHighlight(final Graphics g, final PositionWithIdAndType highlight) {

	}

	private void drawBeaGuitarNoteHighlight(final Graphics g, final PositionWithIdAndType highlight) {

	}

	private void drawHandShapeHighlight(final Graphics g, final PositionWithIdAndType highlight) {

	}

	private void drawNoneHighlight(final Graphics g, final PositionWithIdAndType highlight) {

	}

	private void drawVocalHighlight(final Graphics g, final PositionWithIdAndType highlight) {
		final int position = highlight.position;
		final int length = highlight.vocal == null ? 50 : highlight.vocal.length;
		new StrokedRectangle(getVocalNotePosition(position, length, data.time), highlightColor).draw(g);
	}

	private final Map<PositionType, HighlightTypeDrawer> highlightDrawers = new HashMap<>();

	{
		highlightDrawers.put(PositionType.BEAT, this::drawBeatHighlight);
		highlightDrawers.put(PositionType.GUITAR_NOTE, this::drawBeaGuitarNoteHighlight);
		highlightDrawers.put(PositionType.HAND_SHAPE, this::drawHandShapeHighlight);
		highlightDrawers.put(PositionType.NONE, this::drawNoneHighlight);
		highlightDrawers.put(PositionType.VOCAL, this::drawVocalHighlight);
	}

	public void draw(final Graphics g) {
		final PositionWithIdAndType highlight = highlightManager.getHighlight();
		highlightDrawers.get(highlight.type).drawHighlight(g, highlight);
	}
}
