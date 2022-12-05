package log.charter.io;

import java.util.ArrayList;
import java.util.List;

import log.charter.song.IniData;
import log.charter.util.RW;

public class IniWriter {
	public static void write(final String path, final IniData iniData) {
		final IniWriter writer = new IniWriter(iniData.otherValues.size() + 20);
		writer.addPair("name", iniData.name);
		writer.addPair("artist", iniData.artist);
		writer.addPair("album", iniData.album);
		if (iniData.track != null) {
			writer.addPair("track", iniData.track);
		}
		if (iniData.year != null) {
			writer.addPair("year", iniData.year);
		}
		writer.addPair("genre", iniData.genre);
		writer.addPair("loading_phrase", iniData.loadingPhrase);
		writer.addPair("frets", iniData.charter);
		writer.addPair("charter", iniData.charter);
		writer.addPair("diff_guitar", iniData.diffG);
		writer.addPair("diff_guitar_coop", iniData.diffGC);
		writer.addPair("diff_rhythm", iniData.diffGR);
		writer.addPair("diff_bass", iniData.diffB);
		writer.addPair("diff_drums", iniData.diffD);
		writer.addPair("diff_keys", iniData.diffK);
		writer.addPair("sysex_open_bass", iniData.sysexOpenBass);
		writer.addPair("sysex_slider", iniData.sysexSlider);
		writer.addPair("pro_drums", iniData.proDrums);

		iniData.otherValues.forEach((n, v) -> {
			writer.addPair(n, v);
		});

		writer.write(path);
	}

	private final List<String> names;
	List<String> values;

	private IniWriter(final int listSize) {
		names = new ArrayList<>(listSize);
		values = new ArrayList<>(listSize);
	}

	private void addPair(final String name, final Object value) {
		names.add(name);
		values.add(value.toString());
	}

	private void write(final String path) {
		final StringBuilder b = new StringBuilder("[song]\r\n");

		for (int i = 0; i < names.size(); i++) {
			b.append(names.get(i) + " = " + values.get(i) + "\r\n");
		}

		RW.write(path, b.toString());
	}
}
