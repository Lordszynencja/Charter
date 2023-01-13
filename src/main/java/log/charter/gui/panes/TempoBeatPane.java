package log.charter.gui.panes;

import static log.charter.gui.components.TextInputSelectAllOnFocus.addSelectTextOnFocus;
import static log.charter.gui.components.TextInputWithValidation.ValueValidator.createIntValidator;
import static log.charter.song.notes.IPosition.findClosestId;

import javax.swing.JTextField;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.ParamsPane;
import log.charter.song.Beat;
import log.charter.util.CollectionUtils.ArrayList2;

public class TempoBeatPane extends ParamsPane {
	private static final long serialVersionUID = -4754359602173894487L;

	private static PaneSizes getSizes() {
		final PaneSizes sizes = new PaneSizes();
		sizes.labelWidth = 80;
		sizes.width = 250;

		return sizes;
	}

	private final ChartData data;
	private final UndoSystem undoSystem;

	private final Beat beat;

	private int beatsInMeasure;
	private int noteDenominator;

	public TempoBeatPane(final ChartData data, final CharterFrame frame, final UndoSystem undoSystem, final Beat beat) {
		super(frame, Label.TEMPO_BEAT_PANE, 4, getSizes());
		this.data = data;
		this.undoSystem = undoSystem;

		this.beat = beat;
		beatsInMeasure = beat.beatsInMeasure;
		noteDenominator = beat.noteDenominator;

		int row = 0;
		addIntegerConfigValue(row++, 20, 0, Label.TEMPO_BEAT_PANE_BEATS_IN_MEASURE, beatsInMeasure, 30,
				createIntValidator(1, 99, false), val -> beatsInMeasure = val, false);
		final JTextField beatsInMeasureInput = (JTextField) components.getLast();
		beatsInMeasureInput.setHorizontalAlignment(JTextField.CENTER);
		addSelectTextOnFocus(beatsInMeasureInput);
		addIntegerConfigValue(row++, 20, 0, Label.TEMPO_BEAT_PANE_BEATS_IN_MEASURE, noteDenominator, 30,
				createIntValidator(1, 99, false), val -> noteDenominator = val, false);
		final JTextField noteDenominatorInput = (JTextField) components.getLast();
		noteDenominatorInput.setHorizontalAlignment(JTextField.CENTER);
		addSelectTextOnFocus(noteDenominatorInput);

		row++;
		addDefaultFinish(row, this::saveAndExit);
	}

	private void saveAndExit() {
		undoSystem.addUndo();

		final ArrayList2<Beat> beats = data.songChart.beatsMap.beats;

		final int currentBeatsInMeasure = beat.beatsInMeasure;
		final int beatId = findClosestId(beats, beat.position());
		int beatIdTo = beatId;
		while (beatIdTo < beats.size() && beats.get(beatIdTo).beatsInMeasure == currentBeatsInMeasure) {
			beatIdTo++;
		}

		for (int i = beatId; i < beatIdTo; i++) {
			final Beat beat = beats.get(i);
			beat.setTimeSignature(beatsInMeasure, noteDenominator);
		}

		data.songChart.beatsMap.fixFirstBeatInMeasures();
	}
}
