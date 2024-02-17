package log.charter.gui.chartPanelDrawers.instruments.guitar.theme.modern;

import static log.charter.data.config.GraphicalConfig.anchorInfoHeight;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.anchorY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesBottom;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.lineVertical;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.strokedRectangle;

import java.awt.Font;

import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapePositionWithSize;
import log.charter.gui.chartPanelDrawers.drawableShapes.Text;
import log.charter.gui.chartPanelDrawers.instruments.guitar.theme.HighwayDrawerData;
import log.charter.gui.chartPanelDrawers.instruments.guitar.theme.ThemeAnchors;
import log.charter.song.Anchor;
import log.charter.util.Position2D;

public class ModernThemeAnchors implements ThemeAnchors {
	private static Font anchorFont = new Font(Font.SANS_SERIF, Font.BOLD, anchorInfoHeight - 2);

	public static void reloadGraphics() {
		anchorFont = new Font(Font.SANS_SERIF, Font.BOLD, anchorInfoHeight - 2);
	}

	private final HighwayDrawerData data;

	public ModernThemeAnchors(final HighwayDrawerData highwayDrawerData) {
		data = highwayDrawerData;
	}

	private void addAnchorLine(final int x) {
		data.anchors.add(lineVertical(x, anchorY, lanesBottom, ColorLabel.ANCHOR));
	}

	private void addAnchorText(final Anchor anchor, final int x) {
		final String anchorText = anchor.width == 4 ? anchor.fret + "" : anchor.fret + " - " + anchor.topFret();
		data.anchors.add(new Text(new Position2D(x + 4, anchorY + 1), anchorFont, anchorText, ColorLabel.ANCHOR));
	}

	private void addAnchorBox(final int x, final ColorLabel color) {
		final int top = anchorY - 1;
		final int bottom = lanesBottom + 1;
		final ShapePositionWithSize anchorPosition = new ShapePositionWithSize(x - 1, top, 2, bottom - top);
		data.anchors.add(strokedRectangle(anchorPosition, color));
	}

	@Override
	public void addAnchor(final Anchor anchor, final int x, final boolean selected, final boolean highlighted) {
		addAnchorLine(x);
		addAnchorText(anchor, x);

		if (highlighted) {
			addAnchorBox(x, ColorLabel.HIGHLIGHT);
		} else if (selected) {
			addAnchorBox(x, ColorLabel.SELECT);
		}
	}

	@Override
	public void addAnchorHighlight(final int x) {
		data.anchors.add(lineVertical(x, anchorY, lanesBottom, ColorLabel.HIGHLIGHT));
	}
}
