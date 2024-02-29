package log.charter.gui.chartPanelDrawers.instruments.guitar.theme.modern.iconGenerators;

import static java.lang.Math.max;
import static log.charter.gui.chartPanelDrawers.instruments.guitar.theme.modern.iconGenerators.IconGeneratorUtils.calculateSize;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;

public class MuteIconGenerator {
	private static Polygon generateX(final int size, final int space) {
		return new Polygon(new int[] { //
				0, size / 2 - space, 0, //
				space, size / 2, size - space - 1, //
				size - 1, size / 2 + space, size - 1, //
				size - space - 1, size / 2, space,//
		}, //
				new int[] { //
						space, size / 2, size - space - 1, //
						size - 1, size / 2 + space, size - 1, //
						size - space - 1, size / 2, space, //
						0, size / 2 - space, 0, //
				}, 12);
	}

	public static BufferedImage generatePalmMuteIcon() {
		final int size = max(16, calculateSize(1));
		final int space = max(2, size / 8);
		final int borderWidth = max(1, space / 3);
		final Color borderColor = Color.GRAY;
		final Color innerColor = Color.BLACK.brighter().brighter().brighter();

		final BufferedImage icon = new BufferedImage(size, size, BufferedImage.TYPE_4BYTE_ABGR);
		final Graphics2D g = (Graphics2D) icon.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		final Polygon inner = generateX(size - 2, space - 1);
		inner.translate(1, 1);
		g.setColor(innerColor);
		g.fill(inner);

		final Polygon outer = generateX(size, space);
		final Polygon outerSubtract = generateX(size - 4, space - borderWidth);
		outerSubtract.translate(2, 2);
		final Area borderArea = new Area(outer);
		borderArea.subtract(new Area(outerSubtract));
		g.setColor(borderColor);
		g.fill(borderArea);

		return icon;
	}

	public static BufferedImage generateFullMuteIcon() {
		final int size = max(16, calculateSize(1));
		final int space = max(2, size / 8);
		final int borderWidth = max(1, space / 3);
		final Color borderColor = Color.GRAY;
		final Color innerColor = Color.WHITE;

		final BufferedImage icon = new BufferedImage(size, size, BufferedImage.TYPE_4BYTE_ABGR);
		final Graphics2D g = (Graphics2D) icon.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		final Polygon inner = generateX(size - 2, space - 1);
		inner.translate(1, 1);
		g.setColor(innerColor);
		g.fill(inner);

		final Polygon outer = generateX(size, space);
		final Polygon outerSubtract = generateX(size - 4, space - borderWidth);
		outerSubtract.translate(2, 2);
		final Area borderArea = new Area(outer);
		borderArea.subtract(new Area(outerSubtract));
		g.setColor(borderColor);
		g.fill(borderArea);

		return icon;
	}
}
