package log.charter.gui.chartPanelDrawers.instruments;

import static log.charter.data.config.GraphicalConfig.tempoMapGhostNotesTransparency;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import log.charter.gui.chartPanelDrawers.common.BeatsDrawer;
import log.charter.gui.chartPanelDrawers.common.LyricLinesDrawer;
import log.charter.gui.chartPanelDrawers.common.waveform.WaveFormDrawer;
import log.charter.gui.chartPanelDrawers.data.FrameData;
import log.charter.gui.chartPanelDrawers.instruments.guitar.GuitarDrawer;

public class TempoMapDrawer {
	private BeatsDrawer beatsDrawer;
	private GuitarDrawer guitarDrawer;
	private LyricLinesDrawer lyricLinesDrawer;
	private WaveFormDrawer waveFormDrawer;

	public void guitarDrawer(final GuitarDrawer guitarDrawer) {
		this.guitarDrawer = guitarDrawer;
	}

	public void lyricLinesDrawer(final LyricLinesDrawer lyricLinesDrawer) {
		this.lyricLinesDrawer = lyricLinesDrawer;
	}

	private void drawWithAlpha(final Graphics2D g, final BufferedImage image, final float alpha) {
		final Composite previousComposite = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
		g.drawImage(image, 0, 0, null);
		g.setComposite(previousComposite);
	}

	private void drawGhostNotes(final FrameData frameData) {
		if (tempoMapGhostNotesTransparency <= 0) {
			return;
		}

		final Rectangle bounds = frameData.g.getClipBounds();
		final BufferedImage image = new BufferedImage((int) bounds.getWidth(), (int) bounds.getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		final FrameData subFrameData = frameData.spawnSubData(image.createGraphics());

		lyricLinesDrawer.draw(subFrameData);
		guitarDrawer.drawGuitar(subFrameData);
		guitarDrawer.drawStringNames(subFrameData);

		drawWithAlpha(frameData.g, image, tempoMapGhostNotesTransparency);
	}

	public void draw(final FrameData frameData) {
		waveFormDrawer.draw(frameData);
		beatsDrawer.draw(frameData);
		drawGhostNotes(frameData);
	}
}
