package log.charter.gui.panes.songEdits;

import static log.charter.gui.components.utils.TextInputSelectAllOnFocus.addSelectTextOnFocus;

import javax.swing.JTextField;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.config.values.InstrumentConfig;
import log.charter.data.song.FHP;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.containers.ParamsPane;
import log.charter.gui.components.utils.validators.IntValueValidator;
import log.charter.gui.components.utils.validators.IntegerValueValidator;

public class FHPPane extends ParamsPane {
	private static final long serialVersionUID = -4754359602173894487L;

	private final ChartData data;
	private final UndoSystem undoSystem;

	private final FHP fhp;

	private Integer fret;
	private int width;

	public FHPPane(final ChartData data, final CharterFrame frame, final UndoSystem undoSystem, final FHP fhp,
			final Runnable onCancel) {
		super(frame, Label.FHP_PANE, 250);
		this.data = data;
		this.undoSystem = undoSystem;

		this.fhp = fhp;

		fret = fhp.fret;
		width = fhp.width;

		int row = 0;
		addIntegerConfigValue(row++, 20, 100, Label.FRET, fret, 30, //
				new IntegerValueValidator(1, InstrumentConfig.frets, true), v -> fret = v, false);
		final JTextField input = (JTextField) getPart(-1);
		input.setHorizontalAlignment(JTextField.CENTER);
		addSelectTextOnFocus(input);

		addIntConfigValue(row++, 20, 100, Label.FHP_WIDTH, width, 30, //
				new IntValueValidator(1, InstrumentConfig.frets), v -> width = v, false);
		final JTextField fhpWidthInput = (JTextField) getPart(-1);
		fhpWidthInput.setHorizontalAlignment(JTextField.CENTER);
		addSelectTextOnFocus(fhpWidthInput);

		this.setOnFinish(this::saveAndExit, onCancel);
		addDefaultFinish(row);
	}

	private void saveAndExit() {
		undoSystem.addUndo();

		if (fret == null) {
			data.currentArrangementLevel().fhps.remove(fhp);
			return;
		}

		fhp.fret = fret;
		fhp.width = width;
	}
}
