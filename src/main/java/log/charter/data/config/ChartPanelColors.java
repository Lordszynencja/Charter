package log.charter.data.config;

import static log.charter.util.ColorUtils.setAlpha;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import log.charter.data.config.Localization.Label;
import log.charter.util.ColorUtils;
import log.charter.util.RW;
import log.charter.util.Utils;

public class ChartPanelColors {
	public enum ColorLabel {
		BASE_1(0, 96, 96), //
		BASE_2(32, 128, 128), //

		BASE_BG_0(11, 12, 13), //
		BASE_BG_1(20, 21, 23), //
		BASE_BG_2(31, 32, 35), //
		BASE_BG_3(78, 81, 87), //
		BASE_BG_4(128, 128, 128), //
		BASE_BG_5(255, 255, 255), //

		BASE_DARK_TEXT(78, 81, 87), //
		BASE_TEXT(255, 255, 255), //
		BASE_TEXT_INPUT(255, 255, 255),

		BASE_BG_INPUT(14, 15, 17), //
		BASE_HIGHLIGHT(53, 116, 240), //
		BASE_BUTTON(112, 112, 112), //
		BASE_BORDER(82, 82, 82), //

		NOTE_BACKGROUND(16, 16, 16), //
		NOTE_ADD_LINE(0, 255, 0), //
		LANE(20, 21, 23), //
		MAIN_BEAT(222, 222, 255, 100), //
		MAIN_BEAT_DRAG(222, 222, 255, 200), //
		SECONDARY_BEAT(222, 222, 255, 50), //
		SECONDARY_BEAT_DRAG(222, 222, 255, 150), //
		BEAT_MARKER(222, 222, 0), //
		GRID(222, 222, 255, 25), //
		GRID_DRAGGED(222, 222, 255, 100), //
		BOOKMARK(64, 64, 255), //
		REPEAT_MARKER(33, 217, 245), //
		MARKER(255, 255, 255), //
		MARKER_TIME(255, 255, 255), //
		MARKER_TIME_BACKGROUND(32, 128, 128), //
		MARKER_VIEW_AREA(122, 122, 122), //
		ARRANGEMENT_TEXT(255, 255, 255), //
		SHOWLIGHT_MARKER(160, 160, 160), //
		SHOWLIGHT_LABEL_TEXT(255, 255, 255), //
		SHOWLIGHT_LABEL_BG(0, 0, 0, 160), //
		SECTION_NAME_BG(165, 54, 178), //
		PHRASE_NAME_BG(179, 118, 54), //
		SECTION_COLOR(165, 54, 178), //
		PHRASE_COLOR(179, 118, 54), //
		EVENT_BG(98, 72, 94), //
		HIGHLIGHT(122, 122, 122), //
		VOCAL_HIGHLIGHT(200, 200, 200), //
		SELECT(41, 188, 254), //
		VOCAL_SELECT(255, 255, 0), //
		WAVEFORM_COLOR(64, 128, 128), //
		WAVEFORM_RMS_COLOR(255, 128, 255),

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

		NOTE_0(237, 0, 0), //
		NOTE_1(242, 215, 6), //
		NOTE_2(37, 178, 255), //
		NOTE_3(255, 135, 10), //
		NOTE_4(133, 231, 71), //
		NOTE_5(210, 44, 248), //
		NOTE_6(62, 229, 223), //
		NOTE_7(182, 182, 182), //
		NOTE_8(208, 57, 57), //

		FHP(208, 57, 57), //
		HAND_SHAPE(49, 87, 167), //
		HAND_SHAPE_ARPEGGIO(133, 89, 183), //
		HAND_SHAPE_TEXT(255, 255, 255), //
		TONE_CHANGE(182, 182, 182), //
		TONE_CHANGE_TEXT(78, 81, 87), //

		VOCAL_LINE_BACKGROUND(56, 56, 64), //
		VOCAL_LINE_TEXT(255, 255, 255), //
		VOCAL_TEXT(210, 210, 210), //
		VOCAL_NOTE(13, 162, 255), //
		VOCAL_NOTE_WORD_PART(173, 89, 33, 192), //

		PREVIEW_3D_BACKGROUND(0, 0, 0), //
		PREVIEW_3D_FRET(32, 32, 32), //
		PREVIEW_3D_ACTIVE_FRET(192, 192, 192), //
		PREVIEW_3D_HIGHLIGHTED_FRET(255, 160, 0), //
		PREVIEW_3D_CHORD_BOX(0, 210, 213), //
		PREVIEW_3D_CHORD_BOX_DARK(0, 60, 61), //
		PREVIEW_3D_CHORD_NAME(224, 224, 224), //
		PREVIEW_3D_FULL_MUTE(255, 255, 255), //
		PREVIEW_3D_PALM_MUTE(0, 0, 0), //
		PREVIEW_3D_CHORD_FULL_MUTE(0, 192, 255), //
		PREVIEW_3D_CHORD_PALM_MUTE(0, 80, 100), //
		PREVIEW_3D_FHP(0, 0, 255, 64), //
		PREVIEW_3D_FHP_FRET_COLOR(255, 168, 33), //
		PREVIEW_3D_LANE(37, 144, 232, 64), //
		PREVIEW_3D_LANE_DOTTED(24, 92, 148, 64), //
		PREVIEW_3D_LANE_BORDER(7, 146, 143), //
		PREVIEW_3D_ARPEGGIO(192, 64, 255), //
		PREVIEW_3D_BEAT(15, 59, 94), //
		PREVIEW_3D_BEAT_NUMBER_ACTIVE_COLOR(135, 221, 246), //
		PREVIEW_3D_LYRICS(255, 255, 255), //
		PREVIEW_3D_LYRICS_PASSED(13, 162, 255); //

		public final Color defaultColor;

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

		public Color colorWithAlpha(final int alpha) {
			return setAlpha(color(), alpha);
		}

		public String label() {
			final String labelName = "COLOR_" + name();
			try {
				return Label.valueOf(labelName).label();
			} catch (final Exception e) {
				return labelName;
			}
		}
	}

	public enum StringColorLabelType {
		LANE, LANE_BRIGHT, NOTE, NOTE_TAIL, NOTE_ACCENT
	}

	public static Color getStringBasedColor(final StringColorLabelType type, final int string, final int strings) {
		final int stringId = Utils.stringId(string, strings);

		final Color base = ColorLabel.valueOf("NOTE_" + stringId).color();

		switch (type) {
			case LANE:
				return ColorUtils.multiplyColor(base, 0.8); //
			case LANE_BRIGHT:
				return ColorUtils.multiplyColor(base, 1); //
			case NOTE:
				return base;
			case NOTE_TAIL:
				return ColorUtils.multiplyColor(base, 0.66); //
			case NOTE_ACCENT:
				return ColorUtils.multiplyColor(base, 0.8); //
			default:
				return base;
		}
	}

	private static Map<ColorLabel, Color> colors = new HashMap<>();

	static {
		colors = ColorMap.full().colors;
		save();
	}

	private static String getColorAsString(final Color c) {
		final String r = Integer.toHexString(c.getRed());
		final String g = Integer.toHexString(c.getGreen());
		final String b = Integer.toHexString(c.getBlue());
		final String a = Integer.toHexString(c.getAlpha());
		return r + " " + g + " " + b + " " + a;
	}

	public static void save() {
		final Map<ColorLabel, Color> baseColors = ColorMap.forCurrentSet().colors;

		final Map<String, String> config = new HashMap<>();

		for (final Entry<ColorLabel, Color> colorEntry : colors.entrySet()) {
			final Color baseColor = baseColors.get(colorEntry.getKey());
			if (baseColor != null && baseColor.equals(colorEntry.getValue())) {
				continue;
			}

			config.put(colorEntry.getKey().name(), getColorAsString(colorEntry.getValue()));
		}

		RW.writeConfig(ColorMap.customColorsPath, config);
	}
}
