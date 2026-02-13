package log.charter.services.data.validation;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.Arrangement;
import log.charter.data.song.FHP;
import log.charter.data.song.Level;
import log.charter.gui.CharterFrame.TabType;
import log.charter.gui.components.tabs.errorsTab.ChartError;
import log.charter.gui.components.tabs.errorsTab.ErrorsTab;
import log.charter.gui.components.tabs.errorsTab.position.ChartPositionGenerator;
import log.charter.gui.components.tabs.errorsTab.position.ChartPositionGenerator.ChartPosition;

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

	public void validate(final int arrangementId, final Arrangement arrangement, final int levelId, final Level level) {
		checkFhpBelowCapo(arrangementId, arrangement, levelId, level);
		checkFhpTooCloseToNext(arrangementId, arrangement, levelId, level);
	}
}
