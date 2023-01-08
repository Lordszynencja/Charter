package log.charter.gui.chartPanelDrawers.instruments;

import java.awt.Graphics;

import log.charter.gui.chartPanelDrawers.common.AudioDrawer;
import log.charter.gui.chartPanelDrawers.common.BeatsDrawer;

public class TempoMapDrawer {
	private AudioDrawer audioDrawer;
	private BeatsDrawer beatsDrawer;

	public void init(final AudioDrawer audioDrawer, final BeatsDrawer beatsDrawer) {
		this.audioDrawer = audioDrawer;
		this.beatsDrawer = beatsDrawer;
	}

	public void draw(final Graphics g) {
		audioDrawer.draw(g);
		beatsDrawer.draw(g);
	}
}
