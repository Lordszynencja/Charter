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

	private final String label;
	private Color color;

	public ColorPicker(final ColorLabel label) {
		this(label.label(), label.color());
	}

	public ColorPicker(final Label label, final Color color) {
		this(label.label(), color);
	}

	public ColorPicker(final String label, final Color color) {
		super();

		this.label = label;
		this.color = color;

		final Dimension size = new Dimension(20, 20);
		setPreferredSize(size);

		addMouseListener(this);
	}

	public Color color() {
		return color;
	}

	public void color(final Color value) {
		color = value;
		repaint();
	}

	@Override
	public void paintComponent(final Graphics g) {
		final int w = getWidth();
		final int h = getHeight();

		g.setColor(ColorLabel.BASE_BG_4.color());
		g.drawRect(0, 0, w - 1, h - 1);
		g.setColor(ColorLabel.BASE_BG_3.color());
		g.drawRect(1, 1, w - 3, h - 3);

		g.setColor(color);
		g.fillRect(2, 2, w - 4, h - 4);
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
		final String title = Label.CHOOSE_COLOR_FOR.format(label);
		final Color newColor = JColorChooser.showDialog(this, title, color);

		if (newColor != null) {
			color(newColor);
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
