package log.charter.gui;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import log.charter.io.Logger;
import log.charter.util.RW;

public class ChartPanelColors {
	public enum ColorLabel {
		BACKGROUND(80, 80, 80), //
		NOTE_BACKGROUND(16, 16, 16), //
		NOTE_ADD_LINE(0, 255, 0), //
		LANE(128, 128, 128), //
		MAIN_BEAT(255, 255, 255), //
		SECONDARY_BEAT(200, 200, 200), //
		MARKER(255, 0, 0), //
		SECTION_NAME(255, 255, 255), //
		PHRASE_NAME(255, 255, 0), //
		HIGHLIGHT(255, 0, 0), //
		SELECT(0, 255, 255), //
		SOLO_SECTION(100, 100, 210), //

		CRAZY_NOTE(0, 0, 0), //
		NOTE_FLAG_MARKER(255, 255, 255), //

		LANE_0(115, 10, 10), //
		LANE_1(115, 115, 10), //
		LANE_2(10, 10, 115), //
		LANE_3(115, 60, 10), //
		LANE_4(10, 115, 10), //
		LANE_5(115, 10, 115), //

		NOTE_0(230, 20, 20), //
		NOTE_1(230, 230, 20), //
		NOTE_2(20, 20, 230), //
		NOTE_3(230, 125, 20), //
		NOTE_4(20, 230, 20), //
		NOTE_5(230, 20, 230), //

		REPEATED_CHORD(50, 250, 250, 64), //
		HAND_SHAPE(0, 128, 255, 255), //

		NOTE_TAIL_0(210, 0, 0), //
		NOTE_TAIL_1(210, 210, 0), //
		NOTE_TAIL_2(0, 0, 210), //
		NOTE_TAIL_3(210, 105, 0), //
		NOTE_TAIL_4(0, 210, 0), //
		NOTE_TAIL_5(210, 0, 210), //

		VOCAL_LINE_BACKGROUND(100, 200, 200), //
		VOCAL_LINE_TEXT(0, 0, 128), //
		VOCAL_TEXT(255, 255, 255), //
		VOCAL_NOTE(0, 255, 255), //
		VOCAL_NOTE_WORD_PART(0, 0, 255), //
		VOCAL_NOTE_PHRASE_END(0, 255, 128),//
		;

		public final Color defaultColor;

		private ColorLabel(final int r, final int g, final int b) {
			defaultColor = new Color(r, g, b);
		}

		private ColorLabel(final int r, final int g, final int b, final int a) {
			defaultColor = new Color(r, g, b, a);
		}
	}

	private static final Map<ColorLabel, Color> colors = new HashMap<>();

	static {
		for (final ColorLabel colorLabel : ColorLabel.values()) {
			colors.put(colorLabel, colorLabel.defaultColor);
		}

		Map<String, String> config = RW.readConfig("colors.txt");
		for (final Entry<String, String> configEntry : config.entrySet()) {
			try {
				final ColorLabel colorLabel = ColorLabel.valueOf(configEntry.getKey());
				final String[] rgb = configEntry.getValue().split(" ");
				final Color color = new Color(Integer.valueOf(rgb[0], 16), Integer.valueOf(rgb[1], 16),
						Integer.valueOf(rgb[2], 16));

				colors.put(colorLabel, color);
			} catch (final Exception e) {
				Logger.error("Couldn't load color " + configEntry.getKey() + "=" + configEntry.getValue(), e);
			}
		}

		config = new HashMap<>();

		for (final Entry<ColorLabel, Color> colorEntry : colors.entrySet()) {
			final Color c = colorEntry.getValue();
			final String r = Integer.toHexString(c.getRed());
			final String g = Integer.toHexString(c.getGreen());
			final String b = Integer.toHexString(c.getBlue());
			config.put(colorEntry.getKey().name(), r + " " + g + " " + b);
		}

		RW.writeConfig("colors.txt", config);
	}

	public static Color get(final ColorLabel colorLabel) {
		return colors.get(colorLabel);
	}
}
