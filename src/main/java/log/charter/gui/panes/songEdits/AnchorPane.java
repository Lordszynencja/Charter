package log.charter.gui.panes.songEdits;

import static log.charter.gui.components.utils.TextInputSelectAllOnFocus.addSelectTextOnFocus;

import javax.swing.JTextField;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.containers.ParamsPane;
import log.charter.gui.components.utils.IntValueValidator;
import log.charter.gui.components.utils.IntegerValueValidator;
import log.charter.song.Anchor;

public class AnchorPane extends ParamsPane {
	private static final long serialVersionUID = -4754359602173894487L;

	private final ChartData data;
	private final UndoSystem undoSystem;

	private final Anchor anchor;

	private Integer fret;
	private int width;

	public AnchorPane(final ChartData data, final CharterFrame frame, final UndoSystem undoSystem, final Anchor anchor,
			final Runnable onCancel) {
		super(frame, Label.ANCHOR_PANE, 250);
		this.data = data;
		this.undoSystem = undoSystem;

		this.anchor = anchor;

		fret = anchor.fret;
		width = anchor.width;

		int row = 0;
		addIntegerConfigValue(row++, 20, 100, Label.FRET, fret, 30, //
				new IntegerValueValidator(1, Config.frets, true), v -> fret = v, false);
		final JTextField input = (JTextField) getLastPart();
		input.setHorizontalAlignment(JTextField.CENTER);
		addSelectTextOnFocus(input);

		addIntConfigValue(row++, 20, 100, Label.ANCHOR_WIDTH, width, 30, //
				new IntValueValidator(1, Config.frets), v -> width = v, false);
		final JTextField AnchorWidthInput = (JTextField) getLastPart();
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
