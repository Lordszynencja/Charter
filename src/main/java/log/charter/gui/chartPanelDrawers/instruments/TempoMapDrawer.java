package log.charter.gui.chartPanelDrawers.instruments;

import java.awt.Graphics;

import log.charter.gui.chartPanelDrawers.data.HighlightData;
import log.charter.gui.chartPanelDrawers.instruments.guitar.GuitarDrawer;

public class TempoMapDrawer {
	private GuitarDrawer guitarDrawer;

	public void init(final GuitarDrawer guitarDrawer) {
		this.guitarDrawer = guitarDrawer;
	}

	public void draw(final Graphics g, final int time, final HighlightData highlightData) {
		guitarDrawer.draw(g, time, highlightData);
	}
}
