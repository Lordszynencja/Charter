package log.charter.gui.chartPanelDrawers;

import static log.charter.gui.ChartPanel.isInLanes;

import java.awt.Graphics;

import log.charter.data.ChartData;
import log.charter.gui.ChartPanel;
import log.charter.gui.ChartPanelColors;
import log.charter.gui.ChartPanelColors.ColorLabel;

public class HighlightDrawer implements Drawer {
	private void drawHighlightNoteAdd(final Graphics g, final ChartData data, final int strings) {
//		final DrawList highlighted = new DrawList();
//		final int noteH = ChartPanel.getLaneSize(strings);
//
//		final List<IdOrPosWithLane> highlightedNotes = data.getHighlightedNotes();
//		for (final IdOrPosWithLane note : highlightedNotes) {
//			final int x = timeToX((int) note.pos, data.time);
//			final int y = getLaneY(note.lane, strings);
//			highlighted.addPositions(x - (noteWidth / 2), y - (noteH / 2), noteWidth - 1, noteH - 1);
//		}
//
//		highlighted.draw(g, ChartPanelColors.get(ColorLabel.HIGHLIGHT));
	}

	private void drawHighlightNoteDrag(final Graphics g, final ChartData data, final int lanes) {
//		final IdOrPos idOrPos = data.findClosestIdOrPosForX(data.mx, data.handler.isCtrl());
//		final int x = idOrPos.isId() ? timeToX(data.currentNotes.get(idOrPos.id).pos)//
//				: idOrPos.isPos() ? timeToX(idOrPos.pos,data.t) : -1;
//		final int noteH = lanes == 5 ? noteH5 : noteH6;
//
//		g.drawRect(x - (noteW / 2), clamp(data.my, lanes) - (noteH / 2), noteW - 1, noteH - 1);
	}

	private void drawDefaultHighlight(final Graphics g, final ChartData data, final int lanes) {
//		final IdOrPos idOrPos = data.findClosestIdOrPosForX(data.mx);
//		final int x = idOrPos.isId() ? data.timeToX(data.currentNotes.get(idOrPos.id).pos)//
//				: idOrPos.isPos() ? data.timeToX(idOrPos.pos) : -1;
//		final int noteH = lanes == 5 ? noteH5 : noteH6;
//
//		g.drawRect(x - (noteW / 2), clamp(data.my, lanes) - (noteH / 2), noteW - 1, noteH - 1);
	}

	private void drawGuitarHighlightNoteAdd(final Graphics g, final ChartData data) {
		drawHighlightNoteAdd(g, data, 6);
	}

	private void drawGuitarHighlightNoteDrag(final Graphics g, final ChartData data) {
		drawHighlightNoteDrag(g, data, 6);
	}

	private void drawGuitarDefaultHighlight(final Graphics g, final ChartData data) {
		drawDefaultHighlight(g, data, 6);
	}

	private void drawGuitarHighlight(final Graphics g, final ChartData data) {
		if (isInLanes(data.my)) {
			if (data.isNoteAdd) {
				drawGuitarHighlightNoteAdd(g, data);
			} else if (data.isNoteDrag) {
				drawGuitarHighlightNoteDrag(g, data);
			} else {
				drawGuitarDefaultHighlight(g, data);
			}
		}
	}

	private void drawVocalsHiglightNoteDrag(final Graphics g, final ChartData data) {
//		final IdOrPos idOrPos = data.findClosestVocalIdOrPosForX(data.mx, data.handler.isCtrl());
//		final int x = data.timeToX(idOrPos.isId() ? data.s.v.lyrics.get(idOrPos.id).pos : idOrPos.pos);
//		int xLength = idOrPos.isId() ? data.timeToXLength(data.s.v.lyrics.get(idOrPos.id).getLength()) - 1 : 10;
//		if (xLength < 1) {
//			xLength = 1;
//		}
//
//		final int y = getLaneY(0, 1) - 3;
//		g.drawRect(x, y, xLength, 7);
	}

	private void drawVocalsHiglightDefault(final Graphics g, final ChartData data) {
//		final IdOrPos idOrPos = data.findClosestVocalIdOrPosForX(data.mx);
//		final int x = data.timeToX(idOrPos.isId() ? data.s.v.lyrics.get(idOrPos.id).pos : idOrPos.pos);
//		int xLength = idOrPos.isId() ? data.timeToXLength(data.s.v.lyrics.get(idOrPos.id).getLength()) - 1 : 10;
//		if (xLength < 1) {
//			xLength = 1;
//		}
//
//		final int y = getLaneY(0, 1) - 3;
//		g.drawRect(x, y, xLength, 7);
	}

	private void drawVocalsHiglight(final Graphics g, final ChartData data) {
		if (data.isNoteDrag) {
			drawVocalsHiglightNoteDrag(g, data);
		} else {
			drawVocalsHiglightDefault(g, data);
		}
	}

	@Override
	public void draw(final Graphics g, final ChartPanel panel, final ChartData data) {
		if (data.isEmpty) {
			return;
		}

		g.setColor(ChartPanelColors.get(ColorLabel.HIGHLIGHT));
//		if (data.currentInstrument.type.isGuitarType()) {
//			drawGuitarHighlight(g, data);
//		} else if (data.currentInstrument.type.isVocalsType()) {
//			drawVocalsHiglight(g, data);
//		}
	}
}
