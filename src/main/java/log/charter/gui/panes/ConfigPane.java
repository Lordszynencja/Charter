package log.charter.gui.panes;

import static log.charter.gui.components.utils.validators.ValueValidator.dirValidator;

import java.io.File;

import javax.swing.JButton;
import javax.swing.JTextField;

import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.containers.ParamsPane;
import log.charter.gui.components.utils.validators.IntValueValidator;
import log.charter.gui.utils.Framer;
import log.charter.util.FileChooseUtils;

public final class ConfigPane extends ParamsPane {
	private static final long serialVersionUID = -3193534671039163160L;

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
	private boolean invertStrings3D = Config.invertStrings3D;
	private boolean leftHanded = Config.leftHanded;
	private boolean showChordIds = Config.showChordIds;
	private boolean showGrid = Config.showGrid;
	private boolean createDefaultStretchesInBackground = Config.createDefaultStretchesInBackground;
	private int FPS = Config.FPS;
	private int backupDelay = Config.backupDelay;

	public ConfigPane(final CharterFrame frame, final Framer framer) {
		super(frame, Label.CONFIG_PANE, 600);
		this.frame = frame;
		this.framer = framer;

		int row = 0;

		addStringConfigValue(row, 20, 150, Label.CONFIG_MUSIC_FOLDER, musicPath, 300, dirValidator, //
				val -> musicPath = val, false);
		musicFolderInput = (JTextField) getLastPart();
		final JButton musicFolderPickerButton = new JButton(Label.SELECT_FOLDER.label());
		musicFolderPickerButton.addActionListener(e -> selectMusicFolder());
		this.add(musicFolderPickerButton, 480, getY(row++), 100, 20);

		addStringConfigValue(row, 20, 150, Label.CONFIG_SONGS_FOLDER, songsPath, 300, dirValidator, //
				val -> songsPath = val, false);
		songsFolderInput = (JTextField) getLastPart();
		final JButton songsFolderPickerButton = new JButton(Label.SELECT_FOLDER.label());
		songsFolderPickerButton.addActionListener(e -> selectSongsFolder());
		this.add(songsFolderPickerButton, 480, getY(row++), 100, 20);

		addIntConfigValue(row++, 20, 0, Label.CONFIG_MINIMAL_NOTE_DISTANCE, minNoteDistance, 50, //
				new IntValueValidator(1, 1000), v -> minNoteDistance = v, false);
		addIntConfigValue(row++, 20, 0, Label.CONFIG_MINIMAL_TAIL_LENGTH, minTailLength, 50,
				new IntValueValidator(1, 1000), v -> minTailLength = v, false);
		addIntConfigValue(row++, 20, 0, Label.CONFIG_SOUND_DELAY, delay, 50, //
				new IntValueValidator(1, 10000), v -> delay = v, false);
		addIntConfigValue(row++, 20, 0, Label.CONFIG_MIDI_DELAY, midiDelay, 50, //
				new IntValueValidator(1, 10000), v -> midiDelay = v, false);
		addIntConfigValue(row++, 20, 0, Label.CONFIG_MARKER_POSITION, markerOffset, 50, //
				new IntValueValidator(-10_000, 10_000), v -> markerOffset = v, false);
		addConfigCheckbox(row, 20, 0, Label.CONFIG_INVERT_STRINGS, invertStrings, val -> invertStrings = val);
		addConfigCheckbox(row, 180, 0, Label.CONFIG_INVERT_STRINGS_IN_PREVIEW, invertStrings3D,
				val -> invertStrings3D = val);
		addConfigCheckbox(row++, 380, 0, Label.CONFIG_LEFT_HANDED, leftHanded, val -> leftHanded = val);
		addConfigCheckbox(row++, 20, 0, Label.CONFIG_SHOW_CHORD_IDS, showChordIds, val -> showChordIds = val);
		addConfigCheckbox(row++, 20, 0, Label.CONFIG_SHOW_GRID, showGrid, val -> showGrid = val);
		addConfigCheckbox(row++, 20, 0, Label.CONFIG_CREATE_DEFAULT_STRETCHES_IN_BACKGROUND,
				createDefaultStretchesInBackground, val -> createDefaultStretchesInBackground = val);
		addIntConfigValue(row++, 20, 0, Label.CONFIG_FPS, FPS, 50, //
				new IntValueValidator(1, 1000), v -> FPS = v, false);
		addIntConfigValue(row++, 20, 0, Label.CONFIG_BACKUP_DELAY, backupDelay, 50, //
				new IntValueValidator(1, 3600), v -> backupDelay = v, false);

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
		Config.invertStrings3D = invertStrings3D;
		Config.leftHanded = leftHanded;
		Config.showChordIds = showChordIds;
		Config.showGrid = showGrid;

		Config.FPS = FPS;

		Config.markChanged();
		Config.save();

		frame.updateSizes();
		frame.resize();

		framer.setFPS(FPS);
	}
}
