package log.charter.data.config;

import static log.charter.data.config.values.accessors.BooleanValueAccessor.forBoolean;
import static log.charter.data.config.values.accessors.DoubleValueAccessor.forDouble;
import static log.charter.data.config.values.accessors.EnumValueAccessor.forEnum;
import static log.charter.data.config.values.accessors.FloatValueAccessor.forFloat;
import static log.charter.data.config.values.accessors.IntValueAccessor.forInteger;
import static log.charter.data.config.values.accessors.StringValueAccessor.forString;
import static log.charter.io.Logger.error;

import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import log.charter.data.config.values.accessors.ValueAccessor;
import log.charter.io.Logger;
import log.charter.util.RW;
import log.charter.util.Utils;

public class GraphicalConfig {
	private static final String configPath = Utils.defaultConfigDir + "graphicalConfig.ini";

	public static Theme theme = Theme.MODERN;
	public static int eventsChangeHeight = 10;
	public static int toneChangeHeight = 10;
	public static int fhpInfoHeight = 10;
	public static int noteWidth = 25;
	public static int noteHeight = 25;
	public static int chordHeight = 10;
	public static int handShapesHeight = 10;
	public static int timingHeight = 24;
	public static int chartMapHeightMultiplier = 3;

	public static int markerOffset = 600;
	public static float tempoMapGhostNotesTransparency = 0.66f;
	public static double previewWindowScrollSpeed = 1.3;
	public static boolean invertStrings = false;
	public static boolean invertStrings3D = false;

	public static String colorSet = "default";
	public static String inlay = "default";
	public static String texturePack = "default";

	public static int FPS = 60;
	public static int antialiasingSamples = 16;

	private static final Map<String, ValueAccessor> valueAccessors = new HashMap<>();
	private static boolean changed = false;

	private static void setDefaultFPS() {
		final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		final DisplayMode dm = ge.getDefaultScreenDevice().getDisplayMode();

		final int refreshRate = dm.getRefreshRate();
		if (refreshRate == DisplayMode.REFRESH_RATE_UNKNOWN) {
			Logger.error("Unknown monitor refresh rate, setting to 60");
			GraphicalConfig.FPS = 60;
		} else {
			GraphicalConfig.FPS = refreshRate;
		}
	}

	static {
		valueAccessors.put("theme", forEnum(Theme.class, v -> theme = v, () -> theme, theme));
		valueAccessors.put("eventsChangeHeight",
				forInteger(v -> eventsChangeHeight = v, () -> eventsChangeHeight, eventsChangeHeight));
		valueAccessors.put("toneChangeHeight",
				forInteger(v -> toneChangeHeight = v, () -> toneChangeHeight, toneChangeHeight));
		valueAccessors.put("fhpInfoHeight", forInteger(v -> fhpInfoHeight = v, () -> fhpInfoHeight, fhpInfoHeight));
		valueAccessors.put("noteWidth", forInteger(v -> noteWidth = v, () -> noteWidth, noteWidth));
		valueAccessors.put("noteHeight", forInteger(v -> noteHeight = v, () -> noteHeight, noteHeight));
		valueAccessors.put("chordHeight", forInteger(v -> chordHeight = v, () -> chordHeight, chordHeight));
		valueAccessors.put("handShapesHeight",
				forInteger(v -> handShapesHeight = v, () -> handShapesHeight, handShapesHeight));
		valueAccessors.put("timingHeight", forInteger(v -> timingHeight = v, () -> timingHeight, timingHeight));
		valueAccessors.put("chartMapHeightMultiplier",
				forInteger(v -> chartMapHeightMultiplier = v, () -> chartMapHeightMultiplier, chartMapHeightMultiplier));

		valueAccessors.put("markerOffset", forInteger(v -> markerOffset = v, () -> markerOffset, markerOffset));
		valueAccessors.put("tempoMapGhostNotesTransparency", forFloat(v -> tempoMapGhostNotesTransparency = v,
				() -> tempoMapGhostNotesTransparency, tempoMapGhostNotesTransparency));
		valueAccessors.put("previewWindowScrollSpeed",
				forDouble(v -> previewWindowScrollSpeed = v, () -> previewWindowScrollSpeed, previewWindowScrollSpeed));
		valueAccessors.put("invertStrings", forBoolean(v -> invertStrings = v, () -> invertStrings, invertStrings));
		valueAccessors.put("invertStrings3D", forBoolean(v -> invertStrings3D = v, () -> invertStrings3D, invertStrings3D));

		valueAccessors.put("colorSet", forString(v -> colorSet = v, () -> colorSet, colorSet));
		valueAccessors.put("inlay", forString(v -> inlay = v, () -> inlay, inlay));
		valueAccessors.put("textures", forString(v -> texturePack = v, () -> texturePack, texturePack));

		valueAccessors.put("FPS", forInteger(v -> FPS = v, () -> FPS, FPS));
		valueAccessors.put("antialiasingSamples",
				forInteger(v -> antialiasingSamples = v, () -> antialiasingSamples, antialiasingSamples));

		setDefaultFPS();
	}

	public static void init() {
		for (final Entry<String, String> configVal : RW.readConfig(configPath, false).entrySet()) {
			try {
				final ValueAccessor valueAccessor = valueAccessors.get(configVal.getKey());
				if (valueAccessor == null) {
					continue;
				}

				valueAccessor.set(configVal.getValue());
			} catch (final Exception e) {
				error("wrong config line " + configVal.getKey() + "=" + configVal.getValue(), e);
			}
		}

		markChanged();
		save();
	}

	public static void save() {
		if (!changed) {
			return;
		}

		final Map<String, String> config = new HashMap<>();
		valueAccessors.forEach((name, accessor) -> accessor.saveTo(config, name));

		new File(configPath).getParentFile().mkdirs();
		RW.writeConfig(configPath, config);

		changed = false;
	}

	public static void markChanged() {
		changed = true;
	}
}
