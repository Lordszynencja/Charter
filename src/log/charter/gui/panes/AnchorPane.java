package log.charter.gui.panes;

import static log.charter.gui.components.TextInputSelectAllOnFocus.addSelectTextOnFocus;
import static log.charter.gui.components.TextInputWithValidation.ValueValidator.createIntValidator;

import javax.swing.JTextField;

import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.ParamsPane;
import log.charter.song.Anchor;

public class AnchorPane extends ParamsPane {
	private static final long serialVersionUID = -4754359602173894487L;

	private static PaneSizes getSizes() {
		final PaneSizes sizes = new PaneSizes();
		sizes.labelWidth = 80;
		sizes.width = 250;

		return sizes;
	}

	private final UndoSystem undoSystem;

	private final Anchor anchor;

	private int fret;

	public AnchorPane(final CharterFrame frame, final UndoSystem undoSystem, final Anchor anchor) {
		super(frame, Label.ANCHOR_PANE.label(), 3, getSizes());
		this.undoSystem = undoSystem;

		this.anchor = anchor;

		fret = anchor.fret;

		addIntegerConfigValue(0, 80, 0, Label.FRET, fret, 30, createIntValidator(1, Config.frets, false),
				val -> fret = val, false);
		final JTextField input = (JTextField) components.getLast();
		input.setHorizontalAlignment(JTextField.CENTER);
		addSelectTextOnFocus(input);

		addDefaultFinish(2, this::saveAndExit);
	}

	private void saveAndExit() {
		undoSystem.addUndo();

		anchor.fret = fret;
	}
}
