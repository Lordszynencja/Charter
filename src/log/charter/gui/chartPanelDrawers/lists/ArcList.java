package log.charter.gui.chartPanelDrawers.lists;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Arrays;

public class ArcList {
	private int[] positions = new int[6];
	private int id = 0;

	public void addPositions(final int x, final int y, final int w, final int h, final int rotation, final int angle) {
		if (id >= positions.length) {
			positions = Arrays.copyOf(positions, positions.length * 2);
		}
		positions[id++] = x;
		positions[id++] = y;
		positions[id++] = w;
		positions[id++] = h;
		positions[id++] = rotation;
		positions[id++] = angle;
	}

	public void draw(final Graphics g, final Color c) {
		g.setColor(c);
		int i = 0;
		while (i < id) {
			g.fillArc(positions[i++], positions[i++], positions[i++], positions[i++], positions[i++], positions[i++]);
		}
	}
}
