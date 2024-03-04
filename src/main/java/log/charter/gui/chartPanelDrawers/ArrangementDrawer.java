package log.charter.gui.chartPanelDrawers;

import java.awt.Graphics;

import log.charter.data.ChartData;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.gui.ChartPanel;
import log.charter.gui.chartPanelDrawers.common.BeatsDrawer;
import log.charter.gui.chartPanelDrawers.common.LyricLinesDrawer;
import log.charter.gui.chartPanelDrawers.common.waveform.WaveFormDrawer;
import log.charter.gui.chartPanelDrawers.data.HighlightData;
import log.charter.gui.chartPanelDrawers.instruments.TempoMapDrawer;
import log.charter.gui.chartPanelDrawers.instruments.VocalsDrawer;
import log.charter.gui.chartPanelDrawers.instruments.guitar.GuitarDrawer;
import log.charter.gui.handlers.mouseAndKeyboard.KeyboardHandler;

public class ArrangementDrawer {
	private ModeManager modeManager;

	private final TempoMapDrawer tempoMapDrawer = new TempoMapDrawer();
	private final GuitarDrawer guitarDrawer = new GuitarDrawer();
	private final VocalsDrawer vocalsDrawer = new VocalsDrawer();

	public void init(final BeatsDrawer beatsDrawer, final ChartPanel chartPanel, final ChartData data,
			final KeyboardHandler keyboardHandler, final LyricLinesDrawer lyricLinesDrawer,
			final ModeManager modeManager, final SelectionManager selectionManager,
			final WaveFormDrawer waveFormDrawer) {
		this.modeManager = modeManager;

		tempoMapDrawer.init(guitarDrawer);
		guitarDrawer.init(beatsDrawer, data, chartPanel, keyboardHandler, lyricLinesDrawer, selectionManager,
				waveFormDrawer);
		vocalsDrawer.init(beatsDrawer, data, chartPanel, lyricLinesDrawer, selectionManager, waveFormDrawer);
	}

	public void draw(final Graphics g, final int time, final HighlightData highlightData) {
		switch (modeManager.getMode()) {
			case GUITAR:
				guitarDrawer.draw(g, time, highlightData);
				break;
			case TEMPO_MAP:
				tempoMapDrawer.draw(g, time, highlightData);
				break;
			case VOCALS:
				vocalsDrawer.draw(g, time, highlightData);
				break;
			default:
				break;
		}
	}

}
