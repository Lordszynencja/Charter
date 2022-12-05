package log.charter.gui.chartPanelDrawers;

import static log.charter.gui.ChartPanel.getLaneY;
import static log.charter.gui.ChartPanel.noteH5;
import static log.charter.gui.ChartPanel.noteH6;
import static log.charter.gui.ChartPanel.noteW;

import java.awt.Graphics;

import log.charter.data.ChartData;
import log.charter.gui.ChartPanel;
import log.charter.gui.chartPanelDrawers.lists.DrawList;
import log.charter.song.Lyric;
import log.charter.song.Note;

public class SelectedNotesDrawer implements Drawer {
	private void drawGuitar(final Graphics g, final ChartPanel panel, final ChartData data) {
		final DrawList selects = new DrawList();
		final int w = panel.getWidth();

		for (final int id : data.selectedNotes) {
			final Note n = data.currentNotes.get(id);
			final int x = data.timeToX(n.pos);
			final int length = data.timeToXLength(n.getLength());
			if (x > (w + (noteW / 2))) {
				break;
			}
			if ((x + length) > 0) {
				if (n.notes == 0) {
					final int y = getLaneY(0, 6);
					selects.addPositions(x - (noteW / 2) - 1, y - noteH6 / 2 - 1, noteW + 1, noteH6 + 1);
				} else {
					for (int c = 0; c < 5; c++) {
						if ((n.notes & (1 << c)) > 0) {
							final int y = getLaneY(c + 1, 6);
							selects.addPositions(x - (noteW / 2) - 1, y - noteH6 / 2 - 1, noteW + 1, noteH6 + 1);
						}
					}
				}
			}
		}

		selects.draw(g, ChartPanel.colors.get("SELECT"));
	}

	private void drawDrumsKeys(final Graphics g, final ChartPanel panel, final ChartData data) {
		final DrawList selects = new DrawList();
		final int w = panel.getWidth();

		for (final int id : data.selectedNotes) {
			final Note n = data.currentNotes.get(id);
			final int x = data.timeToX(n.pos);
			final int length = data.timeToXLength(n.getLength());
			if (x > (w + (noteW / 2))) {
				break;
			}
			if ((x + length) > 0) {
				for (int c = 0; c < 5; c++) {
					if ((n.notes & (1 << c)) > 0) {
						final int y = getLaneY(c, 5);
						selects.addPositions(x - (noteW / 2) - 1, y - noteH5 / 2 - 1, noteW + 1, noteH5 + 1);
					}
				}
			}
		}

		selects.draw(g, ChartPanel.colors.get("SELECT"));
	}

	private void drawVocals(final Graphics g, final ChartPanel panel, final ChartData data) {
		final DrawList selects = new DrawList();
		final int w = panel.getWidth();

		for (final int id : data.selectedNotes) {
			final Lyric l = data.s.v.lyrics.get(id);
			final int x = data.timeToX(l.pos);
			final int y = getLaneY(0, 1) - 4;
			int length = data.timeToXLength(l.getLength()) + 1;
			if (length < 3) {
				length = 3;
			}
			if (x > (w + (ChartPanel.noteW / 2))) {
				break;
			}
			if ((x + length) > 0) {
				for (int c = 0; c < 5; c++) {
					selects.addPositions(x - 1, y, length, 9);
				}
			}
		}

		selects.draw(g, ChartPanel.colors.get("SELECT"));
	}

	@Override
	public void draw(final Graphics g, final ChartPanel panel, final ChartData data) {
		if (data.currentInstrument.type.isGuitarType()) {
			drawGuitar(g, panel, data);
		} else if (data.currentInstrument.type.isDrumsType() || data.currentInstrument.type.isKeysType()) {
			drawDrumsKeys(g, panel, data);
		} else if (data.currentInstrument.type.isVocalsType()) {
			drawVocals(g, panel, data);
		}
	}
}
