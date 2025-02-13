package log.charter.gui.chartPanelDrawers.instruments.guitar.theme.modern;

import static java.lang.Math.min;
import static log.charter.data.config.GraphicalConfig.fhpInfoHeight;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.fhpY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesBottom;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.lineVertical;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.strokedRectangle;

import java.awt.Font;
import java.awt.Graphics2D;

import log.charter.data.song.FHP;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapePositionWithSize;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapeSize;
import log.charter.gui.chartPanelDrawers.drawableShapes.Text;
import log.charter.gui.chartPanelDrawers.instruments.guitar.theme.HighwayDrawData;
import log.charter.gui.chartPanelDrawers.instruments.guitar.theme.ThemeFHPs;
import log.charter.util.data.Position2D;

public class ModernThemeFHPs implements ThemeFHPs {
	private static Font fhpFont = new Font(Font.SANS_SERIF, Font.BOLD, fhpInfoHeight);

	public static void reloadGraphics() {
		fhpFont = new Font(Font.SANS_SERIF, Font.BOLD, fhpInfoHeight);
	}

	private final HighwayDrawData data;

	public ModernThemeFHPs(final HighwayDrawData highwayDrawerData) {
		data = highwayDrawerData;
	}

	private String getLabel(final FHP fhp) {
		return fhp.width == 4 ? fhp.fret + "" : fhp.fret + " - " + fhp.topFret();
	}

	private Text generateText(final String label, final int x) {
		return new Text(new Position2D(x + 4, fhpY + 1), fhpFont, label, ColorLabel.FHP);
	}

	private void addFHP(final String label, final int x) {
		data.fhps.add(lineVertical(x, fhpY, lanesBottom, ColorLabel.FHP));
		data.fhps.add(generateText(label, x));
	}

	@Override
	public void addCurrentFHP(final Graphics2D g, final FHP fhp) {
		addFHP(getLabel(fhp), 0);
	}

	@Override
	public void addCurrentFHP(final Graphics2D g, final FHP fhp, final int nextFHPX) {
		final String label = getLabel(fhp);
		final ShapeSize expectedSize = Text.getExpectedSize(g, fhpFont, label);
		final int x = min(0, nextFHPX - 4 - expectedSize.width);

		addFHP(label, x);
	}

	private void addFHPBox(final int x, final ColorLabel color) {
		final int top = fhpY - 1;
		final int bottom = lanesBottom + 1;
		final ShapePositionWithSize fhpPosition = new ShapePositionWithSize(x - 1, top, 2, bottom - top);
		data.fhps.add(strokedRectangle(fhpPosition, color));
	}

	@Override
	public void addFHP(final FHP fhp, final int x, final boolean selected, final boolean highlighted) {
		addFHP(getLabel(fhp), x);

		if (highlighted) {
			addFHPBox(x, ColorLabel.HIGHLIGHT);
		} else if (selected) {
			addFHPBox(x, ColorLabel.SELECT);
		}
	}

	@Override
	public void addFHPHighlight(final int x) {
		data.fhps.add(lineVertical(x, fhpY, lanesBottom, ColorLabel.HIGHLIGHT));
	}

}
