package log.charter.gui.chartPanelDrawers.instruments.guitar.theme.modern;

import static log.charter.data.config.Config.showChordIds;
import static log.charter.data.config.GraphicalConfig.handShapesHeight;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesBottom;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesTop;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.filledRectangle;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.strokedRectangle;

import java.awt.*;

import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapePositionWithSize;
import log.charter.gui.chartPanelDrawers.drawableShapes.Text;
import log.charter.gui.chartPanelDrawers.instruments.guitar.theme.HighwayDrawerData;
import log.charter.gui.chartPanelDrawers.instruments.guitar.theme.ThemeHandShapes;
import log.charter.song.ChordTemplate;
import log.charter.song.HandShape;
import log.charter.util.Position2D;

public class ModernThemeHandShapes implements ThemeHandShapes {
	private static Font handShapesFont = new Font(Font.SANS_SERIF, Font.BOLD, handShapesHeight);

	public static void reloadGraphics() {
		handShapesFont = new Font(Font.SANS_SERIF, Font.BOLD, handShapesHeight); }

	private final HighwayDrawerData data;

	public ModernThemeHandShapes(final HighwayDrawerData data) {
		this.data = data;
	}

	@Override
	public void addHandShape(final int x, final int length, final boolean selected, final boolean highlighted,
			final HandShape handShape, final ChordTemplate chordTemplate) {
		final ShapePositionWithSize positionTop = new ShapePositionWithSize(x, lanesTop, length, lanesBottom - lanesTop);
		final Color fillColorAlpha = chordTemplate.arpeggio
				? ColorLabel.HAND_SHAPE_ARPEGGIO.colorWithAlpha(32)
				: ColorLabel.HAND_SHAPE.colorWithAlpha(32);

		data.handShapes.add(filledRectangle(positionTop, fillColorAlpha));

		final ShapePositionWithSize position = new ShapePositionWithSize(x, lanesBottom + 1, length, handShapesHeight);
		final ColorLabel fillColor = chordTemplate.arpeggio ? ColorLabel.HAND_SHAPE_ARPEGGIO : ColorLabel.HAND_SHAPE;
		data.handShapes.add(filledRectangle(position, fillColor));

		if (highlighted) {
			data.handShapes.add(strokedRectangle(position.resized(-1, -1, 1, 1), ColorLabel.HIGHLIGHT));
		} else if (selected) {
			data.handShapes.add(strokedRectangle(position.resized(-1, -1, 1, 1), ColorLabel.SELECT));
		}

		String chordName = chordTemplate.chordName;
		if (showChordIds) {
			chordName = (chordName == null || chordName.isBlank()) ? "[" + handShape.templateId + "]"
					: chordName + " [" + handShape.templateId + "]";
		}
		if (chordName != null) {
			data.handShapes.add(new Text(new Position2D(x + 2, lanesBottom + 1), handShapesFont, chordName,
					ColorLabel.BASE_TEXT));
		}
	}

	@Override
	public void addHandShapeHighlight(final int x, final int length) {
		final ShapePositionWithSize position = new ShapePositionWithSize(x, lanesBottom + 1, length - 1,
				handShapesHeight); // changed
		data.handShapes.add(strokedRectangle(position, ColorLabel.HIGHLIGHT));
	}
}
