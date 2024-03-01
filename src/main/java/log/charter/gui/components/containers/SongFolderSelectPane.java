package log.charter.gui.components.containers;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.simple.FieldWithLabel;
import log.charter.gui.components.simple.FieldWithLabel.LabelPosition;

public class SongFolderSelectPane extends ParamsPane {
	private static final long serialVersionUID = 8709914106065183780L;

	private boolean audioFolderChosen = Config.audioFolderChosenForNewSong;
	private String folderName;

	public SongFolderSelectPane(final CharterFrame frame, final String songsDirectory, final String audioFolder,
			final String defaultFolderName) {
		super(frame, Label.SONG_FOLDER_SELECT, 700);

		final ButtonGroup group = new ButtonGroup();
		final JTextField folderNameInput = new JTextField(200);

		final String audioFolderOptionLabel = String.format(Label.SONG_FOLDER_AUDIO_FILE_FOLDER.label(), audioFolder);
		final JRadioButton audioFolderOptionButton = new JRadioButton();
		audioFolderOptionButton.setSelected(audioFolderChosen);
		audioFolderOptionButton.addChangeListener(e -> {
			if (audioFolderOptionButton.isSelected()) {
				folderNameInput.setEnabled(false);
			}
		});
		group.add(audioFolderOptionButton);
		final FieldWithLabel<JRadioButton> audioFolderOption = new FieldWithLabel<>(audioFolderOptionLabel, 500, 20, 20,
				audioFolderOptionButton, LabelPosition.RIGHT_CLOSE);
		audioFolderOption.setLocation(20, getY(0));
		this.add(audioFolderOption);

		final String newFolderOptionLabel = String.format(Label.SONG_FOLDER_CREATE_NEW.label(), songsDirectory);
		final JRadioButton newFolderOptionButton = new JRadioButton();
		newFolderOptionButton.setSelected(!audioFolderChosen);
		newFolderOptionButton.addChangeListener(e -> {
			if (newFolderOptionButton.isSelected()) {
				folderNameInput.setEnabled(true);
			}
		});
		group.add(newFolderOptionButton);
		final FieldWithLabel<JRadioButton> newFolderOption = new FieldWithLabel<>(newFolderOptionLabel, 500, 20, 20,
				newFolderOptionButton, LabelPosition.RIGHT_CLOSE);
		newFolderOption.setLocation(20, getY(1));
		this.add(newFolderOption);

		folderNameInput.setText(defaultFolderName);
		folderNameInput.setLocation(40, getY(2));
		folderNameInput.setSize(500, 20);
		this.add(folderNameInput);

		addDefaultFinish(4, () -> {
			audioFolderChosen = audioFolderOptionButton.isSelected();
			folderName = folderNameInput.getText();

			return true;
		}, () -> {
			audioFolderChosen = false;
			folderName = null;

			return true;
		});
	}

	public boolean isAudioFolderChosen() {
		return audioFolderChosen;
	}

	public String getFolderName() {
		return folderName;
	}
}
