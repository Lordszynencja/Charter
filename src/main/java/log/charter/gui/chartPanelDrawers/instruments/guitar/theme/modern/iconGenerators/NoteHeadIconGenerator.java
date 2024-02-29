package log.charter.gui.chartPanelDrawers.instruments.guitar.theme.modern.iconGenerators;

import static java.lang.Math.max;
import static log.charter.gui.chartPanelDrawers.instruments.guitar.theme.modern.iconGenerators.IconGeneratorUtils.calculateSize;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;

public class NoteHeadIconGenerator {
	public static BufferedImage generateNoteIcon(final Color innerColor, final Color borderInnerColor,
			final Color borderOuterColor) {
		final int size = calculateSize(1);

		final BufferedImage icon = new BufferedImage(size, size, BufferedImage.TYPE_4BYTE_ABGR);
		final Graphics2D graphics = (Graphics2D) icon.getGraphics();

		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		final int borderSize = max(1, size / 15);
		final int borderInnerSize = borderSize * 2;

		final Ellipse2D inner = new Ellipse2D.Double(borderInnerSize, borderInnerSize, size - 2 * borderInnerSize,
				size - 2 * borderInnerSize);
		if (innerColor != null) {
			graphics.setColor(innerColor);
			graphics.fill(inner);
		}

		if (borderOuterColor != null) {
			final Ellipse2D outer1 = new Ellipse2D.Double(0, 0, size, size);
			final Area area1 = new Area(outer1);
			area1.subtract(new Area(inner));
			graphics.setColor(borderOuterColor);
			graphics.fill(area1);
		}

		if (borderInnerColor != null) {
			final Ellipse2D outer2 = new Ellipse2D.Double(borderSize, borderSize, size - 2 * borderSize,
					size - 2 * borderSize);
			final Area area2 = new Area(outer2);
			area2.subtract(new Area(inner));
			graphics.setColor(borderInnerColor);
			graphics.fill(area2);
		}

		return icon;
	}

	public static BufferedImage generateHarmonicNoteIcon(final Color innerColor, final Color borderInnerColor,
			final Color borderOuterColor) {
		final int size = calculateSize(1);

		final BufferedImage icon = new BufferedImage(size, size, BufferedImage.TYPE_4BYTE_ABGR);
		final Graphics2D g = (Graphics2D) icon.getGraphics();

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		final int borderSize = max(1, size / 15);
		final int borderInnerSize = borderSize * 2;

		final GeneralPath inner = new GeneralPath();
		inner.moveTo(size / 2, borderInnerSize);
		inner.lineTo(size - borderInnerSize, size / 2);
		inner.lineTo(size / 2, size - borderInnerSize);
		inner.lineTo(borderInnerSize, size / 2);
		inner.closePath();

		if (innerColor != null) {
			g.setColor(innerColor);
			g.fill(inner);
		}

		if (borderOuterColor != null) {
			final GeneralPath outer1 = new GeneralPath();
			outer1.moveTo(size / 2, 0);
			outer1.lineTo(size, size / 2);
			outer1.lineTo(size / 2, size);
			outer1.lineTo(0, size / 2);
			outer1.closePath();

			final Area area1 = new Area(outer1);
			area1.subtract(new Area(inner));
			g.setColor(borderOuterColor);
			g.fill(area1);
		}

		if (borderInnerColor != null) {
			final GeneralPath outer2 = new GeneralPath();
			outer2.moveTo(size / 2, borderSize);
			outer2.lineTo(size - borderSize, size / 2);
			outer2.lineTo(size / 2, size - borderSize);
			outer2.lineTo(borderSize, size / 2);
			outer2.closePath();

			final Area area2 = new Area(outer2);
			area2.subtract(new Area(inner));
			g.setColor(borderInnerColor);
			g.fill(area2);
		}

		return icon;
	}
}
