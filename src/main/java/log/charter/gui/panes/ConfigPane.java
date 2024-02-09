package log.charter.gui.panes;

import static log.charter.gui.components.TextInputWithValidation.ValueValidator.createIntValidator;
import static log.charter.gui.components.TextInputWithValidation.ValueValidator.dirValidator;

import java.io.File;

import javax.swing.JButton;
import javax.swing.JTextField;

import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.Framer;
import log.charter.gui.components.ParamsPane;
import log.charter.util.FileChooseUtils;

public final class ConfigPane extends ParamsPane {
	private static final long serialVersionUID = -3193534671039163160L;

	private static PaneSizes getSizes() {
		final PaneSizes sizes = new PaneSizes();
		sizes.lSpace = 20;
		sizes.labelWidth = 300;
		sizes.width = 600;

		return sizes;
	}

	private final CharterFrame frame;
	private final Framer framer;

	private final JTextField musicFolderInput;
	private final JTextField songsFolderInput;

	private String musicPath = Config.musicPath;
	private String songsPath = Config.songsPath;

	private int minNoteDistance = Config.minNoteDistance;
	private int minTailLength = Config.minTailLength;
	private int delay = Config.delay;
	private int midiDelay = Config.midiDelay;
	private int markerOffset = Config.markerOffset;
	private boolean invertStrings = Config.invertStrings;
	private boolean leftHanded = Config.leftHanded;
	private boolean showChordIds = Config.showChordIds;
	private boolean showGrid = Config.showGrid;
	private boolean createDefaultStretchesInBackground = Config.createDefaultStretchesInBackground;
	private int FPS = Config.FPS;
	private int backupDelay = Config.backupDelay;

	public ConfigPane(final CharterFrame frame, final Framer framer) {
		super(frame, Label.CONFIG_PANE, getSizes());
		this.frame = frame;
		this.framer = framer;

		int row = 0;

		addConfigValue(row, 20, 150, Label.CONFIG_MUSIC_FOLDER, musicPath, 300, dirValidator, //
				val -> musicPath = val, false);
		musicFolderInput = (JTextField) components.getLast();
		final JButton musicFolderPickerButton = new JButton(Label.SELECT_FOLDER.label());
		musicFolderPickerButton.addActionListener(e -> selectMusicFolder());
		this.add(musicFolderPickerButton, 480, getY(row++), 100, 20);

		addConfigValue(row, 20, 150, Label.CONFIG_SONGS_FOLDER, songsPath, 300, dirValidator, //
				val -> songsPath = val, false);
		songsFolderInput = (JTextField) components.getLast();
		final JButton songsFolderPickerButton = new JButton(Label.SELECT_FOLDER.label());
		songsFolderPickerButton.addActionListener(e -> selectSongsFolder());
		this.add(songsFolderPickerButton, 480, getY(row++), 100, 20);

		addConfigValue(row++, 20, 0, Label.CONFIG_MINIMAL_NOTE_DISTANCE, minNoteDistance + "", 50,
				createIntValidator(1, 1000, false), val -> minNoteDistance = Integer.valueOf(val), false);
		addConfigValue(row++, 20, 0, Label.CONFIG_MINIMAL_TAIL_LENGTH, minTailLength + "", 50,
				createIntValidator(1, 1000, false), //
				val -> minTailLength = Integer.valueOf(val), false);
		addConfigValue(row++, 20, 0, Label.CONFIG_SOUND_DELAY, delay + "", 50, createIntValidator(1, 10000, false), //
				val -> delay = Integer.valueOf(val), false);
		addConfigValue(row++, 20, 0, Label.CONFIG_MIDI_DELAY, midiDelay + "", 50, createIntValidator(1, 10000, false), //
				val -> midiDelay = Integer.valueOf(val), false);
		addConfigValue(row++, 20, 0, Label.CONFIG_MARKER_POSITION, markerOffset + "", 50,
				createIntValidator(1, 1000, false), //
				val -> markerOffset = Integer.valueOf(val), false);
		addConfigCheckbox(row, 20, 0, Label.CONFIG_INVERT_STRINGS, invertStrings, val -> invertStrings = val);
		addConfigCheckbox(row++, 180, 0, Label.CONFIG_LEFT_HANDED, leftHanded, val -> leftHanded = val);
		addConfigCheckbox(row++, 20, 0, Label.CONFIG_SHOW_CHORD_IDS, showChordIds, val -> showChordIds = val);
		addConfigCheckbox(row++, 20, 0, Label.CONFIG_SHOW_GRID, showGrid, val -> showGrid = val);
		addConfigCheckbox(row++, 20, 0, Label.CONFIG_CREATE_DEFAULT_STRETCHES_IN_BACKGROUND,
				createDefaultStretchesInBackground, val -> createDefaultStretchesInBackground = val);
		addConfigValue(row++, 20, 0, Label.CONFIG_FPS, FPS + "", 50, createIntValidator(1, 1000, false), //
				val -> FPS = Integer.valueOf(val), false);
		addConfigValue(row++, 20, 0, Label.CONFIG_BACKUP_DELAY, backupDelay + "", 50,
				createIntValidator(1, 3600, false), //
				val -> backupDelay = Integer.valueOf(val), false);

		row++;
		addDefaultFinish(row, this::saveAndExit);
	}

	private void selectMusicFolder() {
		final File newMusicDir = FileChooseUtils.chooseDirectory(this, musicPath);
		if (newMusicDir == null) {
			return;
		}

		musicFolderInput.setText(newMusicDir.getAbsolutePath());
	}

	private void selectSongsFolder() {
		final File newMusicDir = FileChooseUtils.chooseDirectory(this, songsPath);
		if (newMusicDir == null) {
			return;
		}

		songsFolderInput.setText(newMusicDir.getAbsolutePath());
	}

	private void saveAndExit() {
		Config.musicPath = musicPath;
		Config.songsPath = songsPath;

		Config.minNoteDistance = minNoteDistance;
		Config.minTailLength = minTailLength;
		Config.delay = delay;
		Config.midiDelay = midiDelay;
		Config.markerOffset = markerOffset;

		Config.invertStrings = invertStrings;
		Config.leftHanded = leftHanded;
		Config.showChordIds = showChordIds;
		Config.showGrid = showGrid;

		Config.FPS = FPS;

		Config.markChanged();
		Config.save();

		frame.updateEditAreaSizes();
		frame.resize();

		framer.setFPS(FPS);
	}
}
