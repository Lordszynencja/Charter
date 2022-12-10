package log.charter.gui.chartPanelDrawers;

import java.awt.Graphics;

import log.charter.data.ChartData;
import log.charter.gui.ChartPanel;

public class SelectedNotesDrawer implements Drawer {
	private void drawGuitar(final Graphics g, final ChartPanel panel, final ChartData data) {
//		final DrawList selects = new DrawList();
//		final int w = panel.getWidth();
//
//		for (final int id : data.selectedNotes) {
//			final Note n = data.currentNotes.get(id);
//			final int x = timeToX(n.pos, data.t);
//			final int length = data.timeToXLength(n.getLength());
//			if (x > (w + (noteW / 2))) {
//				break;
//			}
//			if ((x + length) > 0) {
//				if (n.notes == 0) {
//					final int y = getLaneY(0, 6);
//					selects.addPositions(x - (noteW / 2) - 1, y - noteH6 / 2 - 1, noteW + 1, noteH6 + 1);
//				} else {
//					for (int c = 0; c < 5; c++) {
//						if ((n.notes & (1 << c)) > 0) {
//							final int y = getLaneY(c + 1, 6);
//							selects.addPositions(x - (noteW / 2) - 1, y - noteH6 / 2 - 1, noteW + 1, noteH6 + 1);
//						}
//					}
//				}
//			}
//		}
//
//		selects.draw(g, ChartPanel.colors.get("SELECT"));
	}

	@Override
	public void draw(final Graphics g, final ChartPanel panel, final ChartData data) {
		if (data.isEmpty) {
			return;
//		if (data.currentInstrument.type.isGuitarType()) {
//			drawGuitar(g, panel, data);
//		}
		}
	}
}
