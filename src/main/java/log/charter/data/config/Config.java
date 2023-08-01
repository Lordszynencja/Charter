package log.charter.data.config;

import static log.charter.io.Logger.error;

import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Supplier;

import log.charter.io.Logger;
import log.charter.util.RW;

public class Config {
	private static class ValueAccessor {
		public static ValueAccessor empty = new ValueAccessor(s -> {
		}, () -> null);

		public static ValueAccessor forString(final Consumer<String> setter, final Supplier<String> getter) {
			return new ValueAccessor(setter, getter);
		}

		public static ValueAccessor forBoolean(final Consumer<Boolean> setter, final Supplier<Boolean> getter) {
			return new ValueAccessor(s -> setter.accept(Boolean.valueOf(s)), () -> getter.get() + "");
		}

		public static ValueAccessor forInteger(final Consumer<Integer> setter, final Supplier<Integer> getter) {
			return new ValueAccessor(s -> setter.accept(Integer.valueOf(s)), () -> getter.get() + "");
		}

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

	private static final String configPath = new File(RW.getProgramDirectory(), "config.ini").getAbsolutePath();

	public static String language = "English";
	public static String lastDir = "";
	public static String lastPath = "";
	public static String musicPath = System.getProperty("user.home") + File.separator + "Music";
	public static String songsPath = System.getProperty("user.home") + File.separator + "Documents";
	public static String rubberbandPath;
	public static String oggEncPath;

	public static int minNoteDistance = 50;
	public static int minTailLength = 50;
	public static int delay = 25;
	public static double volume = 1;
	public static int midiDelay = 200;
	public static double midiVolume = 1;
	public static int markerOffset = 300;
	public static Theme theme = Theme.ROCKSMITH;
	public static int noteWidth = 15;
	public static int noteHeight = 25;
	public static int chartMapHeightMultiplier = 3;

	public static boolean invertStrings = false;
	public static boolean leftHanded = false;
	public static boolean showChordIds = false;
	public static boolean createDefaultStretchesInBackground = true;
	public static int frets = 24;
	public static int maxStrings = 6;
	public static int maxBendValue = 3;
	public static int FPS = 60;
	public static int backupDelay = 600;

	public static int windowPosX = 100;
	public static int windowPosY = 100;
	public static int windowWidth = 1200;
	public static int windowHeight = 700;
	public static boolean windowFullscreen = false;
	public static int zoomLvl = 100;
	public static int stretchedMusicSpeed = 50;

	public static boolean showGrid = true;
	public static GridType gridType = GridType.BEAT;
	public static int gridSize = 4;
	public static boolean audioFolderChosenForNewSong = false;

	public static boolean debugLogging = false;
	public static boolean debugDrawing = false;

	private static boolean changed = false;

	private static final Map<String, ValueAccessor> valueAccessors = new HashMap<>();

	static {
		valueAccessors.put("language", ValueAccessor.forString(v -> language = v, () -> language));
		valueAccessors.put("lastDir", ValueAccessor.forString(v -> lastDir = v, () -> lastDir));
		valueAccessors.put("lastPath", ValueAccessor.forString(v -> lastPath = v, () -> lastPath));
		valueAccessors.put("musicPath", ValueAccessor.forString(v -> musicPath = v, () -> musicPath));
		valueAccessors.put("songsPath", ValueAccessor.forString(v -> songsPath = v, () -> songsPath));

		valueAccessors.put("minNoteDistance",
				ValueAccessor.forInteger(v -> minNoteDistance = v, () -> minNoteDistance));
		valueAccessors.put("minTailLength", ValueAccessor.forInteger(v -> minTailLength = v, () -> minTailLength));
		valueAccessors.put("delay", ValueAccessor.forInteger(v -> delay = v, () -> delay));
		valueAccessors.put("volume", ValueAccessor.forDouble(v -> volume = v, () -> volume));
		valueAccessors.put("midiDelay", ValueAccessor.forInteger(v -> midiDelay = v, () -> midiDelay));
		valueAccessors.put("midiVolume", ValueAccessor.forDouble(v -> midiVolume = v, () -> midiVolume));
		valueAccessors.put("markerOffset", ValueAccessor.forInteger(v -> markerOffset = v, () -> markerOffset));
		valueAccessors.put("noteWidth", ValueAccessor.forInteger(v -> noteWidth = v, () -> noteWidth));
		valueAccessors.put("noteHeight", ValueAccessor.forInteger(v -> noteHeight = v, () -> noteHeight));
		valueAccessors.put("chartMapHeightMultiplier",
				ValueAccessor.forInteger(v -> chartMapHeightMultiplier = v, () -> chartMapHeightMultiplier));

		valueAccessors.put("invertStrings", ValueAccessor.forBoolean(v -> invertStrings = v, () -> invertStrings));
		valueAccessors.put("leftHanded", ValueAccessor.forBoolean(v -> leftHanded = v, () -> leftHanded));
		valueAccessors.put("showChordIds", ValueAccessor.forBoolean(v -> showChordIds = v, () -> showChordIds));
		valueAccessors.put("createDefaultStretchesInBackground", ValueAccessor
				.forBoolean(v -> createDefaultStretchesInBackground = v, () -> createDefaultStretchesInBackground));
		valueAccessors.put("frets", ValueAccessor.forInteger(v -> frets = v, () -> frets));
		valueAccessors.put("maxStrings", ValueAccessor.forInteger(v -> maxStrings = v, () -> maxStrings));
		valueAccessors.put("FPS", ValueAccessor.forInteger(v -> FPS = v, () -> FPS));
		valueAccessors.put("backupDelay", ValueAccessor.forInteger(v -> backupDelay = v, () -> backupDelay));

		valueAccessors.put("theme", ValueAccessor.forString(v -> theme = Theme.valueOf(v), () -> theme.name()));

		valueAccessors.put("windowPosX", ValueAccessor.forInteger(v -> windowPosX = v, () -> windowPosX));
		valueAccessors.put("windowPosY", ValueAccessor.forInteger(v -> windowPosY = v, () -> windowPosY));
		valueAccessors.put("windowWidth", ValueAccessor.forInteger(v -> windowWidth = v, () -> windowWidth));
		valueAccessors.put("windowHeight", ValueAccessor.forInteger(v -> windowHeight = v, () -> windowHeight));
		valueAccessors.put("windowFullscreen",
				ValueAccessor.forBoolean(v -> windowFullscreen = v, () -> windowFullscreen));
		valueAccessors.put("zoomLvl", ValueAccessor.forInteger(v -> zoomLvl = v, () -> zoomLvl));
		valueAccessors.put("stretchedMusicSpeed",
				ValueAccessor.forInteger(v -> stretchedMusicSpeed = v, () -> stretchedMusicSpeed));

		valueAccessors.put("showGrid", ValueAccessor.forBoolean(v -> showGrid = v, () -> showGrid));
		valueAccessors.put("gridType",
				ValueAccessor.forString(v -> gridType = GridType.valueOf(v), () -> gridType.name()));
		valueAccessors.put("gridSize", ValueAccessor.forInteger(v -> gridSize = v, () -> gridSize));
		valueAccessors.put("audioFolderChosenForNewSong",
				ValueAccessor.forBoolean(v -> audioFolderChosenForNewSong = v, () -> audioFolderChosenForNewSong));

		valueAccessors.put("debugLogging", ValueAccessor.forBoolean(v -> debugLogging = v, () -> debugLogging));

		final String os = System.getProperty("os.name").toLowerCase();
		final String osPath = os.startsWith("windows")
				? "rubberband-3.1.2-gpl-executable-windows" + File.separator + "rubberband.exe" //
				: os.startsWith("mac") ? "rubberband-3.1.2-gpl-executable-macos" + File.separator + "rubberband"//
						: "rubberband-3.1.2-gpl-executable-windows" + File.separator + "rubberband.exe";
		rubberbandPath = new File(RW.getProgramDirectory(), "rubberband" + File.separator + osPath).getAbsolutePath();
		oggEncPath = new File(RW.getProgramDirectory(), "oggenc" + File.separator + "oggenc2.exe").getAbsolutePath();

		final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		final GraphicsDevice[] gs = ge.getScreenDevices();

		for (int i = 0; i < gs.length; i++) {
			final DisplayMode dm = gs[i].getDisplayMode();

			final int refreshRate = dm.getRefreshRate();
			if (refreshRate == DisplayMode.REFRESH_RATE_UNKNOWN) {
				Logger.error("Unknown monitor refresh rate, setting to 60");
				FPS = 60;
			} else {
				FPS = refreshRate;
				break;
			}
		}
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

		Localization.init();
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
