package log.charter.data.config;

import static log.charter.data.config.values.ValueAccessor.forBoolean;
import static log.charter.data.config.values.ValueAccessor.forInteger;
import static log.charter.io.Logger.error;

import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import log.charter.data.config.values.ValueAccessor;
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
		valueAccessors.put("theme", ValueAccessor.forString(v -> theme = Theme.valueOf(v), () -> theme.name()));
		valueAccessors.put("eventsChangeHeight",
				ValueAccessor.forInteger(v -> eventsChangeHeight = v, () -> eventsChangeHeight));
		valueAccessors.put("toneChangeHeight",
				ValueAccessor.forInteger(v -> toneChangeHeight = v, () -> toneChangeHeight));
		valueAccessors.put("fhpInfoHeight", ValueAccessor.forInteger(v -> fhpInfoHeight = v, () -> fhpInfoHeight));
		valueAccessors.put("noteWidth", ValueAccessor.forInteger(v -> noteWidth = v, () -> noteWidth));
		valueAccessors.put("noteHeight", ValueAccessor.forInteger(v -> noteHeight = v, () -> noteHeight));
		valueAccessors.put("chordHeight", ValueAccessor.forInteger(v -> chordHeight = v, () -> chordHeight));
		valueAccessors.put("handShapesHeight",
				ValueAccessor.forInteger(v -> handShapesHeight = v, () -> handShapesHeight));
		valueAccessors.put("timingHeight", ValueAccessor.forInteger(v -> timingHeight = v, () -> timingHeight));
		valueAccessors.put("chartMapHeightMultiplier",
				ValueAccessor.forInteger(v -> chartMapHeightMultiplier = v, () -> chartMapHeightMultiplier));

		valueAccessors.put("markerOffset", forInteger(v -> markerOffset = v, () -> markerOffset));
		valueAccessors.put("tempoMapGhostNotesTransparency",
				ValueAccessor.forFloat(v -> tempoMapGhostNotesTransparency = v, () -> tempoMapGhostNotesTransparency));
		valueAccessors.put("previewWindowScrollSpeed",
				ValueAccessor.forDouble(v -> previewWindowScrollSpeed = v, () -> previewWindowScrollSpeed));
		valueAccessors.put("invertStrings", forBoolean(v -> invertStrings = v, () -> invertStrings));
		valueAccessors.put("invertStrings3D", forBoolean(v -> invertStrings3D = v, () -> invertStrings3D));

		valueAccessors.put("colorSet", ValueAccessor.forString(v -> colorSet = v, () -> colorSet));
		valueAccessors.put("inlay", ValueAccessor.forString(v -> inlay = v, () -> inlay));
		valueAccessors.put("textures", ValueAccessor.forString(v -> texturePack = v, () -> texturePack));

		valueAccessors.put("FPS", forInteger(v -> FPS = v, () -> FPS));
		valueAccessors.put("antialiasingSamples", forInteger(v -> antialiasingSamples = v, () -> antialiasingSamples));

		setDefaultFPS();
	}

	public static void init() {
		for (final Entry<String, String> configVal : RW.readConfig(configPath, false).entrySet()) {
			try {
				valueAccessors.getOrDefault(configVal.getKey(), ValueAccessor.empty).set(configVal.getValue());
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
		valueAccessors.forEach((name, accessor) -> config.put(name, accessor.get()));

		new File(configPath).getParentFile().mkdirs();
		RW.writeConfig(configPath, config);

		changed = false;
	}

	public static void markChanged() {
		changed = true;
	}
}
