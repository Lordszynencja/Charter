package log.charter.services.data.validation;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.Arrangement;
import log.charter.data.song.FHP;
import log.charter.data.song.HandShape;
import log.charter.data.song.Level;
import log.charter.gui.CharterFrame.TabType;
import log.charter.gui.components.tabs.errorsTab.ChartError;
import log.charter.gui.components.tabs.errorsTab.ChartPositionGenerator;
import log.charter.gui.components.tabs.errorsTab.ErrorsTab;
import log.charter.gui.components.tabs.errorsTab.ChartPositionGenerator.ChartPosition;
import log.charter.util.CollectionUtils;

public class FHPsValidator {
	/**
	 * in miliseconds
	 */
	private static final int minimalFhpDistance = 5;

	private ChartData chartData;
	private ChartPositionGenerator chartPositionGenerator;
	private ErrorsTab errorsTab;

	private void checkFhpBelowCapo(final int arrangementId, final Arrangement arrangement, final int levelId,
			final Level level) {
		for (int i = 0; i < level.fhps.size(); i++) {
			final FHP fhp = level.fhps.get(i);
			if (fhp.fret <= arrangement.capo) {
				final ChartPosition position = chartPositionGenerator.position()//
						.arrangement(arrangementId).level(levelId).fhp(i).tab(TabType.QUICK_EDIT).build();
				errorsTab.addError(new ChartError(Label.FHP_STARTS_ON_WRONG_FRET, position));
			}
		}
	}

	private void checkFhpTooCloseToNext(final int arrangementId, final Arrangement arrangement, final int levelId,
			final Level level) {
		if (level.fhps.isEmpty()) {
			return;
		}

		FHP current = level.fhps.get(0);
		for (int i = 0; i < level.fhps.size() - 1; i++) {
			final FHP next = level.fhps.get(i + 1);
			if (next.position(chartData.beats()) - current.position(chartData.beats()) < minimalFhpDistance) {
				final String message = Label.FHP_TOO_CLOSE_TO_NEXT.format(minimalFhpDistance);
				final ChartPosition position = chartPositionGenerator.position()//
						.arrangement(arrangementId).level(levelId).fhp(i).build();
				errorsTab.addError(new ChartError(message, position));
			}

			current = next;
		}
	}

	private void checkFhpInsideHandShape(final int arrangementId, final Arrangement arrangement, final int levelId,
			final Level level) {
		for (int i = 0; i < level.fhps.size(); i++) {
			final FHP fhp = level.fhps.get(i);

			final HandShape handShape = CollectionUtils.lastBefore(level.handShapes, fhp).find();
			if (handShape == null || handShape.endPosition().compareTo(fhp) < 0) {
				continue;
			}

			final ChartPosition position = chartPositionGenerator.position()//
					.arrangement(arrangementId).level(levelId).fhp(i).build();
			errorsTab.addError(new ChartError(Label.FHP_INSIDE_HAND_SHAPE, position));
		}
	}

	public void validate(final int arrangementId, final Arrangement arrangement, final int levelId, final Level level) {
		checkFhpBelowCapo(arrangementId, arrangement, levelId, level);
		checkFhpTooCloseToNext(arrangementId, arrangement, levelId, level);
		checkFhpInsideHandShape(arrangementId, arrangement, levelId, level);
	}
}
