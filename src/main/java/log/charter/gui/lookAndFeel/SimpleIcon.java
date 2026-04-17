package log.charter.gui.lookAndFeel;

import javax.swing.Icon;

import log.charter.data.config.GraphicalConfig;

public abstract class SimpleIcon implements Icon {
	@Override
	public int getIconWidth() {
		return GraphicalConfig.inputSize;
	}

	@Override
	public int getIconHeight() {
		return GraphicalConfig.inputSize;
	}
}
