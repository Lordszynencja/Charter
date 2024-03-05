package log.charter.gui.chartPanelDrawers.instruments;

import static log.charter.data.config.GraphicalConfig.tempoMapGhostNotesTransparency;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import log.charter.gui.chartPanelDrawers.common.BeatsDrawer;
import log.charter.gui.chartPanelDrawers.common.LyricLinesDrawer;
import log.charter.gui.chartPanelDrawers.common.waveform.WaveFormDrawer;
import log.charter.gui.chartPanelDrawers.data.HighlightData;
import log.charter.gui.chartPanelDrawers.instruments.guitar.GuitarDrawer;

public class TempoMapDrawer {
	private BeatsDrawer beatsDrawer;
	private GuitarDrawer guitarDrawer;
	private LyricLinesDrawer lyricLinesDrawer;
	private WaveFormDrawer waveFormDrawer;

	public void init(final BeatsDrawer beatsDrawer, final GuitarDrawer guitarDrawer,
			final LyricLinesDrawer lyricLinesDrawer, final WaveFormDrawer waveFormDrawer) {
		this.beatsDrawer = beatsDrawer;
		this.guitarDrawer = guitarDrawer;
		this.lyricLinesDrawer = lyricLinesDrawer;
		this.waveFormDrawer = waveFormDrawer;
	}

	private void drawGhostNotes(final Graphics g, final int time, final HighlightData highlightData) {
		if (tempoMapGhostNotesTransparency <= 0) {
			return;
		}

		final Rectangle bounds = g.getClipBounds();
		final BufferedImage image = new BufferedImage((int) bounds.getWidth(), (int) bounds.getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		final Graphics bufferGraphics = image.getGraphics();
		lyricLinesDrawer.draw(bufferGraphics, time);
		guitarDrawer.drawGuitar(bufferGraphics, time, highlightData);
		guitarDrawer.drawStringNames(bufferGraphics);

		final Composite previousComposite = ((Graphics2D) g).getComposite();
		((Graphics2D) g)
				.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, tempoMapGhostNotesTransparency));
		g.drawImage(image, 0, 0, null);
		((Graphics2D) g).setComposite(previousComposite);
	}

	public void draw(final Graphics g, final int time, final HighlightData highlightData) {
		waveFormDrawer.draw(g, time);
		beatsDrawer.draw(g, time, highlightData);
		drawGhostNotes(g, time, highlightData);
	}
}
