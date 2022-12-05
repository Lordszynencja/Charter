package log.charter.gui.chartPanelDrawers;

import static log.charter.gui.ChartPanel.clamp;
import static log.charter.gui.ChartPanel.getLaneY;
import static log.charter.gui.ChartPanel.isInLanes;
import static log.charter.gui.ChartPanel.noteH5;
import static log.charter.gui.ChartPanel.noteH6;
import static log.charter.gui.ChartPanel.noteW;

import java.awt.Graphics;
import java.util.List;

import log.charter.data.ChartData;
import log.charter.data.IdOrPos;
import log.charter.data.IdOrPosWithLane;
import log.charter.gui.ChartPanel;
import log.charter.gui.chartPanelDrawers.lists.DrawList;

public class HighlightDrawer implements Drawer {
	private void drawHighlightNoteAdd(final Graphics g, final ChartData data, final int lanes) {
		final DrawList highlighted = new DrawList();
		final int noteH = lanes == 5 ? noteH5 : noteH6;

		final List<IdOrPosWithLane> highlightedNotes = data.getHighlightedNotes();
		for (final IdOrPosWithLane note : highlightedNotes) {
			final int x = data.timeToX(note.pos);
			final int y = getLaneY(note.lane, lanes);
			highlighted.addPositions(x - (noteW / 2), y - (noteH / 2), noteW - 1, noteH - 1);
		}

		highlighted.draw(g, ChartPanel.colors.get("HIGHLIGHT"));
	}

	private void drawHighlightNoteDrag(final Graphics g, final ChartData data, final int lanes) {
		final IdOrPos idOrPos = data.findClosestIdOrPosForX(data.mx, data.handler.isCtrl());
		final int x = idOrPos.isId() ? data.timeToX(data.currentNotes.get(idOrPos.id).pos)//
				: idOrPos.isPos() ? data.timeToX(idOrPos.pos) : -1;
		final int noteH = lanes == 5 ? noteH5 : noteH6;

		g.drawRect(x - (noteW / 2), clamp(data.my, lanes) - (noteH / 2), noteW - 1, noteH - 1);
	}

	private void drawDefaultHighlight(final Graphics g, final ChartData data, final int lanes) {
		final IdOrPos idOrPos = data.findClosestIdOrPosForX(data.mx);
		final int x = idOrPos.isId() ? data.timeToX(data.currentNotes.get(idOrPos.id).pos)//
				: idOrPos.isPos() ? data.timeToX(idOrPos.pos) : -1;
		final int noteH = lanes == 5 ? noteH5 : noteH6;

		g.drawRect(x - (noteW / 2), clamp(data.my, lanes) - (noteH / 2), noteW - 1, noteH - 1);
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

	private void drawDrumsKeysHighlightNoteAdd(final Graphics g, final ChartData data) {
		drawHighlightNoteAdd(g, data, 5);
	}

	private void drawDrumsKeysHighlightNoteDrag(final Graphics g, final ChartData data) {
		drawHighlightNoteDrag(g, data, 5);
	}

	private void drawDrumsKeysDefaultHighlight(final Graphics g, final ChartData data) {
		drawDefaultHighlight(g, data, 5);
	}

	private void drawDrumsKeysHighlight(final Graphics g, final ChartData data) {
		if (isInLanes(data.my)) {
			if (data.isNoteAdd) {
				drawDrumsKeysHighlightNoteAdd(g, data);
			} else if (data.isNoteDrag) {
				drawDrumsKeysHighlightNoteDrag(g, data);
			} else {
				drawDrumsKeysDefaultHighlight(g, data);
			}
		}
	}

	private void drawVocalsHiglightNoteDrag(final Graphics g, final ChartData data) {
		final IdOrPos idOrPos = data.findClosestVocalIdOrPosForX(data.mx, data.handler.isCtrl());
		final int x = data.timeToX(idOrPos.isId() ? data.s.v.lyrics.get(idOrPos.id).pos : idOrPos.pos);
		int xLength = idOrPos.isId() ? data.timeToXLength(data.s.v.lyrics.get(idOrPos.id).getLength()) - 1 : 10;
		if (xLength < 1) {
			xLength = 1;
		}

		final int y = getLaneY(0, 1) - 3;
		g.drawRect(x, y, xLength, 7);
	}

	private void drawVocalsHiglightDefault(final Graphics g, final ChartData data) {
		final IdOrPos idOrPos = data.findClosestVocalIdOrPosForX(data.mx);
		final int x = data.timeToX(idOrPos.isId() ? data.s.v.lyrics.get(idOrPos.id).pos : idOrPos.pos);
		int xLength = idOrPos.isId() ? data.timeToXLength(data.s.v.lyrics.get(idOrPos.id).getLength()) - 1 : 10;
		if (xLength < 1) {
			xLength = 1;
		}

		final int y = getLaneY(0, 1) - 3;
		g.drawRect(x, y, xLength, 7);
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
		g.setColor(ChartPanel.colors.get("HIGHLIGHT"));
		if (data.currentInstrument.type.isGuitarType()) {
			drawGuitarHighlight(g, data);
		} else if (data.currentInstrument.type.isDrumsType() || data.currentInstrument.type.isKeysType()) {
			drawDrumsKeysHighlight(g, data);
		} else if (data.currentInstrument.type.isVocalsType()) {
			drawVocalsHiglight(g, data);
		}
	}
}
