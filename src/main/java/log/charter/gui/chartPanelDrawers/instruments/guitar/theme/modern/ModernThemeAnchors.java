package log.charter.gui.chartPanelDrawers.instruments.guitar.theme.modern;

import static java.lang.Math.min;
import static log.charter.data.config.GraphicalConfig.anchorInfoHeight;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.anchorY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesBottom;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.lineVertical;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.strokedRectangle;

import java.awt.Font;
import java.awt.Graphics2D;

import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapePositionWithSize;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapeSize;
import log.charter.gui.chartPanelDrawers.drawableShapes.Text;
import log.charter.gui.chartPanelDrawers.instruments.guitar.theme.HighwayDrawData;
import log.charter.gui.chartPanelDrawers.instruments.guitar.theme.ThemeAnchors;
import log.charter.song.Anchor;
import log.charter.util.Position2D;

public class ModernThemeAnchors implements ThemeAnchors {
	private static Font anchorFont = new Font(Font.SANS_SERIF, Font.BOLD, anchorInfoHeight);

	public static void reloadGraphics() {
		anchorFont = new Font(Font.SANS_SERIF, Font.BOLD, anchorInfoHeight);
	}

	private final HighwayDrawData data;

	public ModernThemeAnchors(final HighwayDrawData highwayDrawerData) {
		data = highwayDrawerData;
	}

	private String getLabel(final Anchor anchor) {
		return anchor.width == 4 ? anchor.fret + "" : anchor.fret + " - " + anchor.topFret();
	}

	private Text generateText(final String label, final int x) {
		return new Text(new Position2D(x + 4, anchorY + 1), anchorFont, label, ColorLabel.ANCHOR);
	}

	private void addAnchor(final String label, final int x) {
		data.anchors.add(lineVertical(x, anchorY, lanesBottom, ColorLabel.ANCHOR));
		data.anchors.add(generateText(label, x));
	}

	@Override
	public void addCurrentAnchor(final Graphics2D g, final Anchor anchor) {
		addAnchor(getLabel(anchor), 0);
	}

	@Override
	public void addCurrentAnchor(final Graphics2D g, final Anchor anchor, final int nextAnchorX) {
		final String label = getLabel(anchor);
		final ShapeSize expectedSize = Text.getExpectedSize(g, anchorFont, label);
		final int x = min(0, nextAnchorX - 4 - expectedSize.width);

		addAnchor(label, x);
	}

	private void addAnchorBox(final int x, final ColorLabel color) {
		final int top = anchorY - 1;
		final int bottom = lanesBottom + 1;
		final ShapePositionWithSize anchorPosition = new ShapePositionWithSize(x - 1, top, 2, bottom - top);
		data.anchors.add(strokedRectangle(anchorPosition, color));
	}

	@Override
	public void addAnchor(final Anchor anchor, final int x, final boolean selected, final boolean highlighted) {
		addAnchor(getLabel(anchor), x);

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
