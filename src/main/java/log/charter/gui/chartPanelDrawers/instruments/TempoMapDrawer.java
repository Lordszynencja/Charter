package log.charter.gui.chartPanelDrawers.instruments;

import java.awt.Graphics;

import log.charter.gui.chartPanelDrawers.common.BeatsDrawer;
import log.charter.gui.chartPanelDrawers.common.waveform.WaveFormDrawer;
import log.charter.gui.chartPanelDrawers.data.HighlightData;

public class TempoMapDrawer {
	private BeatsDrawer beatsDrawer;
	private WaveFormDrawer waveFormDrawer;

	public void init(final BeatsDrawer beatsDrawer, final WaveFormDrawer waveFormDrawer) {
		this.beatsDrawer = beatsDrawer;
		this.waveFormDrawer = waveFormDrawer;
	}

	public void draw(final Graphics g, final int time, final HighlightData highlightData) {
		waveFormDrawer.draw(g, time);
		beatsDrawer.draw(g, time, highlightData);
	}
}
