package log.charter.data.config;

import static log.charter.io.Logger.error;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Supplier;

import log.charter.util.RW;

public class GraphicalConfig {
	private static class ValueAccessor {
		public static ValueAccessor empty = new ValueAccessor(s -> {}, () -> null);

		public static ValueAccessor forString(final Consumer<String> setter, final Supplier<String> getter) {
			return new ValueAccessor(setter, getter);
		}

		@SuppressWarnings("unused")
		public static ValueAccessor forBoolean(final Consumer<Boolean> setter, final Supplier<Boolean> getter) {
			return new ValueAccessor(s -> setter.accept(Boolean.valueOf(s)), () -> getter.get() + "");
		}

		public static ValueAccessor forInteger(final Consumer<Integer> setter, final Supplier<Integer> getter) {
			return new ValueAccessor(s -> setter.accept(Integer.valueOf(s)), () -> getter.get() + "");
		}

		@SuppressWarnings("unused")
		public static ValueAccessor forDouble(final Consumer<Double> setter, final Supplier<Double> getter) {
			return new ValueAccessor(s -> setter.accept(Double.valueOf(s)), () -> getter.get() + "");
		}

		private final Consumer<String> setter;
		private final Supplier<String> getter;

		private ValueAccessor(final Consumer<String> setter, final Supplier<String> getter) {
			this.setter = setter;
			this.getter = getter;
		}

		public void set(final String value) {
			setter.accept(value);
		}

		public String get() {
			return getter.get();
		}
	}

	private static final String configPath = new File(RW.getProgramDirectory(), "graphicalConfig.ini")
			.getAbsolutePath();

	public static Theme theme = Theme.MODERN;
	public static int eventsChangeHeight = 10; // added
	public static int toneChangeHeight = 10;
	public static int anchorInfoHeight = 10; // changed
	public static int noteWidth = 25;
	public static int noteHeight = 25;
	public static int chordHeight = 12; // added
	public static int handShapesHeight = 10; // changed
	public static int timingHeight = 24;
	public static double previewWindowScrollSpeed = 1.3;

	public static String inlay = "default";
	public static String texturePack = "default";

	public static int chartMapHeightMultiplier = 3;

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

		valueAccessors.put("inlay", ValueAccessor.forString(v -> inlay = v, () -> inlay));
		valueAccessors.put("textures", ValueAccessor.forString(v -> texturePack = v, () -> texturePack));
		valueAccessors.put("previewWindowScrollSpeed",
				ValueAccessor.forDouble(v -> previewWindowScrollSpeed = v, () -> previewWindowScrollSpeed));
	}

	public static void init() {
		for (final Entry<String, String> configVal : RW.readConfig(configPath).entrySet()) {
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
