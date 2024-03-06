package log.charter.gui.panes.songEdits;

import static log.charter.gui.components.utils.TextInputSelectAllOnFocus.addSelectTextOnFocus;
import static log.charter.song.notes.IConstantPosition.findClosestId;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.swing.JTextField;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.containers.ParamsPane;
import log.charter.gui.components.utils.validators.BigDecimalValueValidator;
import log.charter.gui.components.utils.validators.IntValueValidator;
import log.charter.song.Beat;
import log.charter.util.CollectionUtils.ArrayList2;

public class TempoBeatPane extends ParamsPane {
	private static final long serialVersionUID = -4754359602173894487L;

	private static final BigDecimal minBPM = new BigDecimal(1);
	private static final BigDecimal maxBPM = new BigDecimal(999);

	private final ChartData data;
	private final UndoSystem undoSystem;

	private final int audioLength;

	private final Beat beat;

	private BigDecimal bpm;
	private int beatsInMeasure;
	private int noteDenominator;

	private BigDecimal roundBPM(final BigDecimal bpm) {
		return bpm.setScale(2, RoundingMode.HALF_UP);
	}

	private BigDecimal calculateBPM(final Beat beat) {
		return roundBPM(new BigDecimal(data.songChart.beatsMap.findBPM(beat)));
	}

	public TempoBeatPane(final ChartData data, final CharterFrame frame, final UndoSystem undoSystem,
			final int audioLength, final Beat beat) {
		super(frame, Label.TEMPO_BEAT_PANE, 250);
		this.data = data;
		this.undoSystem = undoSystem;

		this.audioLength = audioLength;

		this.beat = beat;
		beatsInMeasure = beat.beatsInMeasure;
		noteDenominator = beat.noteDenominator;

		int row = 0;

		bpm = calculateBPM(beat);
		addBigDecimalConfigValue(row++, 20, 0, Label.TEMPO_BEAT_PANE_BPM, bpm, 60,
				new BigDecimalValueValidator(minBPM, maxBPM, false), val -> bpm = roundBPM(val), false);

		addIntConfigValue(row++, 20, 0, Label.TEMPO_BEAT_PANE_BEATS_IN_MEASURE, beatsInMeasure, 30, //
				new IntValueValidator(1, 128), v -> beatsInMeasure = v, false);
		final JTextField beatsInMeasureInput = (JTextField) getLastPart();
		beatsInMeasureInput.setHorizontalAlignment(JTextField.CENTER);
		addSelectTextOnFocus(beatsInMeasureInput);

		addIntConfigValue(row++, 20, 0, Label.TEMPO_BEAT_PANE_NOTE_DENOMINATOR, noteDenominator, 30, //
				new IntValueValidator(1, 32), v -> noteDenominator = v, false);
		final JTextField noteDenominatorInput = (JTextField) getLastPart();
		noteDenominatorInput.setHorizontalAlignment(JTextField.CENTER);
		addSelectTextOnFocus(noteDenominatorInput);

		row++;
		addDefaultFinish(row, this::saveAndExit);
	}

	private void saveAndExit() {
		undoSystem.addUndo();

		final ArrayList2<Beat> beats = data.songChart.beatsMap.beats;
		final int beatId = findClosestId(beats, beat.position());

		if (bpm.compareTo(calculateBPM(beat)) != 0) {
			beat.anchor = true;
			data.songChart.beatsMap.setBPM(beatId, bpm.doubleValue(), audioLength);
		}

		final int currentBeatsInMeasure = beat.beatsInMeasure;
		final int currentNoteDenominator = beat.noteDenominator;
		if (currentBeatsInMeasure != beatsInMeasure || currentNoteDenominator != noteDenominator) {
			int beatIdTo = beatId + 1;
			while (beatIdTo < beats.size()//
					&& beats.get(beatIdTo).beatsInMeasure == currentBeatsInMeasure//
					&& beats.get(beatIdTo).noteDenominator == currentNoteDenominator) {
				beatIdTo++;
			}

			for (int i = beatId; i < beatIdTo; i++) {
				beat.anchor = true;
				beats.get(i).setTimeSignature(beatsInMeasure, noteDenominator);
			}
		}

		data.songChart.beatsMap.fixFirstBeatInMeasures();
	}
}
