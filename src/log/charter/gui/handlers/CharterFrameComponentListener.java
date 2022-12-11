package log.charter.gui.handlers;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JScrollBar;

import log.charter.data.Config;
import log.charter.gui.ChartPanel;

public class CharterFrameComponentListener implements ComponentListener {

	private final ChartPanel chartPanel;
	private final JScrollBar scrollBar;

	public CharterFrameComponentListener(final ChartPanel chartPanel, final JScrollBar scrollBar) {
		this.chartPanel = chartPanel;
		this.scrollBar = scrollBar;
	}

	private void changeWidth(final Component c, final int w) {
		final int y = c.getY();
		final int h = c.getHeight();
		final Dimension newScrollBarSize = new Dimension(Config.windowWidth, h);
		c.setMinimumSize(newScrollBarSize);
		c.setPreferredSize(newScrollBarSize);
		c.setMaximumSize(newScrollBarSize);
		c.setBounds(0, y, Config.windowWidth, h);
		c.validate();
		c.repaint();
	}

	@Override
	public void componentHidden(final ComponentEvent e) {
	}

	@Override
	public void componentMoved(final ComponentEvent e) {
		Config.windowPosX = e.getComponent().getX();
		Config.windowPosY = e.getComponent().getY();
	}

	@Override
	public void componentResized(final ComponentEvent e) {
		Config.windowHeight = e.getComponent().getHeight();
		Config.windowWidth = e.getComponent().getWidth();

		changeWidth(scrollBar, Config.windowWidth);
		changeWidth(chartPanel, Config.windowWidth);
	}

	@Override
	public void componentShown(final ComponentEvent e) {
	}

}
