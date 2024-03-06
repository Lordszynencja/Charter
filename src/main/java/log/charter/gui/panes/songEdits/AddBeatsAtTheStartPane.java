package log.charter.gui.panes.songEdits;

import static log.charter.gui.components.utils.TextInputSelectAllOnFocus.addSelectTextOnFocus;

import javax.swing.JTextField;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.containers.ParamsPane;
import log.charter.gui.components.utils.validators.IntValueValidator;
import log.charter.song.Beat;
import log.charter.song.BeatsMap;
import log.charter.util.CollectionUtils.ArrayList2;

public class AddBeatsAtTheStartPane extends ParamsPane {
	private static final long serialVersionUID = -4754359602173894487L;

	private final ChartData data;
	private final UndoSystem undoSystem;

	private int beatsToGenerate = 4;

	public AddBeatsAtTheStartPane(final CharterFrame frame, final ChartData data, final UndoSystem undoSystem) {
		super(frame, Label.ADD_BEATS_PANE, 300);
		this.data = data;
		this.undoSystem = undoSystem;

		addLabel(0, 20, Label.ADD_BEATS, 0);

		addIntConfigValue(1, 20, 0, null, beatsToGenerate, 100, //
				new IntValueValidator(1, 100), v -> beatsToGenerate = v, false);
		final JTextField input = (JTextField) getLastPart();
		addSelectTextOnFocus(input);

		addDefaultFinish(3, this::saveAndExit);
	}

	private void saveAndExit() {
		undoSystem.addUndo();

		final BeatsMap beatsMap = data.songChart.beatsMap;
		final ArrayList2<Beat> beats = beatsMap.beats;
		final Beat firstBeat = beats.get(0);
		final double bpm = beatsMap.findBPM(firstBeat, 0);

		final int firstBeatPosition = firstBeat.position();
		for (int i = 0; i < beatsToGenerate; i++) {
			final int position = firstBeatPosition - (int) ((1 + i) * 60_000 / bpm);
			if (position >= 0) {
				final Beat beat = new Beat(position, firstBeat.beatsInMeasure, firstBeat.noteDenominator, false);
				beats.add(0, beat);
			}
		}

		beatsMap.fixFirstBeatInMeasures();
	}
}
