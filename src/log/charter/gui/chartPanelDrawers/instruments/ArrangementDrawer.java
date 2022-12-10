package log.charter.gui.chartPanelDrawers.instruments;

import java.awt.Graphics;

import log.charter.data.ChartData;
import log.charter.data.EditMode;
import log.charter.gui.ChartPanel;
import log.charter.gui.chartPanelDrawers.Drawer;

public class ArrangementDrawer implements Drawer {

	private final GuitarDrawer guitarDrawer = new GuitarDrawer();
	private final VocalsDrawer vocalsDrawer = new VocalsDrawer();

	@Override
	public void draw(final Graphics g, final ChartPanel panel, final ChartData data) {
		if (data.isEmpty) {
			return;
		}

		if (data.editMode == EditMode.GUITAR) {
			guitarDrawer.draw(g, panel, data);
		} else if (data.editMode == EditMode.VOCALS) {
			vocalsDrawer.draw(g, panel, data);
		}
	}

}
