package log.charter.data.config;

import static log.charter.data.config.values.ValueAccessor.forBoolean;
import static log.charter.data.config.values.ValueAccessor.forInteger;
import static log.charter.data.config.values.ValueAccessor.forString;
import static log.charter.io.Logger.error;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import log.charter.data.GridType;
import log.charter.data.config.values.AudioConfig;
import log.charter.data.config.values.DebugConfig;
import log.charter.data.config.values.GridConfig;
import log.charter.data.config.values.InstrumentConfig;
import log.charter.data.config.values.NoteDistanceConfig;
import log.charter.data.config.values.PassFiltersConfig;
import log.charter.data.config.values.PathsConfig;
import log.charter.data.config.values.ValueAccessor;
import log.charter.data.config.values.WindowStateConfig;
import log.charter.data.song.BeatsMap.DistanceType;
import log.charter.sound.SoundFileType;
import log.charter.sound.system.SoundSystem;
import log.charter.util.RW;
import log.charter.util.Utils;

public class Config {
	private static final String configPath = Utils.defaultConfigDir + "config.ini";

	public static String language = "English";
	public static boolean defaultEofShortcuts = false;

	public static SoundFileType baseAudioFormat = SoundFileType.WAV;

	public static boolean showTempoInsteadOfBPM = false;
	public static boolean showChordIds = true;
	public static int backupDelay = 600;
	public static int zoomLvl = 100;
	public static int stretchedMusicSpeed = 100;
	public static boolean selectNotesByTails = false;
	public static boolean audioFolderChosenForNewSong = false;

	private static boolean changed = false;

	private static final Map<String, ValueAccessor> valueAccessors = new HashMap<>();

	static {
		// older variable names
		// 0.21.0
		valueAccessors.put("showGrid", forBoolean(v -> GridConfig.showGrid = v, null));
		valueAccessors.put("gridType", forString(v -> GridConfig.gridType = GridType.valueOf(v), null));
		valueAccessors.put("gridSize", forInteger(v -> GridConfig.gridSize = v, null));
		valueAccessors.put("minNoteDistanceFactor",
				forString(v -> NoteDistanceConfig.minSpaceType = DistanceType.valueOf(v), null));
		valueAccessors.put("minNoteDistanceFactor", forInteger(v -> NoteDistanceConfig.minSpaceFactor = v, null));
		valueAccessors.put("minTailLengthType",
				forString(v -> NoteDistanceConfig.minLengthType = DistanceType.valueOf(v), null));
		valueAccessors.put("minTailLengthFactor", forInteger(v -> NoteDistanceConfig.minLengthFactor = v, null));
		valueAccessors.put("markerOffset", forInteger(v -> GraphicalConfig.markerOffset = v, null));
		valueAccessors.put("FPS", forInteger(v -> GraphicalConfig.FPS = v, null));
		valueAccessors.put("antialiasingSamples", forInteger(v -> GraphicalConfig.antialiasingSamples = v, null));
		valueAccessors.put("lastDir", forString(v -> PathsConfig.lastDir = v, null));
		valueAccessors.put("lastPath", forString(v -> PathsConfig.lastPath = v, null));
		valueAccessors.put("musicPath", forString(v -> PathsConfig.musicPath = v, null));
		valueAccessors.put("songsPath", forString(v -> PathsConfig.songsPath = v, null));
		valueAccessors.put("invertStrings", forBoolean(v -> GraphicalConfig.invertStrings = v, null));
		valueAccessors.put("invertStrings3D", forBoolean(v -> GraphicalConfig.invertStrings3D = v, null));

		// current variables
		valueAccessors.put("language", forString(v -> language = v, () -> language));

		valueAccessors.put("baseAudioFormat",
				forString(v -> baseAudioFormat = SoundFileType.valueOf(v), () -> baseAudioFormat.name()));
		valueAccessors.put("showTempoInsteadOfBPM",
				forBoolean(v -> showTempoInsteadOfBPM = v, () -> showTempoInsteadOfBPM));
		valueAccessors.put("showChordIds", forBoolean(v -> showChordIds = v, () -> showChordIds));
		valueAccessors.put("backupDelay", forInteger(v -> backupDelay = v, () -> backupDelay));

		valueAccessors.put("zoomLvl", forInteger(v -> zoomLvl = v, () -> zoomLvl));
		valueAccessors.put("stretchedMusicSpeed", forInteger(v -> stretchedMusicSpeed = v, () -> stretchedMusicSpeed));

		valueAccessors.put("selectNotesByTails", forBoolean(v -> selectNotesByTails = v, () -> selectNotesByTails));
		valueAccessors.put("audioFolderChosenForNewSong",
				forBoolean(v -> audioFolderChosenForNewSong = v, () -> audioFolderChosenForNewSong));

		AudioConfig.init(valueAccessors, "audio");
		DebugConfig.init(valueAccessors, "debug");
		GridConfig.init(valueAccessors, "grid");
		InstrumentConfig.init(valueAccessors, "instrument");
		NoteDistanceConfig.init(valueAccessors, "noteDistance");
		PassFiltersConfig.init(valueAccessors, "passFilters");
		PathsConfig.init(valueAccessors, "paths");
		WindowStateConfig.init(valueAccessors, "windowState");
	}

	private static void readConfigFrom(final String path) {
		for (final Entry<String, String> configVal : RW.readConfig(path, false).entrySet()) {
			try {
				valueAccessors.getOrDefault(configVal.getKey(), ValueAccessor.empty).set(configVal.getValue());
			} catch (final Throwable t) {
				error("wrong config line " + configVal.getKey() + "=" + configVal.getValue(), t);
			}
		}
	}

	public static void init() {
		readConfigFrom(configPath);

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

		new File(configPath).getParentFile().mkdirs();
		RW.writeConfig(configPath, config);

		changed = false;
	}

	public static void markChanged() {
		changed = true;
	}
}
