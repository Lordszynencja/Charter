package log.charter.gui;

import log.charter.data.Config;

public final class ConfigPane extends ParamsPane {
	private static final long serialVersionUID = -3193534671039163160L;;

	private String musicPath = Config.musicPath;
	private String songsPath = Config.songsPath;
	private String charter = Config.charter;

	private int minNoteDistance = Config.minNoteDistance;
	private int minLongNoteDistance = Config.minLongNoteDistance;
	private int minTailLength = Config.minTailLength;
	private int delay = Config.delay;
	private int markerOffset = Config.markerOffset;

	public ConfigPane(final CharterFrame frame) {
		super(frame, "Config", 10);

		addConfigValue(0, "Music folder", musicPath, 300, dirValidator, //
				val -> musicPath = val, false);
		addConfigValue(1, "Songs folder", songsPath, 300, dirValidator, //
				val -> songsPath = val, false);
		addConfigValue(2, "Default charter", charter, 300, null, //
				val -> charter = val, true);

		addConfigValue(3, "Minimal note distance", minNoteDistance + "", 50, createIntValidator(1, 1000, false),
				val -> minNoteDistance = Integer.valueOf(val), false);
		addConfigValue(4, "Minimal distance between tail and next note", minLongNoteDistance + "", 50, //
				createIntValidator(1, 1000, false),
				val -> minLongNoteDistance = Integer.valueOf(val), false);
		addConfigValue(5, "Minimal note tail length", minTailLength + "", 50, createIntValidator(1, 1000, false), //
				val -> minTailLength = Integer.valueOf(val), false);
		addConfigValue(6, "Sound delay", delay + "", 50, createIntValidator(1, 10000, false), //
				val -> delay = Integer.valueOf(val), false);
		addConfigValue(7, "Marker position", markerOffset + "", 50, createIntValidator(1, 1000, false), //
				val -> markerOffset = Integer.valueOf(val), false);

		addButtons(9, e -> {
			Config.musicPath = musicPath;
			Config.songsPath = songsPath;
			Config.charter = charter;
			Config.minNoteDistance = minNoteDistance;
			Config.minLongNoteDistance = minLongNoteDistance;
			Config.minTailLength = minTailLength;
			Config.delay = delay;
			Config.markerOffset = markerOffset;

			Config.save();
			dispose();
		});

		validate();
		setVisible(true);
	}
}
