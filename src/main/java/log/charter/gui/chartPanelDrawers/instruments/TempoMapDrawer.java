package log.charter.gui.chartPanelDrawers.instruments;

import java.awt.Graphics;

import log.charter.gui.chartPanelDrawers.common.BeatsDrawer;
import log.charter.gui.chartPanelDrawers.common.WaveFormDrawer;
import log.charter.gui.chartPanelDrawers.data.HighlightData;

public class TempoMapDrawer {
	private WaveFormDrawer audioDrawer;
	private BeatsDrawer beatsDrawer;

	public void init(final WaveFormDrawer audioDrawer, final BeatsDrawer beatsDrawer) {
		this.audioDrawer = audioDrawer;
		this.beatsDrawer = beatsDrawer;
	}

	public void draw(final Graphics g, final HighlightData highlightData) {
		audioDrawer.draw(g);
		beatsDrawer.draw(g, highlightData);
	}
}
