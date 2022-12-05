package log.charter.data;

import static log.charter.io.Logger.error;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import log.charter.util.RW;

public class Config {
	private static final String configName = "config.ini";

	public static String lastPath = "C:/";
	public static String musicPath = System.getProperty("user.home") + "/Music";
	public static String songsPath = System.getProperty("user.home") + "/Documents";
	public static String charter = "Jurgen Gunterschwarzhaffenstrasen";

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
	public static double lastMaxHOPODist = 100;

	public static boolean debugLogging = false;

	static {
		read();
		save();
	}

	public static void read() {
		for (final Entry<String, String> configVal : RW.readConfig(configName).entrySet()) {
			try {
				final String val = configVal.getValue();
				switch (configVal.getKey()) {
				case "lastPath":
					lastPath = val;
					break;
				case "musicPath":
					musicPath = val;
					break;
				case "charter":
					charter = val;
					break;
				case "songsPath":
					songsPath = val;
					break;
				case "windowPosX":
					windowPosX = Integer.valueOf(val);
					break;
				case "windowPosY":
					windowPosY = Integer.valueOf(val);
					break;
				case "windowWidth":
					windowWidth = Integer.valueOf(val);
					break;
				case "windowHeight":
					windowHeight = Integer.valueOf(val);
					break;
				case "zoomLvl":
					zoomLvl = Integer.valueOf(val);
					break;
				case "minNoteDistance":
					minNoteDistance = Integer.valueOf(val);
					break;
				case "minLongNoteDistance":
					minLongNoteDistance = Integer.valueOf(val);
					break;
				case "minTailLength":
					minTailLength = Integer.valueOf(val);
					break;
				case "delay":
					delay = Integer.valueOf(val);
					break;
				case "markerOffset":
					markerOffset = Integer.valueOf(val);
					break;
				case "lastMaxHOPODist":
					lastMaxHOPODist = Double.valueOf(val);
					break;
				case "debugLogging":
					debugLogging = Boolean.valueOf(val);
					break;
				default:
					error("Unknown config line " + configVal.getKey() + "=" + configVal.getValue());
					break;
				}
			} catch (final Exception e) {
				error("wrong config line " + configVal.getKey() + "=" + configVal.getValue(), e);
			}
		}
	}

	public static void save() {
		final Map<String, String> config = new HashMap<>();
		config.put("lastPath", lastPath);
		config.put("musicPath", musicPath);
		config.put("songsPath", songsPath);
		config.put("charter", charter);
		config.put("windowPosX", windowPosX + "");
		config.put("windowPosY", windowPosY + "");
		config.put("windowWidth", windowWidth + "");
		config.put("windowHeight", windowHeight + "");
		config.put("zoomLvl", zoomLvl + "");
		config.put("minNoteDistance", minNoteDistance + "");
		config.put("minLongNoteDistance", minLongNoteDistance + "");
		config.put("minTailLength", minTailLength + "");
		config.put("delay", delay + "");
		config.put("markerOffset", markerOffset + "");
		config.put("lastMaxHOPODist", lastMaxHOPODist + "");
		config.put("debugLogging", debugLogging + "");

		RW.writeConfig(configName, config);
	}
}
