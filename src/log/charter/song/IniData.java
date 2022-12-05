package log.charter.song;

import static log.charter.io.Logger.debug;
import static log.charter.io.Logger.error;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import log.charter.io.Logger;
import log.charter.util.RW;

public class IniData {
	public String name = "";
	public String artist = "";
	public String album = "";
	public String track = "";
	public String year = "";
	public String genre = "";

	public String loadingPhrase = "";
	public String charter = "";

	public int diffG = -1;
	public int diffGC = -1;
	public int diffGR = -1;
	public int diffB = -1;
	public int diffD = -1;
	public int diffK = -1;

	public String sysexOpenBass = "True";
	public String sysexSlider = "True";
	public String proDrums = "True";

	public Map<String, String> otherValues = new HashMap<>();

	public IniData() {
	}

	public IniData(final File f) {
		debug("Reading ini from " + f.getAbsolutePath());
		final String iniString = RW.read(f);

		for (final String line : iniString.split("(\r\n|\r|\r)")) {
			try {
				final int split = line.indexOf('=');
				if (split < 0) {
					Logger.debug("Skipping ini line " + line);
					continue;
				}
				final String param = line.substring(0, split).trim();
				final String value = line.substring(split + 1).trim();
				debug("name: '" + param + "', value: '" + value + "'");

				switch (param) {
				case "name":
					name = value;
					break;
				case "artist":
					artist = value;
					break;
				case "album":
					album = value;
					break;
				case "track":
					track = value;
					break;
				case "year":
					year = value;
					break;
				case "genre":
					genre = value;
					break;
				case "loading_phrase":
					loadingPhrase = value;
					break;
				case "frets":
				case "charter":
					charter = value;
					break;
				case "diff_guitar":
					diffG = Integer.valueOf(value);
					break;
				case "diff_guitar_coop":
					diffGC = Integer.valueOf(value);
					break;
				case "diff_rhythm":
					diffGR = Integer.valueOf(value);
					break;
				case "diff_bass":
					diffB = Integer.valueOf(value);
					break;
				case "diff_drums":
					diffD = Integer.valueOf(value);
					break;
				case "diff_keys":
					diffK = Integer.valueOf(value);
					break;
				case "sysex_open_bass":
					sysexSlider = value;
					break;
				case "sysex_slider":
					sysexSlider = value;
					break;
				case "pro_drums":
					proDrums = value;
					break;
				default:
					otherValues.put(param, value);
					break;
				}
			} catch (final Exception e) {
				error("Couldn't load line from ini file:" + line, e);
			}
		}

		debug("Reading ini finished");
	}

}
