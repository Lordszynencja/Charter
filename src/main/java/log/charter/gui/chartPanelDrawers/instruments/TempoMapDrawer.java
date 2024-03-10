package log.charter.gui.chartPanelDrawers.instruments;

import static log.charter.data.config.GraphicalConfig.tempoMapGhostNotesTransparency;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import log.charter.data.ChartData;
import log.charter.data.song.Arrangement;
import log.charter.data.song.Level;
import log.charter.gui.chartPanelDrawers.common.BeatsDrawer;
import log.charter.gui.chartPanelDrawers.common.LyricLinesDrawer;
import log.charter.gui.chartPanelDrawers.common.waveform.WaveFormDrawer;
import log.charter.gui.chartPanelDrawers.data.HighlightData;
import log.charter.gui.chartPanelDrawers.instruments.guitar.GuitarDrawer;

public class TempoMapDrawer {
	private BeatsDrawer beatsDrawer;
	private ChartData chartData;
	private GuitarDrawer guitarDrawer;
	private LyricLinesDrawer lyricLinesDrawer;
	private WaveFormDrawer waveFormDrawer;

	public void guitarDrawer(final GuitarDrawer guitarDrawer) {
		this.guitarDrawer = guitarDrawer;
	}

	public void lyricLinesDrawer(final LyricLinesDrawer lyricLinesDrawer) {
		this.lyricLinesDrawer = lyricLinesDrawer;
	}

	private void drawGhostNotes(final Graphics2D g, final int time, final HighlightData highlightData) {
		if (tempoMapGhostNotesTransparency <= 0) {
			return;
		}

		final Rectangle bounds = g.getClipBounds();
		final BufferedImage image = new BufferedImage((int) bounds.getWidth(), (int) bounds.getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		final Graphics2D bufferGraphics = image.createGraphics();

		final Arrangement arrangement = chartData.getCurrentArrangement();
		final Level level = chartData.getCurrentArrangementLevel();
		lyricLinesDrawer.draw(bufferGraphics, time);
		guitarDrawer.drawGuitar(bufferGraphics, arrangement, level, time, highlightData);
		guitarDrawer.drawStringNames(bufferGraphics, arrangement);

		final Composite previousComposite = ((Graphics2D) g).getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, tempoMapGhostNotesTransparency));
		g.drawImage(image, 0, 0, null);
		g.setComposite(previousComposite);
	}

	public void draw(final Graphics2D g, final int time, final HighlightData highlightData) {
		waveFormDrawer.draw(g, time);
		beatsDrawer.draw(g, time, highlightData);
		drawGhostNotes(g, time, highlightData);
	}
}
