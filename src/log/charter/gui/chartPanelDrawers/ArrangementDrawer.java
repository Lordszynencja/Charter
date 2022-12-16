package log.charter.gui.chartPanelDrawers;

import java.awt.Graphics;

import log.charter.data.ChartData;
import log.charter.data.EditMode;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.gui.ChartPanel;
import log.charter.gui.chartPanelDrawers.common.AudioDrawer;
import log.charter.gui.chartPanelDrawers.common.BeatsDrawer;
import log.charter.gui.chartPanelDrawers.instruments.GuitarDrawer;
import log.charter.gui.chartPanelDrawers.instruments.VocalsDrawer;
import log.charter.gui.handlers.KeyboardHandler;

public class ArrangementDrawer {
	private ModeManager modeManager;

	private final GuitarDrawer guitarDrawer = new GuitarDrawer();
	private final VocalsDrawer vocalsDrawer = new VocalsDrawer();

	public void init(final AudioDrawer audioDrawer, final BeatsDrawer beatsDrawer, final ChartPanel chartPanel,
			final ChartData data, final KeyboardHandler keyboardHandler, final ModeManager modeManager,
			final SelectionManager selectionManager) {
		this.modeManager = modeManager;

		guitarDrawer.init(audioDrawer, beatsDrawer, data, chartPanel, keyboardHandler, selectionManager);
		vocalsDrawer.init(audioDrawer, beatsDrawer, data, chartPanel, selectionManager);
	}

	public void draw(final Graphics g) {
		if (modeManager.editMode == EditMode.GUITAR) {
			guitarDrawer.draw(g);
		} else if (modeManager.editMode == EditMode.VOCALS) {
			vocalsDrawer.draw(g);
		}
	}

}
