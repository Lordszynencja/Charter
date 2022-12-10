package log.charter.data;

import static log.charter.io.Logger.error;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import log.charter.util.RW;

public class Config {
	private static final String configName = "config.ini";

	public static String lastPath = "C:/";
	public static String musicPath = System.getProperty("user.home") + File.separator + "Music";
	public static String songsPath = System.getProperty("user.home") + File.separator + "Documents";

	public static int windowPosX = 100;
	public static int windowPosY = 100;
	public static int windowWidth = 800;
	public static int windowHeight = 600;
	public static int zoomLvl = 0;

	public static int minNoteDistance = 5;
	public static int minLongNoteDistance = 30;
	public static int minTailLength = 30;
	public static int delay = 15;
	public static int markerOffset = 300;

	public static boolean invertStrings = false;

	public static boolean debugLogging = false;
	public static boolean changed = false;

	private static final Map<String, Consumer<String>> setters = new HashMap<>();

	public static void init() {
		setters.put("debugLogging", val -> debugLogging = Boolean.valueOf(val));
		setters.put("delay", val -> delay = Integer.valueOf(val));
		setters.put("invertStrings", val -> invertStrings = Boolean.valueOf(val));
		setters.put("lastPath", val -> lastPath = val);
		setters.put("markerOffset", val -> markerOffset = Integer.valueOf(val));
		setters.put("minLongNoteDistance", val -> minLongNoteDistance = Integer.valueOf(val));
		setters.put("minNoteDistance", val -> minNoteDistance = Integer.valueOf(val));
		setters.put("minTailLength", val -> minTailLength = Integer.valueOf(val));
		setters.put("musicPath", val -> musicPath = val);
		setters.put("songsPath", val -> songsPath = val);
		setters.put("windowHeight", val -> windowHeight = Integer.valueOf(val));
		setters.put("windowWidth", val -> windowWidth = Integer.valueOf(val));
		setters.put("windowPosX", val -> windowPosX = Integer.valueOf(val));
		setters.put("windowPosY", val -> windowPosY = Integer.valueOf(val));
		setters.put("zoomLvl", val -> zoomLvl = Integer.valueOf(val));
		setters.put("musicPath", val -> musicPath = val);

		read();
		save();
	}

	public static void read() {
		for (final Entry<String, String> configVal : RW.readConfig(configName).entrySet()) {
			try {
				setters.getOrDefault(configVal.getKey(), val -> {
				}).accept(configVal.getValue());
			} catch (final Exception e) {
				error("wrong config line " + configVal.getKey() + "=" + configVal.getValue(), e);
			}
		}
		changed = true;
	}

	public static void save() {
		if (!changed) {
			return;
		}

		final Map<String, String> config = new HashMap<>();
		config.put("debugLogging", debugLogging + "");
		config.put("delay", delay + "");
		config.put("invertStrings", invertStrings + "");
		config.put("lastPath", lastPath);
		config.put("markerOffset", markerOffset + "");
		config.put("minLongNoteDistance", minLongNoteDistance + "");
		config.put("minNoteDistance", minNoteDistance + "");
		config.put("minTailLength", minTailLength + "");
		config.put("musicPath", musicPath);
		config.put("songsPath", songsPath);
		config.put("windowHeight", windowHeight + "");
		config.put("windowWidth", windowWidth + "");
		config.put("windowPosX", windowPosX + "");
		config.put("windowPosY", windowPosY + "");
		config.put("zoomLvl", zoomLvl + "");

		RW.writeConfig(configName, config);
	}
}
