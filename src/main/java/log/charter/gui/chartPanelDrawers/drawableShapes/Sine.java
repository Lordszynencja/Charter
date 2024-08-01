package log.charter.gui.chartPanelDrawers.drawableShapes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;

import log.charter.util.data.Position2D;

class Sine implements DrawableShape {
	private final Color color;
	private final int thickness;
	private Polygon p;
	
	public Sine(final Position2D from, final int length, final int amplitude, final int phase, final int period, final Color color) {
		this(from, length, amplitude, phase, period, color, 1);
	}

	public Sine(final Position2D from, final int length, final int amplitude, final int phase, final int period, final Color color, final int thickness) {
		this.color = color;
		this.thickness = thickness;
		this.p = new Polygon();
		
		final double A = amplitude;
		final double B = 2 * Math.PI / period;
		for (int x = 0; x < length; x++) {
			final int y = (int) (A * Math.sin(B * (x + phase)));
			p.addPoint(x + from.x, y + from.y);
		}
	}

	@Override
	public void draw(final Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(color);
		g.setStroke(new BasicStroke(thickness));
		g.drawPolyline(p.xpoints, p.ypoints, p.npoints);
	}
}
