package log.charter.gui.chartPanelDrawers.instruments.guitar.theme.modern.iconGenerators;

import static log.charter.data.config.GraphicalConfig.noteHeight;

public class IconGeneratorUtils {
	public static int calculateSize(final double multiplier) {
		int size = (int) (noteHeight * multiplier);
		if (size % 2 == noteHeight % 2) {
			size++;
		}
		return size;
	}
}
