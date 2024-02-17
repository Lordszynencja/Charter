package log.charter.gui.chartPanelDrawers.instruments.guitar.theme.modern;

import static java.lang.Math.round;
import static log.charter.data.config.Config.maxBendValue;
import static log.charter.data.config.GraphicalConfig.noteHeight;
import static log.charter.gui.ChartPanelColors.getStringBasedColor;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.tailHeight;
import static log.charter.gui.chartPanelDrawers.drawableShapes.CenteredTextWithBackgroundAndBorder.getExpectedSize;
import static log.charter.util.ScalingUtils.timeToX;
import static log.charter.util.ScalingUtils.xToTimeLength;

import java.awt.Color;
import java.awt.Font;
import java.math.BigDecimal;
import java.math.RoundingMode;

import log.charter.gui.ChartPanelColors.StringColorLabelType;
import log.charter.gui.chartPanelDrawers.data.EditorNoteDrawingData;
import log.charter.gui.chartPanelDrawers.drawableShapes.CenteredTextWithBackground;
import log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape;
import log.charter.gui.chartPanelDrawers.drawableShapes.Line;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapeSize;
import log.charter.gui.chartPanelDrawers.instruments.guitar.theme.HighwayDrawerData;
import log.charter.song.BendValue;
import log.charter.util.Position2D;

public class ModernThemeBends {
	private static Font bendValueFont = new Font(Font.DIALOG, Font.BOLD, noteHeight / 4);

	public static void reloadGraphics() {
		bendValueFont = new Font(Font.DIALOG, Font.PLAIN, noteHeight / 4);
	}

	private static String formatBendValue(final BigDecimal bendValue) {
		if (bendValue == null) {
			return "0";
		}

		final int value = (int) round(bendValue.doubleValue() * 4);
		final int fullSteps = value / 4;
		final int quarterSteps = value % 4;

		if (fullSteps == 0) {
			if (quarterSteps == 0) {
				return "0";
			}
			if (quarterSteps == 1) {
				return "¼";
			}
			if (quarterSteps == 2) {
				return "½";
			}
			if (quarterSteps == 3) {
				return "¾";
			}
		}

		String text = fullSteps + "";
		if (quarterSteps == 1) {
			text += " ¼";
		}
		if (quarterSteps == 2) {
			text += " ½";
		}
		if (quarterSteps == 3) {
			text += " ¾";
		}

		return text;
	}

	private static int getBendLineY(final int y, BigDecimal bendValue) {
		if (bendValue == null) {
			bendValue = BigDecimal.ZERO;
		}
		if (bendValue.compareTo(new BigDecimal(maxBendValue)) > 0) {
			bendValue = new BigDecimal(maxBendValue);
		}

		final int bendOffset = bendValue.multiply(new BigDecimal(tailHeight * 2 / 3))
				.divide(new BigDecimal(maxBendValue), RoundingMode.HALF_UP).intValue();
		return y + tailHeight / 3 - bendOffset;
	}

	private final HighwayDrawerData data;

	public ModernThemeBends(final HighwayDrawerData highwayDrawerData) {
		this.data = highwayDrawerData;
	}

	private void addBendValueIcon(final EditorNoteDrawingData note, final int position, final int x, final int y,
			final BigDecimal bendValue) {
		if (note.linkPrevious) {
			return;
		}

		final String text = "ノ" + formatBendValue(bendValue);
		final ShapeSize expectedIconSize = getExpectedSize(data.g, bendValueFont, text);
		final int minBendPositionAfterHead = xToTimeLength(noteHeight / 2 + expectedIconSize.width / 2);

		Position2D iconPosition;
		if (position > minBendPositionAfterHead) {
			iconPosition = new Position2D(x, y - tailHeight / 2);
		} else {
			final int bendY = y - noteHeight / 2 - expectedIconSize.height / 2;
			iconPosition = new Position2D(x, bendY);
		}

		final Color backgroundColor = getStringBasedColor(StringColorLabelType.LANE, note.string,
				data.strings);
		final DrawableShape bendValueIcon = new CenteredTextWithBackground(iconPosition, bendValueFont, text,
				Color.WHITE, backgroundColor);
		data.bendValues.add(bendValueIcon);
	}

	public void addBendValues(final EditorNoteDrawingData note, final int y) {
		if (note.bendValues == null || note.bendValues.isEmpty()) {
			return;
		}

		boolean linesDrawn = false;
		Position2D lastBendLinePosition = new Position2D(note.x, getBendLineY(y, BigDecimal.ZERO));
		for (final BendValue bendValue : note.bendValues) {
			final int x = timeToX(note.position + bendValue.position(), data.time);

			final Position2D lineTo = new Position2D(x, getBendLineY(y, bendValue.bendValue));
			if (bendValue.position() == 0) {
				if (!note.linkPrevious) {
					data.noteTails.add(new Line(lastBendLinePosition, lineTo, Color.WHITE, 2));
					addBendValueIcon(note, bendValue.position(), x, lineTo.y, bendValue.bendValue);
				}
				lastBendLinePosition = lineTo.move(1, 0);

				continue;
			}

			data.noteTails.add(new Line(lastBendLinePosition, lineTo, Color.WHITE, 2));
			addBendValueIcon(note, bendValue.position(), x, lineTo.y, bendValue.bendValue);
			linesDrawn = true;
			lastBendLinePosition = lineTo.move(1, 0);
		}

		if (linesDrawn) {
			data.noteTails.add(new Line(lastBendLinePosition,
					new Position2D(note.x + note.length - 2, lastBendLinePosition.y), Color.WHITE, 2));
		}
	}
}
