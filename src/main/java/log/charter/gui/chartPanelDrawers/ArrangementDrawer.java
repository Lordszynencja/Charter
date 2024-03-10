package log.charter.gui.chartPanelDrawers;

import java.awt.Graphics2D;

import log.charter.data.ChartData;
import log.charter.gui.chartPanelDrawers.common.LyricLinesDrawer;
import log.charter.gui.chartPanelDrawers.data.HighlightData;
import log.charter.gui.chartPanelDrawers.instruments.TempoMapDrawer;
import log.charter.gui.chartPanelDrawers.instruments.VocalsDrawer;
import log.charter.gui.chartPanelDrawers.instruments.guitar.GuitarDrawer;
import log.charter.services.CharterContext;
import log.charter.services.CharterContext.Initiable;
import log.charter.services.data.selection.SelectionManager;
import log.charter.services.editModes.ModeManager;
import log.charter.services.mouseAndKeyboard.HighlightManager;
import log.charter.services.mouseAndKeyboard.MouseButtonPressReleaseHandler;
import log.charter.services.mouseAndKeyboard.MouseHandler;

public class ArrangementDrawer implements Initiable {
	private CharterContext charterContext;
	private ChartData chartData;
	private HighlightManager highlightManager;
	private ModeManager modeManager;
	private MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler;
	private MouseHandler mouseHandler;
	private SelectionManager selectionManager;

	private final GuitarDrawer guitarDrawer = new GuitarDrawer();
	private final LyricLinesDrawer lyricLinesDrawer = new LyricLinesDrawer();
	private final TempoMapDrawer tempoMapDrawer = new TempoMapDrawer();
	private final VocalsDrawer vocalsDrawer = new VocalsDrawer();

	@Override
	public void init() {
		charterContext.initObject(guitarDrawer);
		guitarDrawer.lyricLinesDrawer(lyricLinesDrawer);
		charterContext.initObject(lyricLinesDrawer);
		charterContext.initObject(tempoMapDrawer);
		tempoMapDrawer.guitarDrawer(guitarDrawer);
		tempoMapDrawer.lyricLinesDrawer(lyricLinesDrawer);
		charterContext.initObject(vocalsDrawer);
		vocalsDrawer.lyricLinesDrawer(lyricLinesDrawer);
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
