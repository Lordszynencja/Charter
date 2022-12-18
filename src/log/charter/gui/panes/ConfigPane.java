package log.charter.gui.panes;

import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
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
		super(frame, Label.CONFIG_PANE.label(), 10);

		addConfigValue(0, Label.CONFIG_MUSIC_FOLDER, musicPath, 300, dirValidator, //
				val -> musicPath = val, false);
		addConfigValue(1, Label.CONFIG_SONGS_FOLDER, songsPath, 300, dirValidator, //
				val -> songsPath = val, false);

		addConfigValue(2, Label.CONFIG_MINIMAL_NOTE_DISTANCE, minNoteDistance + "", 50,
				createIntValidator(1, 1000, false), val -> minNoteDistance = Integer.valueOf(val), false);
		addConfigValue(3, Label.CONFIG_MINIMAL_TAIL_TO_NOTE_DISTANCE, minLongNoteDistance + "", 50, //
				createIntValidator(1, 1000, false), val -> minLongNoteDistance = Integer.valueOf(val), false);
		addConfigValue(4, Label.CONFIG_MINIMAL_TAIL_LENGTH, minTailLength + "", 50, createIntValidator(1, 1000, false), //
				val -> minTailLength = Integer.valueOf(val), false);
		addConfigValue(5, Label.CONFIG_SOUND_DELAY, delay + "", 50, createIntValidator(1, 10000, false), //
				val -> delay = Integer.valueOf(val), false);
		addConfigValue(6, Label.CONFIG_MARKER_POSITION, markerOffset + "", 50, createIntValidator(1, 1000, false), //
				val -> markerOffset = Integer.valueOf(val), false);
		addConfigCheckbox(7, Label.CONFIG_INVERT_STRINGS, invertStrings, val -> invertStrings = val);

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
