package log.charter.data.config;

import static log.charter.data.config.values.accessors.BooleanValueAccessor.forBoolean;
import static log.charter.data.config.values.accessors.EnumValueAccessor.forEnum;
import static log.charter.data.config.values.accessors.IntValueAccessor.forInteger;
import static log.charter.data.config.values.accessors.StringValueAccessor.forString;
import static log.charter.io.Logger.error;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import log.charter.data.config.values.AudioConfig;
import log.charter.data.config.values.DebugConfig;
import log.charter.data.config.values.GridConfig;
import log.charter.data.config.values.InstrumentConfig;
import log.charter.data.config.values.NoteDistanceConfig;
import log.charter.data.config.values.PassFiltersConfig;
import log.charter.data.config.values.PathsConfig;
import log.charter.data.config.values.SecretsConfig;
import log.charter.data.config.values.WindowStateConfig;
import log.charter.data.config.values.accessors.ValueAccessor;
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
		// current variables
		valueAccessors.put("language", forString(v -> language = v, () -> language, language));

		valueAccessors.put("baseAudioFormat",
				forEnum(SoundFileType.class, v -> baseAudioFormat = v, () -> baseAudioFormat, baseAudioFormat));
		valueAccessors.put("showTempoInsteadOfBPM",
				forBoolean(v -> showTempoInsteadOfBPM = v, () -> showTempoInsteadOfBPM, showTempoInsteadOfBPM));
		valueAccessors.put("showChordIds", forBoolean(v -> showChordIds = v, () -> showChordIds, showChordIds));
		valueAccessors.put("backupDelay", forInteger(v -> backupDelay = v, () -> backupDelay, backupDelay));

		valueAccessors.put("zoomLvl", forInteger(v -> zoomLvl = v, () -> zoomLvl, zoomLvl));
		valueAccessors.put("stretchedMusicSpeed",
				forInteger(v -> stretchedMusicSpeed = v, () -> stretchedMusicSpeed, stretchedMusicSpeed));

		valueAccessors.put("selectNotesByTails",
				forBoolean(v -> selectNotesByTails = v, () -> selectNotesByTails, selectNotesByTails));
		valueAccessors.put("audioFolderChosenForNewSong", forBoolean(v -> audioFolderChosenForNewSong = v,
				() -> audioFolderChosenForNewSong, audioFolderChosenForNewSong));

		AudioConfig.init(valueAccessors, "audio");
		DebugConfig.init(valueAccessors, "debug");
		GridConfig.init(valueAccessors, "grid");
		InstrumentConfig.init(valueAccessors, "instrument");
		NoteDistanceConfig.init(valueAccessors, "noteDistance");
		PassFiltersConfig.init(valueAccessors, "passFilters");
		PathsConfig.init(valueAccessors, "paths");
		SecretsConfig.init(valueAccessors, "secrets");
		WindowStateConfig.init(valueAccessors, "windowState");
	}

	private static void readConfigFrom(final String path) {
		for (final Entry<String, String> configVal : RW.readConfig(path, false).entrySet()) {
			try {
				final ValueAccessor valueAccessor = valueAccessors.get(configVal.getKey());
				if (valueAccessor == null) {
					continue;
				}

				valueAccessor.set(configVal.getValue());
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
		valueAccessors.forEach((name, accessor) -> accessor.saveTo(config, name));

		new File(configPath).getParentFile().mkdirs();
		RW.writeConfig(configPath, config);

		changed = false;
	}

	public static void markChanged() {
		changed = true;
	}
}
