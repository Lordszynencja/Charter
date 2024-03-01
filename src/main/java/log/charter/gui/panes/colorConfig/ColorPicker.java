package log.charter.gui.panes.colorConfig;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JColorChooser;
import javax.swing.JComponent;

import log.charter.data.config.Localization.Label;
import log.charter.gui.ChartPanelColors.ColorLabel;

public class ColorPicker extends JComponent implements MouseListener {
	private static final long serialVersionUID = -5746700025590582773L;

	public final ColorLabel colorLabel;
	private Color color;

	public ColorPicker(final ColorLabel colorLabel) {
		super();

		this.colorLabel = colorLabel;
		color = colorLabel.color();

		final Dimension size = new Dimension(20, 20);
		setPreferredSize(size);

		addMouseListener(this);
	}

	public Color color() {
		return color;
	}

	@Override
	public void paintComponent(final Graphics g) {
		final int w = getWidth();
		final int h = getHeight();

		g.setColor(ColorLabel.BASE_BG_2.color());
		g.fillRect(0, 0, w, h);

		g.setColor(color);
		g.fillRect(2, 2, w - 2, h - 2);
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
		final String title = Label.CHOOSE_COLOR.label().formatted(colorLabel.label());
		final Color newColor = JColorChooser.showDialog(this, title, color);

		if (newColor != null) {
			color = newColor;
			repaint();
		}
	}

	@Override
	public void mousePressed(final MouseEvent e) {
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
	}

	@Override
	public void mouseEntered(final MouseEvent e) {
	}

	@Override
	public void mouseExited(final MouseEvent e) {
	}

}
