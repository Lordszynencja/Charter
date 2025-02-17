package log.charter.data.config;

import static log.charter.data.config.values.ValueAccessor.forBoolean;
import static log.charter.data.config.values.ValueAccessor.forDouble;
import static log.charter.data.config.values.ValueAccessor.forInteger;
import static log.charter.data.config.values.ValueAccessor.forString;
import static log.charter.io.Logger.error;

import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import log.charter.data.GridType;
import log.charter.data.config.values.AudioConfig;
import log.charter.data.config.values.DebugConfig;
import log.charter.data.config.values.InstrumentConfig;
import log.charter.data.config.values.PassFiltersConfig;
import log.charter.data.config.values.ValueAccessor;
import log.charter.data.config.values.WindowConfig;
import log.charter.data.song.BeatsMap.DistanceType;
import log.charter.io.Logger;
import log.charter.sound.SoundFileType;
import log.charter.sound.system.AudioSystemType;
import log.charter.sound.system.SoundSystem;
import log.charter.util.RW;

public class Config {
	private static final String configPath = new File(RW.getProgramDirectory(), "config.ini").getAbsolutePath();

	public static String language = "English";
	public static String lastDir = "";
	public static String lastPath = "";
	public static String musicPath = System.getProperty("user.home") + File.separator + "Music";
	public static String songsPath = System.getProperty("user.home") + File.separator + "Documents";
	public static String oggEncPath;
	public static boolean defaultEofShortcuts = false;

	public static SoundFileType baseAudioFormat = SoundFileType.WAV;
	public static int antialiasingSamples = 16;

	public static DistanceType minNoteDistanceType = DistanceType.NOTES;
	public static int minNoteDistanceFactor = 32;
	public static DistanceType minTailLengthType = DistanceType.NOTES;
	public static int minTailLengthFactor = 32;
	public static int markerOffset = 600;

	public static boolean invertStrings = false;
	public static boolean invertStrings3D = false;
	public static boolean showTempoInsteadOfBPM = false;
	public static boolean showChordIds = true;
	public static int FPS = 60;
	public static int backupDelay = 600;

	public static int zoomLvl = 100;
	public static int stretchedMusicSpeed = 100;

	public static boolean showGrid = true;
	public static GridType gridType = GridType.BEAT;
	public static int gridSize = 4;
	public static boolean selectNotesByTails = false;
	public static boolean audioFolderChosenForNewSong = false;

	public static AudioConfig audio = new AudioConfig();
	public static DebugConfig debug = new DebugConfig();
	public static InstrumentConfig instrument = new InstrumentConfig();
	public static PassFiltersConfig passFilters = new PassFiltersConfig();
	public static WindowConfig window = new WindowConfig();

	private static boolean changed = false;

	private static final Map<String, ValueAccessor> valueAccessors = new HashMap<>();

	static {
		// older variable names
		// 0.19.20
		valueAccessors.put("audioOutSystemType", forString(v -> audio.outSystem = AudioSystemType.valueOf(v), null));
		valueAccessors.put("audioOutSystemName", forString(v -> audio.outSystemName = v, null));
		valueAccessors.put("leftOutChannelId", forInteger(v -> audio.leftOutChannelId = v, null));
		valueAccessors.put("rightOutChannelId", forInteger(v -> audio.rightOutChannelId = v, null));
		valueAccessors.put("audioIn0SystemType", forString(v -> audio.in0System = AudioSystemType.valueOf(v), null));
		valueAccessors.put("audioIn0SystemName", forString(v -> audio.in0SystemName = v, null));
		valueAccessors.put("inChannel0Id", forInteger(v -> audio.inChannel0Id = v, null));
		valueAccessors.put("audioIn1SystemType", forString(v -> audio.in1System = AudioSystemType.valueOf(v), null));
		valueAccessors.put("audioIn1SystemName", forString(v -> audio.in1SystemName = v, null));
		valueAccessors.put("inChannel1Id", forInteger(v -> audio.inChannel1Id = v, null));
		valueAccessors.put("audioBufferSize", forInteger(v -> audio.bufferSize = v, null));
		valueAccessors.put("audioBufferMs", forInteger(v -> audio.bufferedMs = v, null));
		valueAccessors.put("delay", forInteger(v -> audio.delay = v, null));
		valueAccessors.put("midiDelay", forInteger(v -> audio.midiDelay = v, null));
		valueAccessors.put("volume", forDouble(v -> audio.volume = v, null));
		valueAccessors.put("sfxVolume", forDouble(v -> audio.sfxVolume = v, null));

		valueAccessors.put("leftHanded", forBoolean(v -> instrument.leftHanded = v, null));
		valueAccessors.put("frets", forInteger(v -> instrument.frets = v, null));
		valueAccessors.put("maxBendValue", forInteger(v -> instrument.maxBendValue = v, null));

		valueAccessors.put("windowPosX", forInteger(v -> window.x = v, null));
		valueAccessors.put("windowPosY", forInteger(v -> window.y = v, null));
		valueAccessors.put("windowWidth", forInteger(v -> window.width = v, null));
		valueAccessors.put("windowHeight", forInteger(v -> window.height = v, null));
		valueAccessors.put("windowExtendedState", forInteger(v -> window.extendedState = v, null));
		valueAccessors.put("previewWindowPosX", forInteger(v -> window.previewX = v, null));
		valueAccessors.put("previewWindowPosY", forInteger(v -> window.previewY = v, null));
		valueAccessors.put("previewWindowWidth", forInteger(v -> window.previewWidth = v, null));
		valueAccessors.put("previewWindowHeight", forInteger(v -> window.previewHeight = v, null));
		valueAccessors.put("previewWindowExtendedState", forInteger(v -> window.previewExtendedState = v, null));
		valueAccessors.put("previewWindowBorderless", forBoolean(v -> window.previewBorderless = v, null));

		// current variables
		valueAccessors.put("language", forString(v -> language = v, () -> language));
		valueAccessors.put("lastDir", forString(v -> lastDir = v, () -> lastDir));
		valueAccessors.put("lastPath", forString(v -> lastPath = v, () -> lastPath));
		valueAccessors.put("musicPath", forString(v -> musicPath = v, () -> musicPath));
		valueAccessors.put("songsPath", forString(v -> songsPath = v, () -> songsPath));

		valueAccessors.put("baseAudioFormat",
				forString(v -> baseAudioFormat = SoundFileType.valueOf(v), () -> baseAudioFormat.name()));
		valueAccessors.put("antialiasingSamples", forInteger(v -> antialiasingSamples = v, () -> antialiasingSamples));

		valueAccessors.put("minNoteDistanceFactor",
				forString(v -> minNoteDistanceType = DistanceType.valueOf(v), () -> minNoteDistanceType.name()));
		valueAccessors.put("minNoteDistanceFactor",
				forInteger(v -> minNoteDistanceFactor = v, () -> minNoteDistanceFactor));
		valueAccessors.put("minTailLengthType",
				forString(v -> minTailLengthType = DistanceType.valueOf(v), () -> minTailLengthType.name()));
		valueAccessors.put("minTailLengthFactor", forInteger(v -> minTailLengthFactor = v, () -> minTailLengthFactor));
		valueAccessors.put("markerOffset", forInteger(v -> markerOffset = v, () -> markerOffset));

		valueAccessors.put("invertStrings", forBoolean(v -> invertStrings = v, () -> invertStrings));
		valueAccessors.put("invertStrings3D", forBoolean(v -> invertStrings3D = v, () -> invertStrings3D));
		valueAccessors.put("showTempoInsteadOfBPM",
				forBoolean(v -> showTempoInsteadOfBPM = v, () -> showTempoInsteadOfBPM));
		valueAccessors.put("showChordIds", forBoolean(v -> showChordIds = v, () -> showChordIds));
		valueAccessors.put("FPS", forInteger(v -> FPS = v, () -> FPS));
		valueAccessors.put("backupDelay", forInteger(v -> backupDelay = v, () -> backupDelay));

		valueAccessors.put("zoomLvl", forInteger(v -> zoomLvl = v, () -> zoomLvl));
		valueAccessors.put("stretchedMusicSpeed", forInteger(v -> stretchedMusicSpeed = v, () -> stretchedMusicSpeed));

		valueAccessors.put("showGrid", forBoolean(v -> showGrid = v, () -> showGrid));
		valueAccessors.put("gridType", forString(v -> gridType = GridType.valueOf(v), () -> gridType.name()));
		valueAccessors.put("gridSize", forInteger(v -> gridSize = v, () -> gridSize));
		valueAccessors.put("selectNotesByTails", forBoolean(v -> selectNotesByTails = v, () -> selectNotesByTails));
		valueAccessors.put("audioFolderChosenForNewSong",
				forBoolean(v -> audioFolderChosenForNewSong = v, () -> audioFolderChosenForNewSong));

		audio.init(valueAccessors, "audio");
		debug.init(valueAccessors, "debug");
		passFilters.init(valueAccessors, "passFilters");
		window.init(valueAccessors, "windowState");

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

	private static void readConfigFrom(final String path) {
		for (final Entry<String, String> configVal : RW.readConfig(path, false).entrySet()) {
			try {
				valueAccessors.getOrDefault(configVal.getKey(), ValueAccessor.empty).set(configVal.getValue());
			} catch (final Exception e) {
				error("wrong config line " + configVal.getKey() + "=" + configVal.getValue(), e);
			}
		}
	}

	public static void init() {
		readConfigFrom(configPath);
		readConfigFrom(System.getProperty("user.home") + File.separator + "Documents" + File.separator
				+ "CharterDebugConfig.ini");

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
		valueAccessors.forEach((name, accessor) -> {
			if (accessor.hasGetter()) {
				config.put(name, accessor.get());
			}
		});
		RW.writeConfig(configPath, config);

		changed = false;
	}

	public static void markChanged() {
		changed = true;
	}
}
