package log.charter.gui.chartPanelDrawers.instruments;

import java.awt.Graphics;

import log.charter.data.ChartData;
import log.charter.data.EditMode;
import log.charter.gui.ChartPanel;
import log.charter.gui.SelectionManager;
import log.charter.gui.chartPanelDrawers.common.HighlightDrawer;

public class ArrangementDrawer {

	private ChartData data;

	private final GuitarDrawer guitarDrawer = new GuitarDrawer();
	private final VocalsDrawer vocalsDrawer = new VocalsDrawer();

	public void init(final ChartPanel chartPanel, final ChartData data, final HighlightDrawer highlightDrawer,
			final SelectionManager selectionManager) {
		this.data = data;
		guitarDrawer.init(data, chartPanel);
		vocalsDrawer.init(data, chartPanel, highlightDrawer, selectionManager);
	}

	public void draw(final Graphics g) {
		if (data.isEmpty) {
			return;
		}

		if (data.editMode == EditMode.GUITAR) {
			guitarDrawer.draw(g);
		} else if (data.editMode == EditMode.VOCALS) {
			vocalsDrawer.draw(g);
		}
	}

}
