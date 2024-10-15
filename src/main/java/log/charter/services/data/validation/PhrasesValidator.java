package log.charter.services.data.validation;

import static log.charter.util.CollectionUtils.filter;

import java.util.List;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.Arrangement;
import log.charter.data.song.EventPoint;
import log.charter.gui.components.tabs.errorsTab.ChartError;
import log.charter.gui.components.tabs.errorsTab.ChartError.ChartErrorSeverity;
import log.charter.gui.components.tabs.errorsTab.ChartError.ChartPosition;
import log.charter.gui.components.tabs.errorsTab.ErrorsTab;

public class PhrasesValidator {
	private ChartData chartData;
	private ErrorsTab errorsTab;

	private void validateCountPhrases(final List<EventPoint> phrases, final int arrangementId) {
		final List<EventPoint> countPhrases = filter(phrases, phrase -> phrase.phrase.equals("COUNT"));

		for (int i = 1; i < countPhrases.size(); i++) {
			final ChartPosition errorPosition = new ChartPosition(chartData, arrangementId,
					countPhrases.get(i).position());
			errorsTab.addError(new ChartError(Label.DUPLICATED_COUNT_PHRASE, ChartErrorSeverity.ERROR, errorPosition));
		}
	}

	private void validateEndPhrases(final List<EventPoint> phrases, final int arrangementId) {
		final List<EventPoint> endPhrases = filter(phrases, phrase -> phrase.phrase.equals("END"));

		for (int i = 0; i < endPhrases.size() - 1; i++) {
			final ChartPosition errorPosition = new ChartPosition(chartData, arrangementId,
					endPhrases.get(i).position());
			errorsTab.addError(new ChartError(Label.DUPLICATED_END_PHRASE, ChartErrorSeverity.ERROR, errorPosition));
		}
	}

	private void validatePhrasesAmount(final List<EventPoint> phrases, final int arrangementId) {
		if (!filter(phrases, ep -> !ep.phrase.equals("COUNT") && !ep.phrase.equals("END")).isEmpty()) {
			return;
		}

		final ChartPosition errorPosition = new ChartPosition(chartData, arrangementId);
		errorsTab.addError(new ChartError(Label.NO_PHRASES_IN_ARRANGEMENT, ChartErrorSeverity.ERROR, errorPosition));
	}

	public void validatePhrases(final int arrangementId, final Arrangement arrangement) {
		final List<EventPoint> phrases = filter(arrangement.eventPoints, EventPoint::hasPhrase);

		validateCountPhrases(phrases, arrangementId);
		validateEndPhrases(phrases, arrangementId);
		validatePhrasesAmount(phrases, arrangementId);
	}
}
