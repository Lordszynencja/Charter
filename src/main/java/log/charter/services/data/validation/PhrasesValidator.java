package log.charter.services.data.validation;

import static log.charter.util.CollectionUtils.filter;
import static log.charter.util.CollectionUtils.findIdsFor;

import java.util.List;

import log.charter.data.config.Localization.Label;
import log.charter.data.song.Arrangement;
import log.charter.data.song.EventPoint;
import log.charter.data.song.HandShape;
import log.charter.data.song.Level;
import log.charter.data.song.position.FractionalPosition;
import log.charter.gui.components.tabs.errorsTab.ChartError;
import log.charter.gui.components.tabs.errorsTab.ChartPositionGenerator;
import log.charter.gui.components.tabs.errorsTab.ChartPositionGenerator.ChartPosition;
import log.charter.gui.components.tabs.errorsTab.ErrorsTab;
import log.charter.util.CollectionUtils;

public class PhrasesValidator {
	private ChartPositionGenerator chartPositionGenerator;
	private ErrorsTab errorsTab;

	private void addError(final Label label, final int arrangementId, final int id) {
		final ChartPosition position = chartPositionGenerator.position().arrangement(arrangementId).event(id).build();

		errorsTab.addError(new ChartError(label, position));
	}

	private void validateCountPhrases(final List<EventPoint> eventPoints, final int arrangementId) {
		final List<Integer> countPhraseIds = findIdsFor(eventPoints, p -> "COUNT".equals(p.phrase));

		if (countPhraseIds.size() == 1) {
			return;
		}

		if (countPhraseIds.size() < 1) {
			final ChartPosition position = chartPositionGenerator.position().arrangement(arrangementId)
					.time(new FractionalPosition(0)).build();
			errorsTab.addError(new ChartError(Label.NO_COUNT_PHRASE_IN_ARRANGEMENT, position));
			return;
		}

		for (final int id : countPhraseIds) {
			addError(Label.DUPLICATED_COUNT_PHRASE, arrangementId, id);
		}
	}

	private void validateEndPhrases(final List<EventPoint> eventPoints, final int arrangementId) {
		final List<Integer> endPhraseIds = findIdsFor(eventPoints, p -> "END".equals(p.phrase));
		if (endPhraseIds.size() == 1) {
			return;
		}

		if (endPhraseIds.size() < 1) {
			final ChartPosition position = chartPositionGenerator.position().arrangement(arrangementId)
					.time(new FractionalPosition(0)).build();
			errorsTab.addError(new ChartError(Label.NO_END_PHRASE_IN_ARRANGEMENT, position));
			return;
		}

		for (final int id : endPhraseIds) {
			addError(Label.DUPLICATED_END_PHRASE, arrangementId, id);
		}
	}

	private void validatePhrasesAmount(final List<EventPoint> eventPoints, final int arrangementId) {
		if (!filter(eventPoints, p -> p.hasPhrase() && !p.phrase.equals("COUNT") && !p.phrase.equals("END"))
				.isEmpty()) {
			return;
		}

		final ChartPosition position = chartPositionGenerator.position().arrangement(arrangementId).build();
		errorsTab.addError(new ChartError(Label.NO_PHRASES_IN_ARRANGEMENT, position));
	}

	private void checkPhrasesInsideHandShape(final int arrangementId, final Arrangement arrangement, final int levelId,
			final Level level) {
		for (int i = 0; i < arrangement.eventPoints.size(); i++) {
			final EventPoint eventPoint = arrangement.eventPoints.get(i);
			if (!eventPoint.hasPhrase()) {
				continue;
			}

			final HandShape handShape = CollectionUtils.lastBefore(level.handShapes, eventPoint).find();
			if (handShape == null || handShape.endPosition().compareTo(eventPoint) < 0) {
				continue;
			}

			final ChartPosition position = chartPositionGenerator.position()//
					.arrangement(arrangementId).level(levelId).event(i).build();
			errorsTab.addError(new ChartError(Label.PHRASE_INSIDE_HAND_SHAPE, position));
		}
	}

	public void validate(final int arrangementId, final Arrangement arrangement) {
		validateCountPhrases(arrangement.eventPoints, arrangementId);
		validateEndPhrases(arrangement.eventPoints, arrangementId);
		validatePhrasesAmount(arrangement.eventPoints, arrangementId);

		for (int levelId = 0; levelId < arrangement.levels.size(); levelId++) {
			checkPhrasesInsideHandShape(arrangementId, arrangement, levelId, arrangement.getLevel(levelId));
		}
	}
}
