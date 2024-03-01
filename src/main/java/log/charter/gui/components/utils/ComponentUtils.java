package log.charter.gui.components.utils;

import java.awt.Component;
import java.awt.Dimension;

public class ComponentUtils {
	public static void setComponentBounds(final Component component, final int x, final int y, final int w,
			final int h) {
		component.setBounds(x, y, w, h);
		final Dimension size = new Dimension(w, h);
		component.setMinimumSize(size);
		component.setPreferredSize(size);
		component.setMaximumSize(size);
	}
}
