package log.charter.gui;

import java.awt.Color;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import log.charter.data.config.Config;
import log.charter.io.Logger;
import log.charter.util.RW;

public class ChartPanelColors {
	public enum ColorLabel {
		BASE_1(0, 96, 96), //
		BASE_2(32, 128, 128), //

		BASE_BG_0(0, 0, 0), //
		BASE_BG_1(32, 32, 32), //
		BASE_BG_2(64, 64, 64), //
		BASE_BG_3(96, 96, 96), //
		BASE_BG_4(128, 128, 128), //
		BASE_BG_5(255, 255, 255), //

		BASE_DARK_TEXT(64, 192, 192), //
		BASE_TEXT(96, 224, 224), //

		NOTE_BACKGROUND(16, 16, 16), //
		NOTE_ADD_LINE(0, 255, 0), //
		LANE(128, 128, 128), //
		MAIN_BEAT(255, 255, 255), //
		SECONDARY_BEAT(160, 160, 160), //
		GRID(96, 96, 96), //
		MARKER(255, 0, 0), //
		SECTION_NAME_BG(192, 128, 64), //
		PHRASE_NAME_BG(192, 0, 192), //
		EVENT_BG(128, 0, 0), //
		HIGHLIGHT(255, 0, 0), //
		SELECT(0, 255, 255), //
		VOCAL_SELECT(255, 0, 255), //

		NOTE_FLAG_MARKER(255, 255, 255), //
		SLIDE_NORMAL_FRET_BG(255, 255, 255), //
		SLIDE_NORMAL_FRET_TEXT(0, 0, 0), //
		SLIDE_UNPITCHED_FRET_BG(255, 128, 128), //
		SLIDE_UNPITCHED_FRET_TEXT(0, 0, 0), //
		NOTE_FULL_MUTE(128, 128, 128), //
		HAMMER_ON(255, 255, 255), //
		PULL_OFF(255, 255, 255), //
		TAP(0, 0, 0), //
		HARMONIC(255, 255, 255), //
		PINCH_HARMONIC(0, 0, 0), //

		LANE_0(96, 0, 0), //
		LANE_BRIGHT_0(128, 0, 0), //
		NOTE_0(192, 0, 0), //
		NOTE_TAIL_0(224, 0, 0), //
		NOTE_ACCENT_0(255, 0, 0), //

		LANE_1(96, 96, 0), //
		LANE_BRIGHT_1(128, 128, 0), //
		NOTE_1(192, 192, 0), //
		NOTE_TAIL_1(224, 224, 0), //
		NOTE_ACCENT_1(255, 255, 0), //

		LANE_2(0, 0, 144), //
		LANE_BRIGHT_2(0, 0, 208), //
		NOTE_2(16, 16, 224), //
		NOTE_TAIL_2(24, 24, 224), //
		NOTE_ACCENT_2(32, 32, 255), //

		LANE_3(96, 48, 0), //
		LANE_BRIGHT_3(128, 64, 0), //
		NOTE_3(192, 96, 0), //
		NOTE_TAIL_3(224, 105, 0), //
		NOTE_ACCENT_3(255, 128, 0), //

		LANE_4(0, 96, 0), //
		LANE_BRIGHT_4(0, 128, 0), //
		NOTE_4(0, 192, 0), //
		NOTE_TAIL_4(0, 224, 0), //
		NOTE_ACCENT_4(0, 255, 0), //

		LANE_5(96, 0, 96), //
		LANE_BRIGHT_5(128, 0, 128), //
		NOTE_5(192, 0, 192), //
		NOTE_TAIL_5(224, 0, 224), //
		NOTE_ACCENT_5(255, 0, 255), //

		LANE_6(48, 24, 0), //
		LANE_BRIGHT_6(64, 32, 0), //
		NOTE_6(96, 48, 0), //
		NOTE_TAIL_6(160, 80, 0), //
		NOTE_ACCENT_6(128, 64, 0), //

		LANE_7(16, 16, 16), //
		LANE_BRIGHT_7(48, 48, 48), //
		NOTE_7(0, 0, 0), //
		NOTE_TAIL_7(32, 32, 32), //
		NOTE_ACCENT_7(255, 255, 255), //

		LANE_8(128, 128, 128), //
		LANE_BRIGHT_8(160, 160, 160), //
		NOTE_8(255, 255, 255), //
		NOTE_TAIL_8(224, 224, 224), //
		NOTE_ACCENT_8(0, 0, 0), //

		ANCHOR(192, 32, 32), //
		HAND_SHAPE(0, 128, 255), //
		HAND_SHAPE_ARPEGGIO(128, 0, 255), //
		TONE_CHANGE(128, 128, 0), //

		VOCAL_LINE_BACKGROUND(0, 210, 210), //
		VOCAL_LINE_TEXT(0, 0, 128), //
		VOCAL_TEXT(210, 210, 210), //
		VOCAL_NOTE(0, 255, 255, 192), //
		VOCAL_NOTE_WORD_PART(0, 0, 255, 192), //

		PREVIEW_3D_CHORD_BOX(0, 192, 255), //
		PREVIEW_3D_FULL_MUTE(255, 255, 255), //
		PREVIEW_3D_PALM_MUTE(0, 0, 0), //
		PREVIEW_3D_CHORD_FULL_MUTE(0, 128, 160), //
		PREVIEW_3D_CHORD_PALM_MUTE(0, 128, 160), //
		PREVIEW_3D_ANCHOR(0, 0, 255, 64), //
		PREVIEW_3D_FRET_LANE(64, 192, 255), //
		PREVIEW_3D_BEAT(64, 128, 255);

		private final Color defaultColor;

		private ColorLabel(final int r, final int g, final int b) {
			defaultColor = new Color(r, g, b);
		}

		private ColorLabel(final int r, final int g, final int b, final int a) {
			defaultColor = new Color(r, g, b, a);
		}

		public Color color() {
			return colors.getOrDefault(this, defaultColor);
		}

		public void setColor(final Color color) {
			colors.put(this, color);
		}
	}

	public enum StringColorLabelType {
		LANE, LANE_BRIGHT, NOTE, NOTE_TAIL, NOTE_ACCENT
	}

	public static Color getStringBasedColor(final StringColorLabelType type, final int string, final int strings) {
		if (strings <= 6) {
			return ColorLabel.valueOf(type.name() + "_" + string).color();
		}

		final int offset = strings - 6;
		if (string < offset) {
			return ColorLabel.valueOf(type.name() + "_" + (Config.maxStrings - string - 1)).color();
		}
		return ColorLabel.valueOf(type.name() + "_" + (string - offset)).color();
	}

	private static final String colorFilePath = new File(RW.getProgramDirectory(), "colors.txt").getAbsolutePath();

	private static final Map<ColorLabel, Color> colors = new HashMap<>();

	public static void saveColors() {
		final Map<String, String> config = new HashMap<>();

		for (final Entry<ColorLabel, Color> colorEntry : colors.entrySet()) {
			final Color c = colorEntry.getValue();
			final String r = Integer.toHexString(c.getRed());
			final String g = Integer.toHexString(c.getGreen());
			final String b = Integer.toHexString(c.getBlue());
			final String a = Integer.toHexString(c.getAlpha());
			config.put(colorEntry.getKey().name(), r + " " + g + " " + b + " " + a);
		}

		RW.writeConfig(colorFilePath, config);
	}

	static {
		for (final ColorLabel colorLabel : ColorLabel.values()) {
			colors.put(colorLabel, colorLabel.defaultColor);
		}

		final Map<String, String> config = RW.readConfig(colorFilePath);
		for (final Entry<String, String> configEntry : config.entrySet()) {
			try {
				final ColorLabel colorLabel = ColorLabel.valueOf(configEntry.getKey());
				final String[] rgb = configEntry.getValue().split(" ");
				final Color color;
				if (rgb.length == 3) {
					color = new Color(Integer.valueOf(rgb[0], 16), Integer.valueOf(rgb[1], 16),
							Integer.valueOf(rgb[2], 16));
				} else if (rgb.length == 4) {
					color = new Color(Integer.valueOf(rgb[0], 16), Integer.valueOf(rgb[1], 16),
							Integer.valueOf(rgb[2], 16), Integer.valueOf(rgb[3], 16));
				} else {
					color = new Color(0, 0, 0, 255);
				}

				colors.put(colorLabel, color);
			} catch (final Exception e) {
				Logger.error("Couldn't load color " + configEntry.getKey() + "=" + configEntry.getValue(), e);
			}
		}

		saveColors();
	}
}
