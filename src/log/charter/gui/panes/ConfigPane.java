package log.charter.gui.panes;

import static log.charter.gui.components.TextInputWithValidation.ValueValidator.createIntValidator;
import static log.charter.gui.components.TextInputWithValidation.ValueValidator.dirValidator;

import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.KeyStroke;

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
	private int minLongNoteDistance = Config.minLongNoteDistance;
	private int minTailLength = Config.minTailLength;
	private int delay = Config.delay;
	private int markerOffset = Config.markerOffset;
	private boolean invertStrings = Config.invertStrings;

	public ConfigPane(final CharterFrame frame) {
		super(frame, Label.CONFIG_PANE.label(), 10, getSizes());

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

		addButtons(9, this::saveAndExit);

		getRootPane().registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		getRootPane().registerKeyboardAction(e -> saveAndExit(), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);

		validate();
		setVisible(true);
	}

	private void saveAndExit() {
		dispose();

		Config.delay = delay;
		Config.invertStrings = invertStrings;
		Config.markerOffset = markerOffset;
		Config.minLongNoteDistance = minLongNoteDistance;
		Config.minNoteDistance = minNoteDistance;
		Config.minTailLength = minTailLength;
		Config.musicPath = musicPath;
		Config.songsPath = songsPath;

		Config.save();
	}
}
