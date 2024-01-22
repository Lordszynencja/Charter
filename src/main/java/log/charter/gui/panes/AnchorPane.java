package log.charter.gui.panes;

import static log.charter.gui.components.TextInputSelectAllOnFocus.addSelectTextOnFocus;
import static log.charter.gui.components.TextInputWithValidation.ValueValidator.createIntValidator;

import javax.swing.JTextField;

import log.charter.data.ChartData;
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

	private final ChartData data;
	private final UndoSystem undoSystem;

	private final Anchor anchor;

	private Integer fret;
	private int width;

	public AnchorPane(final ChartData data, final CharterFrame frame, final UndoSystem undoSystem, final Anchor anchor,
			final Runnable onCancel) {
		super(frame, Label.ANCHOR_PANE, getSizes());
		this.data = data;
		this.undoSystem = undoSystem;

		this.anchor = anchor;

		fret = anchor.fret;
		width = anchor.width;

		int row = 0;
		addIntegerConfigValue(row++, 20, 100, Label.FRET, fret, 30, createIntValidator(1, Config.frets, true),
				val -> fret = val, false);
		final JTextField input = (JTextField) components.getLast();
		input.setHorizontalAlignment(JTextField.CENTER);
		addSelectTextOnFocus(input);

		addIntegerConfigValue(row++, 20, 100, Label.ANCHOR_WIDTH, width, 30, createIntValidator(1, Config.frets, false),
				val -> width = val, false);
		final JTextField AnchorWidthInput = (JTextField) components.getLast();
		AnchorWidthInput.setHorizontalAlignment(JTextField.CENTER);
		addSelectTextOnFocus(AnchorWidthInput);

		addDefaultFinish(row, this::saveAndExit, onCancel);
	}

	private void saveAndExit() {
		undoSystem.addUndo();

		if (fret == null) {
			data.getCurrentArrangementLevel().anchors.remove(anchor);
			return;
		}

		anchor.fret = fret;
		anchor.width = width;
	}
}
