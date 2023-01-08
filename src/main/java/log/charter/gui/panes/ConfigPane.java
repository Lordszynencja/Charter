package log.charter.gui.panes;

import static log.charter.gui.components.TextInputWithValidation.ValueValidator.createIntValidator;
import static log.charter.gui.components.TextInputWithValidation.ValueValidator.dirValidator;

import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.ParamsPane;

public final class ConfigPane extends ParamsPane {
	private static final long serialVersionUID = -3193534671039163160L;

	private static PaneSizes getSizes() {
		final PaneSizes sizes = new PaneSizes();
		sizes.lSpace = 20;
		sizes.labelWidth = 260;
		sizes.width = 600;

		return sizes;
	}

	private String musicPath = Config.musicPath;
	private String songsPath = Config.songsPath;

	private int minNoteDistance = Config.minNoteDistance;
	private int minTailLength = Config.minTailLength;
	private int delay = Config.delay;
	private int markerOffset = Config.markerOffset;
	private boolean invertStrings = Config.invertStrings;
	private boolean showChordIds = Config.showChordIds;

	public ConfigPane(final CharterFrame frame) {
		super(frame, Label.CONFIG_PANE, 11, getSizes());

		int row = 0;

		addConfigValue(row++, Label.CONFIG_MUSIC_FOLDER, musicPath, 300, dirValidator, //
				val -> musicPath = val, false);
		addConfigValue(row++, Label.CONFIG_SONGS_FOLDER, songsPath, 300, dirValidator, //
				val -> songsPath = val, false);

		addConfigValue(row++, Label.CONFIG_MINIMAL_NOTE_DISTANCE, minNoteDistance + "", 50,
				createIntValidator(1, 1000, false), val -> minNoteDistance = Integer.valueOf(val), false);
		addConfigValue(row++, Label.CONFIG_MINIMAL_TAIL_LENGTH, minTailLength + "", 50,
				createIntValidator(1, 1000, false), //
				val -> minTailLength = Integer.valueOf(val), false);
		addConfigValue(row++, Label.CONFIG_SOUND_DELAY, delay + "", 50, createIntValidator(1, 10000, false), //
				val -> delay = Integer.valueOf(val), false);
		addConfigValue(row++, Label.CONFIG_MARKER_POSITION, markerOffset + "", 50, createIntValidator(1, 1000, false), //
				val -> markerOffset = Integer.valueOf(val), false);
		addConfigCheckbox(row++, Label.CONFIG_INVERT_STRINGS, invertStrings, val -> invertStrings = val);
		addConfigCheckbox(row++, Label.CONFIG_SHOW_CHORD_IDS, showChordIds, val -> showChordIds = val);

		row++;
		addDefaultFinish(row, this::saveAndExit);
	}

	private void saveAndExit() {
		Config.musicPath = musicPath;
		Config.songsPath = songsPath;

		Config.minNoteDistance = minNoteDistance;
		Config.minTailLength = minTailLength;
		Config.delay = delay;
		Config.markerOffset = markerOffset;
		Config.invertStrings = invertStrings;
		Config.showChordIds = showChordIds;

		Config.markChanged();
		Config.save();
	}
}
