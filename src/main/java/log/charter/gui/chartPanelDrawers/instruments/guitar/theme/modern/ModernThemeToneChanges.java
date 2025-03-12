package log.charter.gui.chartPanelDrawers.instruments.guitar.theme.modern;

import static java.lang.Math.min;
import static log.charter.data.config.GraphicalConfig.toneChangeHeight;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesBottom;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.toneChangeY;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.lineVertical;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.strokedRectangle;

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
	private static final int toneChangeSpace = 2;

	private static Font toneChangeFont = new Font(Font.SANS_SERIF, Font.BOLD, toneChangeHeight);

	public static void reloadSizes() {
		toneChangeFont = new Font(Font.SANS_SERIF, Font.BOLD, toneChangeHeight);
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
		return new TextWithBackground(new Position2D(x, toneChangeY + 3), toneChangeFont, cleanTone(tone),
				ColorLabel.TONE_CHANGE_TEXT, ColorLabel.TONE_CHANGE, toneChangeSpace, ColorLabel.BASE_BORDER);
	}

	@Override
	public void addCurrentTone(final Graphics2D g, final String tone, final int nextToneChangeX) {
		if (nextToneChangeX <= 0) {
			return;
		}

		final ShapeSize expectedSize = TextWithBackground.getExpectedSize(g, toneChangeFont, cleanTone(tone),
				toneChangeSpace);
		final int x = min(0, nextToneChangeX - expectedSize.width);

		data.sectionsAndPhrases.add(generateText(tone, x));
	}

	@Override
	public void addCurrentTone(final Graphics2D g, final String tone) {
		data.sectionsAndPhrases.add(generateText(tone, 0));
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
		data.toneChanges.add(lineVertical(x, toneChangeY + 5, lanesBottom, ColorLabel.TONE_CHANGE));
		data.toneChanges.add(generateText(toneChange.toneName, x));

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
