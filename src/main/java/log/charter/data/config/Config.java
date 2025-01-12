package log.charter.data.config;

import static log.charter.data.config.ValueAccessor.forBoolean;
import static log.charter.data.config.ValueAccessor.forDouble;
import static log.charter.data.config.ValueAccessor.forInteger;
import static log.charter.data.config.ValueAccessor.forString;
import static log.charter.io.Logger.error;

import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JFrame;

import log.charter.data.GridType;
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
	public static boolean showTempoInsteadOfBPM = false;
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
	public static boolean specialDebugOption = false;

	private static boolean changed = false;

	private static final Map<String, ValueAccessor> valueAccessors = new HashMap<>();

	static {
		valueAccessors.put("language", forString(v -> language = v, () -> language));
		valueAccessors.put("lastDir", forString(v -> lastDir = v, () -> lastDir));
		valueAccessors.put("lastPath", forString(v -> lastPath = v, () -> lastPath));
		valueAccessors.put("musicPath", forString(v -> musicPath = v, () -> musicPath));
		valueAccessors.put("songsPath", forString(v -> songsPath = v, () -> songsPath));
		valueAccessors.put("audioOutSystemType",
				forString(v -> audioOutSystemType = AudioSystemType.valueOf(v), () -> audioOutSystemType.name()));
		valueAccessors.put("audioOutSystemName", forString(v -> audioOutSystemName = v, () -> audioOutSystemName));
		valueAccessors.put("leftOutChannelId", forInteger(v -> leftOutChannelId = v, () -> leftOutChannelId));
		valueAccessors.put("rightOutChannelId", forInteger(v -> rightOutChannelId = v, () -> rightOutChannelId));
		valueAccessors.put("audioIn0SystemType",
				forString(v -> audioIn0SystemType = AudioSystemType.valueOf(v), () -> audioIn0SystemType.name()));
		valueAccessors.put("audioIn0SystemName", forString(v -> audioIn0SystemName = v, () -> audioIn0SystemName));
		valueAccessors.put("inChannel0Id", forInteger(v -> inChannel0Id = v, () -> inChannel0Id));
		valueAccessors.put("audioIn1SystemType",
				forString(v -> audioIn1SystemType = AudioSystemType.valueOf(v), () -> audioIn1SystemType.name()));
		valueAccessors.put("audioIn1SystemName", forString(v -> audioIn1SystemName = v, () -> audioIn1SystemName));
		valueAccessors.put("inChannel1Id", forInteger(v -> inChannel1Id = v, () -> inChannel1Id));
		valueAccessors.put("baseAudioFormat",
				forString(v -> baseAudioFormat = SoundFileType.valueOf(v), () -> baseAudioFormat.name()));
		valueAccessors.put("audioBufferSize", forInteger(v -> audioBufferSize = v, () -> audioBufferSize));
		valueAccessors.put("audioBufferMs", forInteger(v -> audioBufferMs = v, () -> audioBufferMs));
		valueAccessors.put("antialiasingSamples", forInteger(v -> antialiasingSamples = v, () -> antialiasingSamples));

		valueAccessors.put("minNoteDistanceFactor",
				forString(v -> minNoteDistanceType = DistanceType.valueOf(v), () -> minNoteDistanceType.name()));
		valueAccessors.put("minNoteDistanceFactor",
				forInteger(v -> minNoteDistanceFactor = v, () -> minNoteDistanceFactor));
		valueAccessors.put("minTailLengthType",
				forString(v -> minTailLengthType = DistanceType.valueOf(v), () -> minTailLengthType.name()));
		valueAccessors.put("minTailLengthFactor", forInteger(v -> minTailLengthFactor = v, () -> minTailLengthFactor));
		valueAccessors.put("delay", forInteger(v -> delay = v, () -> delay));
		valueAccessors.put("volume", forDouble(v -> volume = v, () -> volume));
		valueAccessors.put("midiDelay", forInteger(v -> midiDelay = v, () -> midiDelay));
		valueAccessors.put("sfxVolume", forDouble(v -> sfxVolume = v, () -> sfxVolume));
		valueAccessors.put("markerOffset", forInteger(v -> markerOffset = v, () -> markerOffset));

		valueAccessors.put("invertStrings", forBoolean(v -> invertStrings = v, () -> invertStrings));
		valueAccessors.put("invertStrings3D", forBoolean(v -> invertStrings3D = v, () -> invertStrings3D));
		valueAccessors.put("leftHanded", forBoolean(v -> leftHanded = v, () -> leftHanded));
		valueAccessors.put("showTempoInsteadOfBPM",
				forBoolean(v -> showTempoInsteadOfBPM = v, () -> showTempoInsteadOfBPM));
		valueAccessors.put("showChordIds", forBoolean(v -> showChordIds = v, () -> showChordIds));
		valueAccessors.put("frets", forInteger(v -> frets = v, () -> frets));
		valueAccessors.put("maxBendValue", forInteger(v -> maxBendValue = v, () -> maxBendValue));
		valueAccessors.put("FPS", forInteger(v -> FPS = v, () -> FPS));
		valueAccessors.put("backupDelay", forInteger(v -> backupDelay = v, () -> backupDelay));

		valueAccessors.put("windowPosX", forInteger(v -> windowPosX = v, () -> windowPosX));
		valueAccessors.put("windowPosY", forInteger(v -> windowPosY = v, () -> windowPosY));
		valueAccessors.put("windowWidth", forInteger(v -> windowWidth = v, () -> windowWidth));
		valueAccessors.put("windowHeight", forInteger(v -> windowHeight = v, () -> windowHeight));
		valueAccessors.put("windowExtendedState", forInteger(v -> windowExtendedState = v, () -> windowExtendedState));

		valueAccessors.put("previewWindowPosX", forInteger(v -> previewWindowPosX = v, () -> previewWindowPosX));
		valueAccessors.put("previewWindowPosY", forInteger(v -> previewWindowPosY = v, () -> previewWindowPosY));
		valueAccessors.put("previewWindowWidth", forInteger(v -> previewWindowWidth = v, () -> previewWindowWidth));
		valueAccessors.put("previewWindowHeight", forInteger(v -> previewWindowHeight = v, () -> previewWindowHeight));
		valueAccessors.put("previewWindowExtendedState",
				forInteger(v -> previewWindowExtendedState = v, () -> previewWindowExtendedState));
		valueAccessors.put("previewWindowBorderless",
				forBoolean(v -> previewWindowBorderless = v, () -> previewWindowBorderless));
		valueAccessors.put("zoomLvl", forInteger(v -> zoomLvl = v, () -> zoomLvl));
		valueAccessors.put("stretchedMusicSpeed", forInteger(v -> stretchedMusicSpeed = v, () -> stretchedMusicSpeed));

		valueAccessors.put("showGrid", forBoolean(v -> showGrid = v, () -> showGrid));
		valueAccessors.put("gridType", forString(v -> gridType = GridType.valueOf(v), () -> gridType.name()));
		valueAccessors.put("gridSize", forInteger(v -> gridSize = v, () -> gridSize));
		valueAccessors.put("selectNotesByTails", forBoolean(v -> selectNotesByTails = v, () -> selectNotesByTails));
		valueAccessors.put("audioFolderChosenForNewSong",
				forBoolean(v -> audioFolderChosenForNewSong = v, () -> audioFolderChosenForNewSong));

		valueAccessors.put("debugLogging", forBoolean(v -> debugLogging = v, () -> debugLogging));
		valueAccessors.put("specialDebugOption", forBoolean(v -> specialDebugOption = v, () -> specialDebugOption));

		final String os = System.getProperty("os.name").toLowerCase();
		@SuppressWarnings("unused")
		final String osType = os.startsWith("windows") ? "windows" : os.startsWith("mac") ? "mac" : "linux";
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
