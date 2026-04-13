package log.charter.gui.chartPanelDrawers.instruments.guitar.theme.modern;

import static log.charter.data.config.GraphicalConfig.chartTextHeight;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesBottom;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.toneChangeY;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.lineVertical;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.strokedRectangle;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.strokedRoundRectangle;

import java.awt.Font;
import java.awt.Graphics2D;

import log.charter.data.config.ChartPanelColors.ColorLabel;
import log.charter.data.song.ToneChange;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapePositionWithSize;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapeSize;
import log.charter.gui.chartPanelDrawers.drawableShapes.TextWithBackground;
import log.charter.gui.chartPanelDrawers.instruments.guitar.theme.HighwayDrawData;
import log.charter.gui.chartPanelDrawers.instruments.guitar.theme.ThemeToneChanges;
import log.charter.util.data.Position2D;

public class ModernThemeToneChanges implements ThemeToneChanges {
	private static int toneChangeSpace;
	private static int arcSize;
	private static Font toneChangeFont;

	public static void reloadSizes() {
		toneChangeSpace = chartTextHeight / 5;
		arcSize = chartTextHeight / 2;
		toneChangeFont = new Font(Font.SANS_SERIF, Font.BOLD, chartTextHeight);
	}

	private final HighwayDrawData data;

	public ModernThemeToneChanges(final HighwayDrawData highwayDrawerData) {
		data = highwayDrawerData;
	}

	private String cleanTone(final String tone) {
		if (tone == null || tone.length() < 1) {
			return "none";
		}

		return tone;
	}

	private TextWithBackground generateText(final String tone, final int x) {
		return new TextWithBackground()//
				.position(new Position2D(x, toneChangeY + toneChangeSpace))//
				.font(toneChangeFont)//
				.text(cleanTone(tone))//
				.color(ColorLabel.TONE_CHANGE_TEXT)//
				.backgroundColor(ColorLabel.TONE_CHANGE)//
				.space(toneChangeSpace)//
				.arcSize(arcSize);
	}

	@Override
	public ShapeSize getSizeOfTone(final Graphics2D g, final String tone) {
		final String label = cleanTone(tone);
		return TextWithBackground.getExpectedSize(g, toneChangeFont, label, toneChangeSpace);
	}

	@Override
	public void addTone(final Graphics2D g, final String tone, final int x, final boolean highlight) {
		final String label = cleanTone(tone);

		final TextWithBackground labelToAdd = generateText(label, x);
		data.toneChanges.add(labelToAdd);

		if (highlight) {
			data.toneChanges.add(strokedRoundRectangle(labelToAdd.getPositionWithSize(g), ColorLabel.HIGHLIGHT.color(),
					toneChangeSpace, arcSize));
		}
	}

	private void addToneChangeBox(final int x, final ColorLabel color) {
		final int top = toneChangeY + chartTextHeight;
		final int bottom = lanesBottom + 1;
		final ShapePositionWithSize toneChangePosition = new ShapePositionWithSize(x - 1, top, 2, bottom - top);
		data.toneChanges.add(strokedRectangle(toneChangePosition, color));
	}

	@Override
	public void addToneChange(final Graphics2D g, final ToneChange toneChange, final int x, final boolean selected,
			final boolean highlighted) {
		data.toneChanges.add(lineVertical(x, toneChangeY + chartTextHeight, lanesBottom, ColorLabel.TONE_CHANGE));
		addTone(g, toneChange.toneName, x, highlighted);

		if (highlighted) {
			addToneChangeBox(x, ColorLabel.HIGHLIGHT);
		} else if (selected) {
			addToneChangeBox(x, ColorLabel.SELECT);
		}
	}

	@Override
	public void addToneChangeHighlight(final int x) {
		data.fhps.add(lineVertical(x, toneChangeY, lanesBottom, ColorLabel.HIGHLIGHT));
	}

}
