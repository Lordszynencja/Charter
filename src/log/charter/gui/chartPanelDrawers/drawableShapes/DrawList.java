package log.charter.gui.chartPanelDrawers.drawableShapes;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Arrays;

public class DrawList {
	private int[] positions = new int[1024];
	private int id = 0;

	public void addPositions(final int x, final int y, final int w, final int h) {
		if (id >= positions.length) {
			positions = Arrays.copyOf(positions, positions.length * 2);
		}
		positions[id++] = x;
		positions[id++] = y;
		positions[id++] = w;
		positions[id++] = h;
	}

	public void draw(final Graphics g, final Color c) {
		g.setColor(c);
		int i = 0;
		while (i < id) {
			g.drawRect(positions[i++], positions[i++], positions[i++], positions[i++]);
		}
	}
}
