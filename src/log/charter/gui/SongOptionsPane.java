package log.charter.gui;

import log.charter.song.IniData;

public final class SongOptionsPane extends ParamsPane {
	private static final long serialVersionUID = -3193534671039163160L;;

	private String name;
	private String artist;
	private String album;
	private String track;
	private String year;
	private String genre;

	private String loadingPhrase;
	private String charter;

	private int diffG;
	private int diffGC;
	private int diffGR;
	private int diffB;
	private int diffD;
	private int diffK;

	private boolean sysexOpenBass;
	private boolean sysexSlider;
	private boolean proDrums;

	public SongOptionsPane(final CharterFrame frame) {
		super(frame, "Options", 21);

		final IniData iniData = frame.handler.data.ini;
		name = iniData.name;
		artist = iniData.artist;
		album = iniData.album;
		track = iniData.track;
		year = iniData.year;
		genre = iniData.genre;

		loadingPhrase = iniData.loadingPhrase;
		charter = iniData.charter;

		diffG = iniData.diffG;
		diffGC = iniData.diffGC;
		diffGR = iniData.diffGR;
		diffB = iniData.diffB;
		diffD = iniData.diffD;
		diffK = iniData.diffK;

		sysexOpenBass = "True".equals(iniData.sysexOpenBass);
		sysexSlider = "True".equals(iniData.sysexSlider);
		proDrums = "True".equals(iniData.proDrums);

		addConfigValue(0, "Song name", name, 300, null, //
				val -> name = val, false);
		addConfigValue(1, "Artist", artist, 300, null, //
				val -> artist = val, false);
		addConfigValue(2, "Album", album, 300, null, //
				val -> album = val, false);
		addConfigValue(3, "Track", track, 40, createIntValidator(0, 1000, true), //
				val -> track = (val == null) || val.isEmpty() ? null : val, true);
		addConfigValue(4, "Year", year, 80, createIntValidator(Integer.MIN_VALUE, Integer.MAX_VALUE, true), //
				val -> year = (val == null) || val.isEmpty() ? null : val, true);
		addConfigValue(5, "Genre", genre, 200, null, //
				val -> genre = val, true);

		addConfigValue(7, "Loading phrase", loadingPhrase, 450, null, //
				val -> loadingPhrase = val, true);
		addConfigValue(8, "Charter", charter, 200, null, //
				val -> charter = val, true);

		addConfigValue(9, "Guitar difficulty", diffG, 40, createIntValidator(-1, 100, false), //
				val -> diffG = Integer.valueOf(val), false);
		addConfigValue(10, "Coop guitar difficulty", diffGC, 40, createIntValidator(-1, 100, false), //
				val -> diffGC = Integer.valueOf(val), false);
		addConfigValue(11, "Rhytm guitar difficulty", diffGR, 40, createIntValidator(-1, 100, false), //
				val -> diffGR = Integer.valueOf(val), false);
		addConfigValue(12, "Bass difficulty", diffB, 40, createIntValidator(-1, 100, false), //
				val -> diffB = Integer.valueOf(val), false);
		addConfigValue(13, "Drums difficulty", diffD, 40, createIntValidator(-1, 100, false), //
				val -> diffD = Integer.valueOf(val), false);
		addConfigValue(14, "Keyboard difficulty", diffK, 40, createIntValidator(-1, 100, false), //
				val -> diffK = Integer.valueOf(val), false);

		addConfigCheckbox(16, "sysex_open_bass", sysexOpenBass, val -> sysexOpenBass = val);
		addConfigCheckbox(17, "sysex_slider", sysexSlider, val -> sysexSlider = val);
		addConfigCheckbox(18, "pro_drums", proDrums, val -> proDrums = val);

		addButtons(20, e -> {
			iniData.name = name;
			iniData.artist = artist;
			iniData.album = album;
			iniData.track = track;
			iniData.year = year;
			iniData.genre = genre;

			iniData.loadingPhrase = loadingPhrase;
			iniData.charter = charter;

			iniData.diffG = diffG;
			iniData.diffGC = diffGC;
			iniData.diffGR = diffGR;
			iniData.diffB = diffB;
			iniData.diffD = diffD;
			iniData.diffK = diffK;

			iniData.sysexOpenBass = sysexOpenBass ? "True" : "False";
			iniData.sysexSlider = sysexSlider ? "True" : "False";
			iniData.proDrums = proDrums ? "True" : "False";

			frame.handler.songFileHandler.save();

			dispose();
		});

		validate();
		setVisible(true);
	}
}
