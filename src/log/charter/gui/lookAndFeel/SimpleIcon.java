package log.charter.gui.lookAndFeel;

import javax.swing.Icon;

public abstract class SimpleIcon implements Icon {
	public final int width;
	public final int height;

	protected SimpleIcon(final int width, final int height) {
		this.width = width;
		this.height = height;
	}

	@Override
	public int getIconWidth() {
		return width;
	}

	@Override
	public int getIconHeight() {
		return height;
	}
}
