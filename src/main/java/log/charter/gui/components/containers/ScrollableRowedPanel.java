package log.charter.gui.components.containers;

import static log.charter.gui.components.utils.ComponentUtils.setComponentBounds;

import java.awt.Component;

import javax.swing.JPanel;

import log.charter.gui.components.utils.PaneSizes;
import log.charter.gui.components.utils.PaneSizesBuilder;

public class ScrollableRowedPanel extends CharterScrollPane {
	private static final long serialVersionUID = 7451891986721556918L;

	private static JPanel preparePanel(final PaneSizes sizes, final int rows) {
		final JPanel panel = new JPanel(null);
		panel.setOpaque(true);
		setComponentBounds(panel, 0, 0, sizes.width, sizes.getHeight(rows));

		return panel;
	}

	private final PaneSizes sizes;
	private final JPanel panel;

	public ScrollableRowedPanel(final int width, final int rows) {
		this(new PaneSizesBuilder(width).build(), rows);
	}

	public ScrollableRowedPanel(final PaneSizes sizes, final int rows) {
		this(sizes, preparePanel(sizes, rows));
	}

	private ScrollableRowedPanel(final PaneSizes sizes, final JPanel panel) {
		super(panel);

		this.sizes = sizes;
		this.panel = panel;
	}

	public void add(final Component component, final int x, final int row) {
		add(component, x, row, component.getWidth(), component.getHeight());
	}

	public void add(final Component component, final int x, final int row, final int width, final int height) {
		setComponentBounds(component, x, sizes.getY(row), width, height);
		panel.add(component);
	}
}
