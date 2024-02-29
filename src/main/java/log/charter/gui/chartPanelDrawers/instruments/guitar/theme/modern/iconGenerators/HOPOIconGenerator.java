package log.charter.gui.chartPanelDrawers.instruments.guitar.theme.modern.iconGenerators;

import static log.charter.data.config.GraphicalConfig.noteHeight;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;

public class HOPOIconGenerator {
	public static BufferedImage generateHammerOnIcon() {
		final int w = noteHeight / 2;
		final int h = noteHeight * 2 / 5;
		final BufferedImage icon = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
		final Graphics2D g = (Graphics2D) icon.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g.setColor(Color.WHITE);
		g.fill(new Polygon(new int[] { 1, w / 2, w - 1 }, new int[] { 1, h - 1, 1 }, 3));

		final Area area = new Area(new Polygon(new int[] { 0, w / 2, w }, new int[] { 0, h, 0 }, 3));
		area.subtract(new Area(new Polygon(new int[] { 2, w / 2, w - 2 }, new int[] { 1, h - 2, 1 }, 3)));
		g.setColor(Color.BLACK);
		g.fill(area);

		return icon;
	}

	public static BufferedImage generatePullOffIcon() {
		final int w = noteHeight / 2;
		final int h = noteHeight * 2 / 5;
		final BufferedImage icon = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
		final Graphics2D g = (Graphics2D) icon.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g.setColor(Color.WHITE);
		g.fill(new Polygon(new int[] { 1, w / 2, w - 1 }, new int[] { h - 1, 1, h - 1 }, 3));

		final Area area = new Area(new Polygon(new int[] { 0, w / 2, w }, new int[] { h, 0, h }, 3));
		area.subtract(new Area(new Polygon(new int[] { 2, w / 2, w - 2 }, new int[] { h - 1, 2, h - 1 }, 3)));
		g.setColor(Color.BLACK);
		g.fill(area);

		return icon;
	}

	public static BufferedImage generateTapIcon() {
		final int w = noteHeight / 2;
		final int h = noteHeight * 2 / 5;
		final BufferedImage icon = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
		final Graphics2D g = (Graphics2D) icon.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g.setColor(Color.BLACK);
		g.fill(new Polygon(new int[] { 1, w / 2, w - 1 }, new int[] { 1, h - 1, 1 }, 3));

		final Area area = new Area(new Polygon(new int[] { 0, w / 2, w }, new int[] { 0, h, 0 }, 3));
		area.subtract(new Area(new Polygon(new int[] { 2, w / 2, w - 2 }, new int[] { 1, h - 2, 1 }, 3)));
		g.setColor(Color.LIGHT_GRAY);
		g.fill(area);
		return icon;
	}

	public static BufferedImage generateSingleLetterIcon(final String letter, final Color color) {
		final int w = noteHeight / 2;
		final int h = noteHeight * 2 / 5;
		final BufferedImage icon = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
		final Graphics2D g = (Graphics2D) icon.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g.setColor(color);
		g.setFont(new Font(Font.DIALOG, Font.BOLD, h + 1));
		g.drawString(letter, 0, h - 1);

		return icon;
	}
}
