package log.charter.gui.chartPanelDrawers.instruments.guitar.theme.modern;

import static log.charter.data.config.GraphicalConfig.toneChangeHeight;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesBottom;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.toneChangeY;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.lineVertical;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.strokedRectangle;

import java.awt.Font;

import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapePositionWithSize;
import log.charter.gui.chartPanelDrawers.drawableShapes.TextWithBackground;
import log.charter.gui.chartPanelDrawers.instruments.guitar.theme.HighwayDrawerData;
import log.charter.gui.chartPanelDrawers.instruments.guitar.theme.ThemeToneChanges;
import log.charter.song.ToneChange;
import log.charter.util.Position2D;

public class ModernThemeToneChanges implements ThemeToneChanges {

	private static Font toneChangeFont = new Font(Font.SANS_SERIF, Font.BOLD, toneChangeHeight);

	public static void reloadSizes() {
		toneChangeFont = new Font(Font.SANS_SERIF, Font.BOLD, toneChangeHeight);
	}

	private final HighwayDrawerData data;

	public ModernThemeToneChanges(final HighwayDrawerData highwayDrawerData) {
		data = highwayDrawerData;
	}

	private void addToneChangeBox(final int x, final ColorLabel color) {
		final int top = toneChangeY - 1;
		final int bottom = lanesBottom + 1;
		final ShapePositionWithSize toneChangePosition = new ShapePositionWithSize(x - 1, top, 2, bottom - top);
		data.toneChanges.add(strokedRectangle(toneChangePosition, color));
	}

	@Override
	public void addToneChange(final ToneChange toneChange, final int x, final boolean selected,
			final boolean highlighted) {
		data.toneChanges.add(lineVertical(x, toneChangeY, lanesBottom, ColorLabel.TONE_CHANGE));
		data.toneChanges.add(new TextWithBackground(new Position2D(x, toneChangeY), toneChangeFont,
				"" + toneChange.toneName, ColorLabel.TONE_CHANGE_TEXT, ColorLabel.TONE_CHANGE, 2, ColorLabel.BASE_BORDER.color()));

		if (highlighted) {
			addToneChangeBox(x, ColorLabel.HIGHLIGHT);
		} else if (selected) {
			addToneChangeBox(x, ColorLabel.SELECT);
		}
	}

	@Override
	public void addToneChangeHighlight(final int x) {
		data.anchors.add(lineVertical(x, toneChangeY, lanesBottom, ColorLabel.HIGHLIGHT));
	}
}
