package log.charter.data;

import static log.charter.gui.components.utils.ComponentUtils.askYesNoCancel;
import static log.charter.util.CollectionUtils.filter;
import static log.charter.util.Utils.formatTime;

import log.charter.data.config.Localization.Label;
import log.charter.data.managers.ModeManager;
import log.charter.gui.CharterFrame;
import log.charter.gui.handlers.data.ChartTimeHandler;
import log.charter.song.Arrangement;
import log.charter.song.EventPoint;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.Utils.TimeUnit;

public class ArrangementValidator {
	private ChartData chartData;
	private CharterFrame charterFrame;
	private ChartTimeHandler chartTimeHandler;
	private ModeManager modeManager;

	private void moveToTimeOnArrangement(final int arrangementId, final int time) {
		modeManager.setArrangement(arrangementId);
		chartTimeHandler.nextTime(time);
	}

	private boolean validateCountPhrases(final ArrayList2<EventPoint> phrases, final int arrangementId,
			final String arrangementName) {
		final ArrayList2<EventPoint> countPhrases = filter(phrases, phrase -> phrase.phrase.equals("COUNT"),
				ArrayList2::new);

		if (countPhrases.size() <= 1) {
			return true;
		}

		switch (askYesNoCancel(charterFrame, Label.WARNING, Label.MULTIPLE_COUNT_PHRASES_MOVE_TO_LAST_QUESTION,
				arrangementName)) {
			case YES:
				moveToTimeOnArrangement(arrangementId, countPhrases.getLast().position());
				return false;
			case NO:
				return true;
			case CANCEL:
			case EXIT:
			default:
				return false;
		}
	}

	private boolean validateEndPhrases(final ArrayList2<EventPoint> phrases, final int arrangementId,
			final String arrangementName) {
		final ArrayList2<EventPoint> endPhrases = filter(phrases, phrase -> phrase.phrase.equals("END"),
				ArrayList2::new);

		if (endPhrases.size() <= 1) {
			return true;
		}

		switch (askYesNoCancel(charterFrame, Label.WARNING, Label.MULTIPLE_END_PHRASES_MOVE_TO_FIRST_QUESTION,
				arrangementName)) {
			case YES:
				moveToTimeOnArrangement(arrangementId, endPhrases.get(0).position());
				return false;
			case NO:
				return true;
			case CANCEL:
			case EXIT:
			default:
				return false;
		}
	}

	private boolean validatePhrasesAmount(final ArrayList2<EventPoint> phrases, final int arrangementId,
			final String arrangementName) {
		if (!phrases.isEmpty()) {
			return true;
		}

		switch (askYesNoCancel(charterFrame, Label.WARNING, Label.NO_PHRASES_MOVE_TO_ARRANGEMENT_QUESTION,
				arrangementName)) {
			case YES:
				modeManager.setArrangement(arrangementId);
				return false;
			case NO:
				return true;
			case CANCEL:
			case EXIT:
			default:
				return false;
		}
	}

	private boolean validatePhrases(final int arrangementId, final Arrangement arrangement) {
		final ArrayList2<EventPoint> phrases = filter(arrangement.eventPoints, EventPoint::hasPhrase, ArrayList2::new);
		final String arrangementName = arrangement.getTypeNameLabel(arrangementId);

		if (!validateCountPhrases(phrases, arrangementId, arrangementName)) {
			return false;
		}
		if (!validateEndPhrases(phrases, arrangementId, arrangementName)) {
			return false;
		}
		if (!validatePhrasesAmount(phrases, arrangementId, arrangementName)) {
			return false;
		}

		return true;
	}

	private boolean validateSectionsAmount(final ArrayList2<EventPoint> sections, final int arrangementId,
			final String arrangementName) {
		if (!sections.isEmpty()) {
			return true;
		}

		switch (askYesNoCancel(charterFrame, Label.WARNING, Label.NO_SECTIONS_MOVE_TO_ARRANGEMENT_QUESTION,
				arrangementName)) {
			case YES:
				modeManager.setArrangement(arrangementId);
				return false;
			case NO:
				return true;
			case CANCEL:
			case EXIT:
			default:
				return false;
		}
	}

	private boolean validateSectionsContainPhrases(final ArrayList2<EventPoint> sections, final int arrangementId,
			final String arrangementName) {
		for (final EventPoint section : sections) {
			if (section.hasPhrase()) {
				continue;
			}

			switch (askYesNoCancel(charterFrame, Label.WARNING, Label.SECTION_WITHOUT_PHRASE_MOVE_QUESTION,
					section.section.label,
					formatTime(section.position(), TimeUnit.MILISECONDS, TimeUnit.MINUTES, TimeUnit.YEARS),
					arrangementName)) {
				case YES:
					moveToTimeOnArrangement(arrangementId, section.position());
					return false;
				case NO:
					return true;
				case CANCEL:
				case EXIT:
				default:
					return false;
			}
		}

		return true;
	}

	private boolean validateSections(final int arrangementId, final Arrangement arrangement) {
		final ArrayList2<EventPoint> sections = filter(arrangement.eventPoints, p -> p.section != null,
				ArrayList2::new);
		final String arrangementName = arrangement.getTypeNameLabel(arrangementId);

		if (!validateSectionsAmount(sections, arrangementId, arrangementName)) {
			return false;
		}
		if (!validateSectionsContainPhrases(sections, arrangementId, arrangementName)) {
			return false;
		}

		return true;
	}

	/**
	 * @return true if validation passed
	 */
	public boolean validate() {
		final ArrayList2<Arrangement> arrangements = chartData.songChart.arrangements;
		for (int i = 0; i < arrangements.size(); i++) {
			final Arrangement arrangement = arrangements.get(i);
			if (!validatePhrases(i, arrangement)) {
				return false;
			}
			if (!validateSections(i, arrangement)) {
				return false;
			}
		}

		return true;
	}
}
