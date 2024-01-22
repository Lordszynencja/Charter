package log.charter.gui.panes;

import static log.charter.gui.components.TextInputSelectAllOnFocus.addSelectTextOnFocus;
import static log.charter.gui.components.TextInputWithValidation.ValueValidator.createIntValidator;

import javax.swing.JTextField;

import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.ParamsPane;
import log.charter.gui.handlers.AudioHandler;

public class StretchPane extends ParamsPane {
	private static final long serialVersionUID = -4754359602173894487L;

	private static PaneSizes getSizes() {
		final PaneSizes sizes = new PaneSizes();
		sizes.labelWidth = 55;
		sizes.width = 250;

		return sizes;
	}

	private final AudioHandler audioHandler;

	private int stretchedMusicSpeed = Config.stretchedMusicSpeed;

	public StretchPane(final AudioHandler audioHandler, final CharterFrame frame) {
		super(frame, Label.STRETCH_PANE, getSizes());

		this.audioHandler = audioHandler;

		int row = 0;
		addIntegerConfigValue(row++, 20, 70, Label.STRETCH_PANE_VALUE, stretchedMusicSpeed, 50,
				createIntValidator(10, 1000, false), val -> stretchedMusicSpeed = Integer.valueOf(val), false);
		addSelectTextOnFocus((JTextField) components.getLast());

		row++;
		addDefaultFinish(row, this::saveAndExit);
	}

	private void saveAndExit() {
		Config.stretchedMusicSpeed = stretchedMusicSpeed;
		Config.markChanged();

		audioHandler.clear();
		audioHandler.addSpeedToStretch();
	}
}
