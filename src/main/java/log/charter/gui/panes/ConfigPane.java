package log.charter.gui.panes;

import static log.charter.gui.components.utils.validators.ValueValidator.dirValidator;

import java.io.File;
import java.util.stream.Stream;

import javax.swing.JButton;
import javax.swing.JTextField;

import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.BeatsMap.DistanceType;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.containers.ParamsPane;
import log.charter.gui.components.simple.CharterSelect;
import log.charter.gui.components.simple.FieldWithLabel;
import log.charter.gui.components.simple.FieldWithLabel.LabelPosition;
import log.charter.gui.components.utils.validators.IntValueValidator;
import log.charter.services.utils.Framer;
import log.charter.sound.SoundFileType;
import log.charter.util.FileChooseUtils;

public final class ConfigPane extends ParamsPane {
	private static final long serialVersionUID = -3193534671039163160L;

	private final CharterFrame frame;
	private final Framer framer;

	private final JTextField musicFolderInput;
	private final JTextField songsFolderInput;

	private String musicPath = Config.musicPath;
	private String songsPath = Config.songsPath;
	private SoundFileType baseAudioFormat = Config.baseAudioFormat;

	private DistanceType minNoteDistanceType = Config.minNoteDistanceType;
	private int minNoteDistanceFactor = Config.minNoteDistanceFactor;
	private final DistanceType minTailLengthType = Config.minTailLengthType;
	private int minTailLengthFactor = Config.minTailLengthFactor;
	private int delay = Config.delay;
	private int audioBufferMs = Config.audioBufferMs;
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
		super(frame, Label.CONFIG_PANE_TITLE, 600);
		this.frame = frame;
		this.framer = framer;

		int row = 0;

		addStringConfigValue(row, 20, 150, Label.MUSIC_FOLDER, musicPath, 300, dirValidator, //
				val -> musicPath = val, false);
		musicFolderInput = (JTextField) getLastPart();
		final JButton musicFolderPickerButton = new JButton(Label.SELECT_FOLDER.label());
		musicFolderPickerButton.addActionListener(e -> selectMusicFolder());
		this.add(musicFolderPickerButton, 480, getY(row++), 100, 20);

		addStringConfigValue(row, 20, 150, Label.SONGS_FOLDER, songsPath, 300, dirValidator, //
				val -> songsPath = val, false);
		songsFolderInput = (JTextField) getLastPart();
		final JButton songsFolderPickerButton = new JButton(Label.SELECT_FOLDER.label());
		songsFolderPickerButton.addActionListener(e -> selectSongsFolder());
		this.add(songsFolderPickerButton, 480, getY(row++), 100, 20);

		addIntConfigValue(row, 20, 0, Label.MINIMAL_NOTE_DISTANCE, minNoteDistanceFactor, 50, //
				new IntValueValidator(1, 1000), v -> minNoteDistanceFactor = v, false);
		addMinNoteDistanceTypeSelect(200, row++);

		addIntConfigValue(row++, 20, 0, Label.MINIMAL_TAIL_LENGTH, minTailLengthFactor, 50,
				new IntValueValidator(1, 1000), v -> minTailLengthFactor = v, false);

		addIntConfigValue(row, 20, 0, Label.SOUND_DELAY, delay, 50, //
				new IntValueValidator(1, 10000), v -> delay = v, false);
		addIntConfigValue(row++, 220, 0, Label.BUFFER_SIZE_MS, delay, 50, //
				new IntValueValidator(1, 250), v -> audioBufferMs = v, false);

		addIntConfigValue(row, 20, 0, Label.MIDI_SOUND_DELAY, midiDelay, 50, //
				new IntValueValidator(1, 10000), v -> midiDelay = v, false);
		addBaseAudioFormatSelect(220, row++);

		addIntConfigValue(row++, 20, 0, Label.MARKER_POSITION_PX, markerOffset, 50, //
				new IntValueValidator(-10_000, 10_000), v -> markerOffset = v, false);
		addConfigCheckbox(row, 20, 0, Label.INVERT_STRINGS, invertStrings, val -> invertStrings = val);
		addConfigCheckbox(row, 180, 0, Label.INVERT_STRINGS_IN_PREVIEW, invertStrings3D, val -> invertStrings3D = val);
		addConfigCheckbox(row++, 380, 0, Label.LEFT_HANDED, leftHanded, val -> leftHanded = val);
		addConfigCheckbox(row++, 20, 0, Label.SHOW_CHORD_IDS, showChordIds, val -> showChordIds = val);
		addConfigCheckbox(row++, 20, 0, Label.SHOW_GRID, showGrid, val -> showGrid = val);
		addConfigCheckbox(row++, 20, 0, Label.CREATE_DEFAULT_STRETCHES_IN_BACKGROUND,
				createDefaultStretchesInBackground, val -> createDefaultStretchesInBackground = val);
		addIntConfigValue(row++, 20, 0, Label.FPS, FPS, 50, //
				new IntValueValidator(1, 1000), v -> FPS = v, false);
		addIntConfigValue(row++, 20, 0, Label.BACKUP_DELAY_S, backupDelay, 50, //
				new IntValueValidator(1, 3600), v -> backupDelay = v, false);

		row++;
		addDefaultFinish(row, this::saveAndExit);
	}

	private void addMinNoteDistanceTypeSelect(final int x, final int row) {
		final Stream<DistanceType> possibleValues = Stream.of(DistanceType.MILISECONDS, DistanceType.BEATS,
				DistanceType.NOTES);

		final CharterSelect<DistanceType> select = new CharterSelect<>(possibleValues, minNoteDistanceType,
				t -> t.label.label(), t -> minNoteDistanceType = t);

		add(select, x, getY(row), 200, 20);
	}

	private void addBaseAudioFormatSelect(final int x, final int row) {
		final Stream<SoundFileType> possibleValues = Stream.of(SoundFileType.values())//
				.filter(type -> type.writer != null && type.loader != null);

		final CharterSelect<SoundFileType> select = new CharterSelect<>(possibleValues, baseAudioFormat, t -> t.name,
				t -> baseAudioFormat = t);
		final FieldWithLabel<CharterSelect<SoundFileType>> field = new FieldWithLabel<>(Label.BASE_AUDIO_FORMAT, 100,
				100, 20, select, LabelPosition.LEFT);

		add(field, x, getY(row), field.getWidth(), field.getHeight());
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
		Config.baseAudioFormat = baseAudioFormat;

		Config.minNoteDistanceType = minNoteDistanceType;
		Config.minNoteDistanceFactor = minNoteDistanceFactor;
		Config.minTailLengthType = minTailLengthType;
		Config.minTailLengthFactor = minTailLengthFactor;
		Config.delay = delay;
		Config.audioBufferMs = audioBufferMs;
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
