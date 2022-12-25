package log.charter.gui.handlers;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JLabel;
import javax.swing.JScrollBar;

import log.charter.data.config.Config;
import log.charter.gui.ChartPanel;
import log.charter.gui.CharterFrame;

public class CharterFrameComponentListener implements ComponentListener {

	private final CharterFrame frame;
	private final ChartPanel chartPanel;
	private final JLabel helpLabel;
	private final JScrollBar scrollBar;

	public CharterFrameComponentListener(final CharterFrame frame, final ChartPanel chartPanel, final JLabel helpLabel,
			final JScrollBar scrollBar) {
		this.frame = frame;
		this.chartPanel = chartPanel;
		this.helpLabel = helpLabel;
		this.scrollBar = scrollBar;
	}

	private void changeWidth(final Component c, final int w) {
		final int y = c.getY();
		final int h = c.getHeight();
		final Dimension newScrollBarSize = new Dimension(w, h);

		c.setMinimumSize(newScrollBarSize);
		c.setPreferredSize(newScrollBarSize);
		c.setMaximumSize(newScrollBarSize);
		c.setBounds(0, y, w, h);
		c.validate();
		c.repaint();
	}

	private void changeSize(final Component c, final int w, final int h) {
		final int y = c.getY();
		final Dimension newScrollBarSize = new Dimension(w, h);

		c.setMinimumSize(newScrollBarSize);
		c.setPreferredSize(newScrollBarSize);
		c.setMaximumSize(newScrollBarSize);
		c.setBounds(0, y, w, h);
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

		final Insets insets = frame.getInsets();
		final int widthDifference = insets.left + insets.right;

		changeWidth(chartPanel, Config.windowWidth - widthDifference);
		changeWidth(scrollBar, Config.windowWidth - widthDifference);
		changeSize(helpLabel, Config.windowWidth - widthDifference,
				Config.windowHeight - insets.top - insets.bottom - helpLabel.getY());
	}

	@Override
	public void componentShown(final ComponentEvent e) {
	}

}
