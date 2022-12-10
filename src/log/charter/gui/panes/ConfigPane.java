package log.charter.gui.panes;

import log.charter.data.Config;
import log.charter.gui.CharterFrame;

public final class ConfigPane extends ParamsPane {
	private static final long serialVersionUID = -3193534671039163160L;;

	private String musicPath = Config.musicPath;
	private String songsPath = Config.songsPath;

	private int minNoteDistance = Config.minNoteDistance;
	private int minLongNoteDistance = Config.minLongNoteDistance;
	private int minTailLength = Config.minTailLength;
	private int delay = Config.delay;
	private int markerOffset = Config.markerOffset;
	private boolean invertStrings = Config.invertStrings;

	public ConfigPane(final CharterFrame frame) {
		super(frame, "Config", 10);

		addConfigValue(0, "Music folder", musicPath, 300, dirValidator, //
				val -> musicPath = val, false);
		addConfigValue(1, "Songs folder", songsPath, 300, dirValidator, //
				val -> songsPath = val, false);

		addConfigValue(2, "Minimal note distance", minNoteDistance + "", 50, createIntValidator(1, 1000, false),
				val -> minNoteDistance = Integer.valueOf(val), false);
		addConfigValue(3, "Minimal distance between tail and next note", minLongNoteDistance + "", 50, //
				createIntValidator(1, 1000, false), val -> minLongNoteDistance = Integer.valueOf(val), false);
		addConfigValue(4, "Minimal note tail length", minTailLength + "", 50, createIntValidator(1, 1000, false), //
				val -> minTailLength = Integer.valueOf(val), false);
		addConfigValue(5, "Sound delay", delay + "", 50, createIntValidator(1, 10000, false), //
				val -> delay = Integer.valueOf(val), false);
		addConfigValue(6, "Marker position", markerOffset + "", 50, createIntValidator(1, 1000, false), //
				val -> markerOffset = Integer.valueOf(val), false);
		addConfigCheckbox(7, "Invert strings", invertStrings, val -> invertStrings = val);

		addButtons(9, e -> {
			Config.delay = delay;
			Config.invertStrings = invertStrings;
			Config.markerOffset = markerOffset;
			Config.minLongNoteDistance = minLongNoteDistance;
			Config.minNoteDistance = minNoteDistance;
			Config.minTailLength = minTailLength;
			Config.musicPath = musicPath;
			Config.songsPath = songsPath;

			Config.save();
			dispose();
		});

		validate();
		setVisible(true);
	}
}
