package log.charter.data.config;

import static log.charter.io.Logger.error;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import log.charter.data.config.values.ValueAccessor;
import log.charter.util.RW;

public class GraphicalConfig {
	private static final String configPath = new File(RW.getProgramDirectory(), "graphicalConfig.ini")
			.getAbsolutePath();

	public static Theme theme = Theme.MODERN;
	public static int eventsChangeHeight = 10;
	public static int toneChangeHeight = 10;
	public static int anchorInfoHeight = 10;
	public static int noteWidth = 25;
	public static int noteHeight = 25;
	public static int chordHeight = 10;
	public static int handShapesHeight = 10;
	public static int timingHeight = 24;
	public static int chartMapHeightMultiplier = 3;

	public static float tempoMapGhostNotesTransparency = 0.66f;
	public static double previewWindowScrollSpeed = 1.3;

	public static String colorSet = "default";
	public static String inlay = "default";
	public static String texturePack = "default";

	private static final Map<String, ValueAccessor> valueAccessors = new HashMap<>();
	private static boolean changed = false;

	static {
		valueAccessors.put("theme", ValueAccessor.forString(v -> theme = Theme.valueOf(v), () -> theme.name()));
		valueAccessors.put("eventsChangeHeight",
				ValueAccessor.forInteger(v -> eventsChangeHeight = v, () -> eventsChangeHeight));
		valueAccessors.put("toneChangeHeight",
				ValueAccessor.forInteger(v -> toneChangeHeight = v, () -> toneChangeHeight));
		valueAccessors.put("anchorInfoHeight",
				ValueAccessor.forInteger(v -> anchorInfoHeight = v, () -> anchorInfoHeight));
		valueAccessors.put("noteWidth", ValueAccessor.forInteger(v -> noteWidth = v, () -> noteWidth));
		valueAccessors.put("noteHeight", ValueAccessor.forInteger(v -> noteHeight = v, () -> noteHeight));
		valueAccessors.put("chordHeight", ValueAccessor.forInteger(v -> chordHeight = v, () -> chordHeight));
		valueAccessors.put("handShapesHeight",
				ValueAccessor.forInteger(v -> handShapesHeight = v, () -> handShapesHeight));
		valueAccessors.put("timingHeight", ValueAccessor.forInteger(v -> timingHeight = v, () -> timingHeight));
		valueAccessors.put("chartMapHeightMultiplier",
				ValueAccessor.forInteger(v -> chartMapHeightMultiplier = v, () -> chartMapHeightMultiplier));

		valueAccessors.put("tempoMapGhostNotesTransparency",
				ValueAccessor.forFloat(v -> tempoMapGhostNotesTransparency = v, () -> tempoMapGhostNotesTransparency));
		valueAccessors.put("previewWindowScrollSpeed",
				ValueAccessor.forDouble(v -> previewWindowScrollSpeed = v, () -> previewWindowScrollSpeed));

		valueAccessors.put("colorSet", ValueAccessor.forString(v -> colorSet = v, () -> colorSet));
		valueAccessors.put("inlay", ValueAccessor.forString(v -> inlay = v, () -> inlay));
		valueAccessors.put("textures", ValueAccessor.forString(v -> texturePack = v, () -> texturePack));
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
		RW.writeConfig(configPath, config);

		changed = false;
	}

	public static void markChanged() {
		changed = true;
	}
}
