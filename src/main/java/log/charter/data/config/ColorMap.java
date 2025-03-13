package log.charter.data.config;

import java.awt.Color;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import log.charter.data.config.ChartPanelColors.ColorLabel;
import log.charter.io.Logger;
import log.charter.util.FileUtils;
import log.charter.util.RW;
import log.charter.util.Utils;

public class ColorMap {
	public static final File customColorsPath = new File(Utils.defaultConfigDir, "customColors.ini");

	private static int readColorValue(final String value, final int defaultValue) {
		try {
			return Integer.valueOf(value, 16);
		} catch (final NumberFormatException e) {
			return defaultValue;
		}
	}

	private static Color readColor(final String value) {
		final String[] rgb = value.split(" ");
		final int r = rgb.length < 1 ? 0 : readColorValue(rgb[0], 0);
		final int g = rgb.length < 2 ? 0 : readColorValue(rgb[1], 0);
		final int b = rgb.length < 3 ? 0 : readColorValue(rgb[2], 0);
		final int a = rgb.length < 4 ? 255 : readColorValue(rgb[3], 255);

		return new Color(r, g, b, a);
	}

	private static File colorSetFile(final String setName) {
		return new File(RW.getProgramDirectory(), FileUtils.colorSetsFolder + setName + ".txt");
	}

	public static ColorMap forSet(final String setName) {
		return new ColorMap()//
				.applyColorsFrom(colorSetFile(setName));
	}

	public static ColorMap forCurrentSet() {
		return forSet(GraphicalConfig.colorSet);
	}

	public static ColorMap full() {
		return forCurrentSet().applyColorsFrom(customColorsPath);
	}

	public final Map<ColorLabel, Color> colors = new HashMap<>();

	public ColorMap() {
		for (final ColorLabel colorLabel : ColorLabel.values()) {
			colors.put(colorLabel, colorLabel.defaultColor);
		}
	}

	public ColorMap applyColorsFrom(final File f) {
		final Map<String, String> config = RW.readConfig(f, false);
		for (final Entry<String, String> configEntry : config.entrySet()) {
			try {
				final ColorLabel colorLabel = ColorLabel.valueOf(configEntry.getKey());
				final Color color = readColor(configEntry.getValue());

				colors.put(colorLabel, color);
			} catch (final Exception e) {
				Logger.error("Couldn't load color " + configEntry.getKey() + "=" + configEntry.getValue(), e);
			}
		}

		return this;
	}
}
