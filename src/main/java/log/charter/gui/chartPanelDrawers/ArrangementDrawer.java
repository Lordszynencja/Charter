package log.charter.gui.chartPanelDrawers;

import java.awt.Graphics2D;

import log.charter.data.ChartData;
import log.charter.data.managers.HighlightManager;
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
import log.charter.gui.handlers.mouseAndKeyboard.MouseButtonPressReleaseHandler;
import log.charter.gui.handlers.mouseAndKeyboard.MouseHandler;

public class ArrangementDrawer {
	private ChartData chartData;
	private HighlightManager highlightManager;
	private ModeManager modeManager;
	private MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler;
	private MouseHandler mouseHandler;
	private SelectionManager selectionManager;

	private final LyricLinesDrawer lyricLinesDrawer = new LyricLinesDrawer();

	private final TempoMapDrawer tempoMapDrawer = new TempoMapDrawer();
	private final GuitarDrawer guitarDrawer = new GuitarDrawer();
	private final VocalsDrawer vocalsDrawer = new VocalsDrawer();

	public void init(final BeatsDrawer beatsDrawer, final ChartData chartData, final ChartPanel chartPanel,
			final HighlightManager highlightManager, final KeyboardHandler keyboardHandler,
			final ModeManager modeManager, final MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler,
			final MouseHandler mouseHandler, final SelectionManager selectionManager,
			final WaveFormDrawer waveFormDrawer) {
		this.chartData = chartData;
		this.highlightManager = highlightManager;
		this.modeManager = modeManager;
		this.mouseButtonPressReleaseHandler = mouseButtonPressReleaseHandler;
		this.mouseHandler = mouseHandler;
		this.selectionManager = selectionManager;

		lyricLinesDrawer.init(chartData, modeManager);

		tempoMapDrawer.init(beatsDrawer, chartData, guitarDrawer, lyricLinesDrawer, waveFormDrawer);
		guitarDrawer.init(beatsDrawer, chartPanel, keyboardHandler, lyricLinesDrawer, selectionManager, waveFormDrawer);
		vocalsDrawer.init(beatsDrawer, chartData, chartPanel, lyricLinesDrawer, selectionManager, waveFormDrawer);
	}

	private HighlightData generateHighlightData(final int time) {
		return HighlightData.getCurrentHighlight(time, chartData, highlightManager, modeManager,
				mouseButtonPressReleaseHandler, mouseHandler, selectionManager);
	}

	private void drawGuitar(final Graphics2D g, final int time) {
		guitarDrawer.draw(g, chartData.getCurrentArrangement(), chartData.getCurrentArrangementLevel(), time,
				generateHighlightData(time));
	}

	public void draw(final Graphics2D g, final int time) {
		switch (modeManager.getMode()) {
			case TEMPO_MAP -> tempoMapDrawer.draw(g, time, generateHighlightData(time));
			case VOCALS -> vocalsDrawer.draw(g, time, generateHighlightData(time));
			case GUITAR -> drawGuitar(g, time);
			default -> {}
		}
	}

}
