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

import javax.swing.JFrame;

import log.charter.data.GridType;
import log.charter.data.song.BeatsMap.DistanceType;
import log.charter.io.Logger;
import log.charter.sound.SoundFileType;
import log.charter.sound.system.AudioSystemType;
import log.charter.sound.system.SoundSystem;
import log.charter.util.RW;

public class Config {
	private static class ValueAccessor {
		public static ValueAccessor empty = new ValueAccessor(s -> {}, () -> null);

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

	public static AudioSystemType audioOutSystemType = AudioSystemType.DEFAULT;
	public static String audioOutSystemName = null;
	public static int leftOutChannelId = 0;
	public static int rightOutChannelId = 1;
	public static AudioSystemType audioIn0SystemType = AudioSystemType.DEFAULT;
	public static String audioIn0SystemName = null;
	public static int inChannel0Id = 0;
	public static AudioSystemType audioIn1SystemType = AudioSystemType.DEFAULT;
	public static String audioIn1SystemName = null;
	public static int inChannel1Id = 1;

	public static SoundFileType baseAudioFormat = SoundFileType.WAV;
	public static int audioBufferSize = 2048;
	public static int audioBufferMs = 10;
	public static int antialiasingSamples = 16;

	public static DistanceType minNoteDistanceType = DistanceType.NOTES;
	public static int minNoteDistanceFactor = 32;
	public static DistanceType minTailLengthType = DistanceType.NOTES;
	public static int minTailLengthFactor = 32;
	public static int delay = 25;
	public static double volume = 1;
	public static int midiDelay = 200;
	public static double sfxVolume = 1;
	public static int markerOffset = 300;

	public static boolean invertStrings = false;
	public static boolean invertStrings3D = false;
	public static boolean leftHanded = false;
	public static boolean showChordIds = true;
	public static int frets = 28;
	public static int maxStrings = 9;
	public static int maxBendValue = 6;// in half steps
	public static int FPS = 60;
	public static int backupDelay = 600;

	public static int windowPosX = 100;
	public static int windowPosY = 100;
	public static int windowWidth = 1200;
	public static int windowHeight = 700;
	public static int windowExtendedState = JFrame.NORMAL;

	public static int previewWindowPosX = 200;
	public static int previewWindowPosY = 200;
	public static int previewWindowWidth = 1200;
	public static int previewWindowHeight = 700;
	public static int previewWindowExtendedState = JFrame.NORMAL;
	public static boolean previewWindowBorderless = false;

	public static int zoomLvl = 100;
	public static int stretchedMusicSpeed = 100;

	public static boolean showGrid = true;
	public static GridType gridType = GridType.BEAT;
	public static int gridSize = 4;
	public static boolean selectNotesByTails = false;
	public static boolean audioFolderChosenForNewSong = false;

	public static boolean debugLogging = false;

	private static boolean changed = false;

	private static final Map<String, ValueAccessor> valueAccessors = new HashMap<>();

	static {
		valueAccessors.put("language", ValueAccessor.forString(v -> language = v, () -> language));
		valueAccessors.put("lastDir", ValueAccessor.forString(v -> lastDir = v, () -> lastDir));
		valueAccessors.put("lastPath", ValueAccessor.forString(v -> lastPath = v, () -> lastPath));
		valueAccessors.put("musicPath", ValueAccessor.forString(v -> musicPath = v, () -> musicPath));
		valueAccessors.put("songsPath", ValueAccessor.forString(v -> songsPath = v, () -> songsPath));
		valueAccessors.put("audioOutSystemType", ValueAccessor
				.forString(v -> audioOutSystemType = AudioSystemType.valueOf(v), () -> audioOutSystemType.name()));
		valueAccessors.put("audioOutSystemName",
				ValueAccessor.forString(v -> audioOutSystemName = v, () -> audioOutSystemName));
		valueAccessors.put("leftOutChannelId",
				ValueAccessor.forInteger(v -> leftOutChannelId = v, () -> leftOutChannelId));
		valueAccessors.put("rightOutChannelId",
				ValueAccessor.forInteger(v -> rightOutChannelId = v, () -> rightOutChannelId));
		valueAccessors.put("audioIn0SystemType", ValueAccessor
				.forString(v -> audioIn0SystemType = AudioSystemType.valueOf(v), () -> audioIn0SystemType.name()));
		valueAccessors.put("audioIn0SystemName",
				ValueAccessor.forString(v -> audioIn0SystemName = v, () -> audioIn0SystemName));
		valueAccessors.put("inChannel0Id", ValueAccessor.forInteger(v -> inChannel0Id = v, () -> inChannel0Id));
		valueAccessors.put("audioIn1SystemType", ValueAccessor
				.forString(v -> audioIn1SystemType = AudioSystemType.valueOf(v), () -> audioIn1SystemType.name()));
		valueAccessors.put("audioIn1SystemName",
				ValueAccessor.forString(v -> audioIn1SystemName = v, () -> audioIn1SystemName));
		valueAccessors.put("inChannel1Id", ValueAccessor.forInteger(v -> inChannel1Id = v, () -> inChannel1Id));
		valueAccessors.put("baseAudioFormat",
				ValueAccessor.forString(v -> baseAudioFormat = SoundFileType.valueOf(v), () -> baseAudioFormat.name()));
		valueAccessors.put("audioBufferSize",
				ValueAccessor.forInteger(v -> audioBufferSize = v, () -> audioBufferSize));
		valueAccessors.put("audioBufferMs", ValueAccessor.forInteger(v -> audioBufferMs = v, () -> audioBufferMs));
		valueAccessors.put("antialiasingSamples",
				ValueAccessor.forInteger(v -> antialiasingSamples = v, () -> antialiasingSamples));

		valueAccessors.put("minNoteDistanceFactor", ValueAccessor
				.forString(v -> minNoteDistanceType = DistanceType.valueOf(v), () -> minNoteDistanceType.name()));
		valueAccessors.put("minNoteDistanceFactor",
				ValueAccessor.forInteger(v -> minNoteDistanceFactor = v, () -> minNoteDistanceFactor));
		valueAccessors.put("minTailLengthType", ValueAccessor
				.forString(v -> minTailLengthType = DistanceType.valueOf(v), () -> minTailLengthType.name()));
		valueAccessors.put("minTailLengthFactor",
				ValueAccessor.forInteger(v -> minTailLengthFactor = v, () -> minTailLengthFactor));
		valueAccessors.put("delay", ValueAccessor.forInteger(v -> delay = v, () -> delay));
		valueAccessors.put("volume", ValueAccessor.forDouble(v -> volume = v, () -> volume));
		valueAccessors.put("midiDelay", ValueAccessor.forInteger(v -> midiDelay = v, () -> midiDelay));
		valueAccessors.put("sfxVolume", ValueAccessor.forDouble(v -> sfxVolume = v, () -> sfxVolume));
		valueAccessors.put("markerOffset", ValueAccessor.forInteger(v -> markerOffset = v, () -> markerOffset));

		valueAccessors.put("invertStrings", ValueAccessor.forBoolean(v -> invertStrings = v, () -> invertStrings));
		valueAccessors.put("invertStrings3D",
				ValueAccessor.forBoolean(v -> invertStrings3D = v, () -> invertStrings3D));
		valueAccessors.put("leftHanded", ValueAccessor.forBoolean(v -> leftHanded = v, () -> leftHanded));
		valueAccessors.put("showChordIds", ValueAccessor.forBoolean(v -> showChordIds = v, () -> showChordIds));
		valueAccessors.put("frets", ValueAccessor.forInteger(v -> frets = v, () -> frets));
		valueAccessors.put("maxBendValue", ValueAccessor.forInteger(v -> maxBendValue = v, () -> maxBendValue));
		valueAccessors.put("FPS", ValueAccessor.forInteger(v -> FPS = v, () -> FPS));
		valueAccessors.put("backupDelay", ValueAccessor.forInteger(v -> backupDelay = v, () -> backupDelay));

		valueAccessors.put("windowPosX", ValueAccessor.forInteger(v -> windowPosX = v, () -> windowPosX));
		valueAccessors.put("windowPosY", ValueAccessor.forInteger(v -> windowPosY = v, () -> windowPosY));
		valueAccessors.put("windowWidth", ValueAccessor.forInteger(v -> windowWidth = v, () -> windowWidth));
		valueAccessors.put("windowHeight", ValueAccessor.forInteger(v -> windowHeight = v, () -> windowHeight));
		valueAccessors.put("windowExtendedState",
				ValueAccessor.forInteger(v -> windowExtendedState = v, () -> windowExtendedState));

		valueAccessors.put("previewWindowPosX",
				ValueAccessor.forInteger(v -> previewWindowPosX = v, () -> previewWindowPosX));
		valueAccessors.put("previewWindowPosY",
				ValueAccessor.forInteger(v -> previewWindowPosY = v, () -> previewWindowPosY));
		valueAccessors.put("previewWindowWidth",
				ValueAccessor.forInteger(v -> previewWindowWidth = v, () -> previewWindowWidth));
		valueAccessors.put("previewWindowHeight",
				ValueAccessor.forInteger(v -> previewWindowHeight = v, () -> previewWindowHeight));
		valueAccessors.put("previewWindowExtendedState",
				ValueAccessor.forInteger(v -> previewWindowExtendedState = v, () -> previewWindowExtendedState));
		valueAccessors.put("previewWindowBorderless",
				ValueAccessor.forBoolean(v -> previewWindowBorderless = v, () -> previewWindowBorderless));
		valueAccessors.put("zoomLvl", ValueAccessor.forInteger(v -> zoomLvl = v, () -> zoomLvl));
		valueAccessors.put("stretchedMusicSpeed",
				ValueAccessor.forInteger(v -> stretchedMusicSpeed = v, () -> stretchedMusicSpeed));

		valueAccessors.put("showGrid", ValueAccessor.forBoolean(v -> showGrid = v, () -> showGrid));
		valueAccessors.put("gridType",
				ValueAccessor.forString(v -> gridType = GridType.valueOf(v), () -> gridType.name()));
		valueAccessors.put("gridSize", ValueAccessor.forInteger(v -> gridSize = v, () -> gridSize));
		valueAccessors.put("selectNotesByTails",
				ValueAccessor.forBoolean(v -> selectNotesByTails = v, () -> selectNotesByTails));
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
		SoundSystem.setCurrentSoundSystem();
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
