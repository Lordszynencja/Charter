package log.charter.services.data.validation;

import static log.charter.util.CollectionUtils.filter;

import java.util.List;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.Arrangement;
import log.charter.data.song.EventPoint;
import log.charter.data.song.position.FractionalPosition;
import log.charter.gui.components.tabs.errorsTab.ChartError;
import log.charter.gui.components.tabs.errorsTab.ChartError.ChartErrorSeverity;
import log.charter.gui.components.tabs.errorsTab.ChartPosition;
import log.charter.gui.components.tabs.errorsTab.ChartPositionOnArrangement;
import log.charter.gui.components.tabs.errorsTab.ChartPositionOnArrangementTime;
import log.charter.gui.components.tabs.errorsTab.ErrorsTab;
import log.charter.services.data.ChartTimeHandler;
import log.charter.services.editModes.ModeManager;

public class PhrasesValidator {
	private ChartData chartData;
	private ChartTimeHandler chartTimeHandler;
	private ErrorsTab errorsTab;
	private ModeManager modeManager;

	private void addError(final Label label, final int arrangementId, final FractionalPosition position) {
		final ChartPosition errorPosition = new ChartPositionOnArrangementTime(chartData, arrangementId, position,
				chartTimeHandler, modeManager);
		errorsTab.addError(new ChartError(label, ChartErrorSeverity.ERROR, errorPosition));
	}

	private void validateCountPhrases(final List<EventPoint> phrases, final int arrangementId) {
		final List<EventPoint> countPhrases = filter(phrases, phrase -> phrase.phrase.equals("COUNT"));
		if (countPhrases.size() <= 1) {
			return;
		}

		for (int i = 0; i < countPhrases.size(); i++) {
			addError(Label.DUPLICATED_COUNT_PHRASE, arrangementId, countPhrases.get(i).position());
		}
	}

	private void validateEndPhrases(final List<EventPoint> phrases, final int arrangementId) {
		final List<EventPoint> endPhrases = filter(phrases, phrase -> phrase.phrase.equals("END"));
		if (endPhrases.size() <= 1) {
			return;
		}

		for (int i = 0; i < endPhrases.size(); i++) {
			addError(Label.DUPLICATED_END_PHRASE, arrangementId, endPhrases.get(i).position());
		}
	}

	private void validatePhrasesAmount(final List<EventPoint> phrases, final int arrangementId) {
		if (!filter(phrases, ep -> !ep.phrase.equals("COUNT") && !ep.phrase.equals("END")).isEmpty()) {
			return;
		}

		final ChartPosition errorPosition = new ChartPositionOnArrangement(chartData, arrangementId, modeManager);
		errorsTab.addError(new ChartError(Label.NO_PHRASES_IN_ARRANGEMENT, ChartErrorSeverity.ERROR, errorPosition));
	}

	public void validate(final int arrangementId, final Arrangement arrangement) {
		final List<EventPoint> phrases = filter(arrangement.eventPoints, EventPoint::hasPhrase);

		validateCountPhrases(phrases, arrangementId);
		validateEndPhrases(phrases, arrangementId);
		validatePhrasesAmount(phrases, arrangementId);
	}
}
