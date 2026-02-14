package log.charter.services.data.validation;

import static log.charter.util.CollectionUtils.filter;

import java.util.List;

import log.charter.data.config.Localization.Label;
import log.charter.data.song.Arrangement;
import log.charter.data.song.EventPoint;
import log.charter.data.song.SectionType;
import log.charter.gui.components.tabs.errorsTab.ChartError;
import log.charter.gui.components.tabs.errorsTab.ChartPositionGenerator;
import log.charter.gui.components.tabs.errorsTab.ErrorsTab;
import log.charter.gui.components.tabs.errorsTab.ChartPositionGenerator.ChartPosition;
import log.charter.util.CollectionUtils;

public class SectionsValidator {
	private ChartPositionGenerator chartPositionGenerator;
	private ErrorsTab errorsTab;

	private void validateSectionsAmount(final List<EventPoint> eventPoints, final int arrangementId) {
		if (!filter(eventPoints, p -> p.section != null).isEmpty()) {
			return;
		}

		final ChartPosition position = chartPositionGenerator.position().arrangement(arrangementId).build();
		errorsTab.addError(new ChartError(Label.NO_SECTIONS_IN_ARRANGEMENT, position));
	}

	private void validateSectionsContainPhrases(final List<EventPoint> eventPoints, final int arrangementId) {
		final List<Integer> sectionsWithoutPhraseIds = CollectionUtils.findIdsFor(eventPoints,
				p -> p.section != null && (!p.hasPhrase() && p.section != SectionType.NO_GUITAR));

		for (final int id : sectionsWithoutPhraseIds) {

			final ChartPosition position = chartPositionGenerator.position().arrangement(arrangementId).event(id)
					.build();
			errorsTab.addError(new ChartError(Label.SECTION_WITHOUT_PHRASE, position));
		}
	}

	public void validate(final int arrangementId, final Arrangement arrangement) {
		validateSectionsAmount(arrangement.eventPoints, arrangementId);
		validateSectionsContainPhrases(arrangement.eventPoints, arrangementId);
	}
}
