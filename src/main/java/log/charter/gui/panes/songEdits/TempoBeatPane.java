package log.charter.gui.panes.songEdits;

import static log.charter.gui.components.utils.TextInputSelectAllOnFocus.addSelectTextOnFocus;
import static log.charter.util.CollectionUtils.lastBeforeEqual;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.swing.JTextField;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.Beat;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.containers.ParamsPane;
import log.charter.gui.components.utils.RowedPosition;
import log.charter.gui.components.utils.validators.BigDecimalValueValidator;
import log.charter.gui.components.utils.validators.IntValueValidator;

public class TempoBeatPane extends ParamsPane {
	private static final long serialVersionUID = -4754359602173894487L;

	private static final BigDecimal minBPM = new BigDecimal(1);
	private static final BigDecimal maxBPM = new BigDecimal(999);

	private final ChartData chartData;
	private final UndoSystem undoSystem;

	private final double audioLength;

	private final Beat beat;

	private boolean bpmChanged = false;
	private BigDecimal bpm;
	private int beatsInMeasure;
	private int noteDenominator;

	private BigDecimal roundBPM(final BigDecimal bpm) {
		return bpm.setScale(2, RoundingMode.HALF_UP);
	}

	private BigDecimal calculateBPM(final Beat beat) {
		return roundBPM(new BigDecimal(chartData.songChart.beatsMap.findBPM(beat)));
	}

	public TempoBeatPane(final ChartData chartData, final CharterFrame charterFrame, final UndoSystem undoSystem,
			final double audioLength, final Beat beat) {
		super(charterFrame, Label.TEMPO_BEAT_PANE, 250);
		this.chartData = chartData;
		this.undoSystem = undoSystem;

		this.audioLength = audioLength;

		this.beat = beat;
		beatsInMeasure = beat.beatsInMeasure;
		noteDenominator = beat.noteDenominator;

		final RowedPosition position = new RowedPosition(20, 0, 1);

		addBPM(position);
		position.newRow();

		addBeatsInMeasure(position);
		position.newRow();

		addNoteDenominator(position);
		position.newRow();
		position.newRow();

		this.setOnFinish(this::saveAndExit, null);
		addDefaultFinish(position.getY());
	}

	private void setBPM(final BigDecimal newBPM) {
		bpmChanged = true;
		bpm = newBPM;
	}

	private void addBPM(final RowedPosition position) {
		bpm = calculateBPM(beat);
		addBigDecimalConfigValue(position.getY(), position.getX(), 0, Label.TEMPO_BEAT_PANE_BPM, bpm, 60,
				new BigDecimalValueValidator(minBPM, maxBPM, false), this::setBPM, false);
	}

	private void addBeatsInMeasure(final RowedPosition position) {
		addIntConfigValue(position.getY(), 20, 0, Label.TEMPO_BEAT_PANE_BEATS_IN_MEASURE, beatsInMeasure, 30, //
				new IntValueValidator(1, 128), v -> beatsInMeasure = v, false);
		final JTextField beatsInMeasureInput = (JTextField) getPart(-1);
		beatsInMeasureInput.setHorizontalAlignment(JTextField.CENTER);
		addSelectTextOnFocus(beatsInMeasureInput);
	}

	private void addNoteDenominator(final RowedPosition position) {
		addIntConfigValue(position.getY(), 20, 0, Label.TEMPO_BEAT_PANE_NOTE_DENOMINATOR, noteDenominator, 30, //
				new IntValueValidator(1, 32), v -> noteDenominator = v, false);
		final JTextField noteDenominatorInput = (JTextField) getPart(-1);
		noteDenominatorInput.setHorizontalAlignment(JTextField.CENTER);
		addSelectTextOnFocus(noteDenominatorInput);
	}

	private void saveBPMChange(final int beatId) {
		if (!bpmChanged) {
			return;
		}

		beat.anchor = true;
		chartData.songChart.beatsMap.setBPM(beatId, bpm.doubleValue(), audioLength);
	}

	private void saveTimeSignatureChange(final int beatId) {
		if (beat.beatsInMeasure == beatsInMeasure && beat.noteDenominator == noteDenominator) {
			return;
		}

		final ImmutableBeatsMap beats = chartData.beats();
		for (int i = beatId; i < chartData.songChart.beatsMap.beats.size(); i++) {
			beats.get(i).setTimeSignature(beatsInMeasure, noteDenominator);
		}
	}

	private void saveAndExit() {
		undoSystem.addUndo();

		final int beatId = lastBeforeEqual(chartData.beats(), beat).findId(0);
		saveBPMChange(beatId);
		saveTimeSignatureChange(beatId);
		chartData.songChart.beatsMap.fixFirstBeatInMeasures();
	}
}
