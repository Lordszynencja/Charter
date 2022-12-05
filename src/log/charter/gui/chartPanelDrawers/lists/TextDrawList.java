package log.charter.gui.chartPanelDrawers.lists;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Arrays;

public class TextDrawList {
	private String[] strings = new String[16];
	private int[] positions = new int[32];
	private int id = 0;

	public void addString(final String s, final int x, final int y) {
		if (s == null) {
			return;
		}
		if (id >= positions.length) {
			strings = Arrays.copyOf(strings, strings.length * 2);
			positions = Arrays.copyOf(positions, positions.length * 2);
		}
		strings[id / 2] = s;
		positions[id++] = x;
		positions[id++] = y;
	}

	public void draw(final Graphics g, final Color c) {
		g.setColor(c);
		int i = 0;
		while (i < id) {
			g.drawString(strings[i / 2], positions[i++], positions[i++]);
		}
	}
}
