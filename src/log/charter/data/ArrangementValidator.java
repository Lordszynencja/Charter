package log.charter.data;

import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;

import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.song.ArrangementChart;
import log.charter.song.PhraseIteration;
import log.charter.util.CollectionUtils.ArrayList2;

public class ArrangementValidator {
	private ChartData data;
	private CharterFrame frame;

	public void init(final ChartData data, final CharterFrame frame) {
		this.data = data;
		this.frame = frame;
	}

	private Runnable moveToTime(final int time) {
		return () -> data.setNextTime(time);
	}

	private Runnable moveToTimeOnArrangement(final int arrangementId, final int time) {
		return () -> {
			data.currentArrangement = arrangementId;
			data.currentLevel = 0;
			data.setNextTime(time);
		};
	}

	/**
	 * @return true if validation should continue
	 */
	private boolean showWarning(final Label msg, final Runnable onYes) {
		final int result = JOptionPane.showConfirmDialog(frame, msg.label(), "", JOptionPane.YES_NO_CANCEL_OPTION);

		if (result == JOptionPane.YES_OPTION) {
			onYes.run();
			return false;
		}
		if (result == JOptionPane.NO_OPTION) {
			return true;
		}
		if (result == JOptionPane.CANCEL_OPTION) {
			return false;
		}

		return true;
	}

	private boolean validateCountPhrases(final int arrangementId, final ArrangementChart arrangement) {
		final ArrayList2<PhraseIteration> countPhrases = arrangement.phraseIterations.stream()//
				.filter(phraseIteration -> phraseIteration.phraseName.equals("COUNT"))//
				.collect(Collectors.toCollection(ArrayList2::new));
		if (countPhrases.isEmpty()) {
			final boolean warningStoppedValidation = !showWarning(Label.COUNT_PHRASE_MISSING,
					moveToTimeOnArrangement(arrangementId, 0));
			if (warningStoppedValidation) {
				return false;
			}
		} else if (countPhrases.size() > 1) {
			final boolean warningStoppedValidation = !showWarning(Label.COUNT_PHRASE_MULTIPLE,
					moveToTimeOnArrangement(arrangementId, countPhrases.getLast().position()));
			if (warningStoppedValidation) {
				return false;
			}
		}

		return true;
	}

	private boolean validateEndPhrases(final int arrangementId, final ArrangementChart arrangement) {
		final List<PhraseIteration> endPhrases = arrangement.phraseIterations.stream()//
				.filter(phraseIteration -> phraseIteration.phraseName.equals("END"))//
				.collect(Collectors.toList());
		if (endPhrases.isEmpty()) {
			final boolean warningStoppedValidation = !showWarning(Label.END_PHRASE_MISSING,
					moveToTimeOnArrangement(arrangementId, data.songChart.beatsMap.beats.getLast().position()));
			if (warningStoppedValidation) {
				return false;
			}
		} else if (endPhrases.size() > 1) {
			final boolean warningStoppedValidation = !showWarning(Label.END_PHRASE_MULTIPLE,
					moveToTimeOnArrangement(arrangementId, endPhrases.get(0).position()));
			if (warningStoppedValidation) {
				return false;
			}
		}

		return true;
	}

	/**
	 * @return true if validation passed
	 */
	public boolean validate() {
		final ArrayList2<ArrangementChart> arrangements = data.songChart.arrangements;
		for (int i = 0; i < arrangements.size(); i++) {
			final ArrangementChart arrangement = arrangements.get(i);
			if (!validateCountPhrases(i, arrangement)) {
				return false;
			}
			if (!validateEndPhrases(i, arrangement)) {
				return false;
			}
		}

		return true;
	}
}
