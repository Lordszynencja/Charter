package log.charter.gui.components.containers;

import static log.charter.data.config.GraphicalConfig.inputSize;
import static log.charter.gui.components.utils.ComponentUtils.setDefaultFontSize;
import static log.charter.util.FileUtils.cleanFileName;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import log.charter.data.config.Localization.Label;
import log.charter.data.config.values.PathsConfig;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.simple.FieldWithLabel;
import log.charter.gui.components.simple.FieldWithLabel.LabelPosition;
import log.charter.gui.components.utils.RowedPosition;
import log.charter.util.FileChooseUtils;
import log.charter.util.collections.Pair;

public class SongFolderSelectPane extends RowedDialog {
	public enum FolderSelectedType {
		AUDIO, CHARTS_DIR, OTHER_FOLDER, GP, XML_FILE
	}

	private static final long serialVersionUID = 8709914106065183780L;

	private final String audioDir;
	private final String audioFolder;
	private final String defaultFolderName;
	private final String gpDir;
	private final String gpFolder;
	private final String xmlDir;
	private final String xmlFolder;

	private final JTextField folderPathInput;
	private final JTextField folderNameInput;
	private final JButton pathSelectButton;

	private FolderSelectedType folderType;

	private void enableInputsAndSetValues() {
		final String path = switch (folderType) {
			case AUDIO -> audioDir;
			case GP -> gpDir;
			case XML_FILE -> xmlDir;
			default -> PathsConfig.songsPath;
		};
		folderPathInput.setText(path);
		folderPathInput.setEnabled(folderType == FolderSelectedType.OTHER_FOLDER);

		final String folder = switch (folderType) {
			case AUDIO -> audioFolder;
			case GP -> gpFolder;
			case XML_FILE -> xmlFolder;
			default -> defaultFolderName;
		};
		folderNameInput.setText(folder);
		folderNameInput.setEnabled(
				folderType == FolderSelectedType.CHARTS_DIR || folderType == FolderSelectedType.OTHER_FOLDER);

		pathSelectButton.setVisible(folderType == FolderSelectedType.OTHER_FOLDER);
	}

	public SongFolderSelectPane(final CharterFrame charterFrame, final File audioFolderPath,
			final String defaultFolderName, final String gpFolderPath, final File xmlFolderPath,
			final FolderSelectedType defaultType) {
		super(charterFrame, Label.SONG_FOLDER_SELECT);

		audioDir = audioFolderPath.getParentFile().getAbsolutePath();
		audioFolder = audioFolderPath.getName();
		this.defaultFolderName = defaultFolderName;

		if (gpFolderPath != null) {
			final File gpFolderFile = new File(gpFolderPath);
			gpDir = gpFolderFile.getParentFile().getAbsolutePath();
			gpFolder = gpFolderFile.getName();
		} else {
			gpDir = null;
			gpFolder = null;
		}
		if (xmlFolderPath != null) {
			xmlDir = xmlFolderPath.getParentFile().getAbsolutePath();
			xmlFolder = xmlFolderPath.getName();
		} else {
			xmlDir = null;
			xmlFolder = null;
		}

		folderPathInput = new JTextField(400);
		folderNameInput = new JTextField(200);

		final RowedPosition position = new RowedPosition(inputSize * 3 / 2, panel.sizes);
		final Map<FolderSelectedType, JRadioButton> buttonsPerType = addButtons(position);

		this.add(folderNameInput);
		setDefaultFontSize(folderPathInput);
		panel.addWithSettingSize(folderPathInput, position, 400);
		position.newRow();

		setDefaultFontSize(folderNameInput);
		panel.addWithSettingSize(folderNameInput, position, 200);
		pathSelectButton = addFolderSelectButton(position);
		position.newRow();
		position.newRow();

		if (buttonsPerType.containsKey(defaultType)) {
			buttonsPerType.get(defaultType).setSelected(true);
		} else {
			buttonsPerType.get(FolderSelectedType.CHARTS_DIR).setSelected(true);
		}

		addDefaultFinish(position.y(), null, SaverWithStatus.defaultFor(this::cancel), true);
	}

	private Map<FolderSelectedType, JRadioButton> addButtons(final RowedPosition position) {
		final Map<FolderSelectedType, JRadioButton> buttonsPerType = new HashMap<>();
		final ButtonGroup group = new ButtonGroup();

		final List<Pair<Label, FolderSelectedType>> buttonData = new ArrayList<>();
		buttonData.add(new Pair<>(Label.SONG_FOLDER_AUDIO_FOLDER, FolderSelectedType.AUDIO));
		if (gpDir != null) {
			buttonData.add(new Pair<>(Label.SONG_FOLDER_GP_FOLDER, FolderSelectedType.GP));
		}
		if (xmlDir != null) {
			buttonData.add(new Pair<>(Label.SONG_FOLDER_XML_FOLDER, FolderSelectedType.XML_FILE));
		}
		buttonData.add(new Pair<>(Label.SONG_FOLDER_IN_CHARTS_DIR, FolderSelectedType.CHARTS_DIR));
		buttonData.add(new Pair<>(Label.SONG_FOLDER_OTHER, FolderSelectedType.OTHER_FOLDER));
		for (final Pair<Label, FolderSelectedType> data : buttonData) {
			final FieldWithLabel<JRadioButton> field = addOption(position, group, data.a, data.b);
			buttonsPerType.put(data.b, field.field);
		}

		return buttonsPerType;
	}

	private FieldWithLabel<JRadioButton> addOption(final RowedPosition position, final ButtonGroup group,
			final Label label, final FolderSelectedType type) {
		final JRadioButton radioButton = new JRadioButton();
		radioButton.setSelected(false);
		radioButton.addChangeListener(e -> {
			if (radioButton.isSelected()) {
				folderType = type;
				enableInputsAndSetValues();
			}
		});
		group.add(radioButton);
		final FieldWithLabel<JRadioButton> audioFolderOption = new FieldWithLabel<>(label, 0, inputSize, inputSize,
				radioButton, LabelPosition.RIGHT_CLOSE);

		panel.add(audioFolderOption, position);
		position.newRow();

		return audioFolderOption;
	}

	private void selectFolder() {
		File currentFolder = getFolder();
		if (currentFolder == null) {
			currentFolder = new File(PathsConfig.songsPath);
		}
		final File newFolder = FileChooseUtils.chooseDirectory(panel, currentFolder.getAbsolutePath());
		if (newFolder == null) {
			return;
		}

		folderPathInput.setText(newFolder.getParentFile().getAbsolutePath());
		folderNameInput.setText(newFolder.getName());
	}

	private JButton addFolderSelectButton(final RowedPosition position) {
		final JButton button = new JButton(Label.SELECT_FOLDER.label());
		button.setSize(inputSize * 5, inputSize);
		setDefaultFontSize(button);
		button.addActionListener(e -> selectFolder());
		panel.add(button, position);

		return button;
	}

	public File getFolder() {
		final String path = folderPathInput.getText();
		final String folder = folderNameInput.getText();
		if (folderType == null || path == null || path.isBlank() || folder == null || folder.isBlank()) {
			return null;
		}

		return new File(path, cleanFileName(folder));
	}

	public FolderSelectedType getFolderType() {
		return folderType;
	}

	private void cancel() {
		folderType = null;
	}
}
