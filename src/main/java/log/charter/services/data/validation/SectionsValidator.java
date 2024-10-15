package log.charter.services.data.validation;

import static log.charter.util.CollectionUtils.filter;

import java.util.List;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.Arrangement;
import log.charter.data.song.EventPoint;
import log.charter.data.song.SectionType;
import log.charter.gui.components.tabs.errorsTab.ChartError;
import log.charter.gui.components.tabs.errorsTab.ChartError.ChartErrorSeverity;
import log.charter.gui.components.tabs.errorsTab.ChartError.ChartPosition;
import log.charter.gui.components.tabs.errorsTab.ErrorsTab;

public class SectionsValidator {
	private ChartData chartData;
	private ErrorsTab errorsTab;

	private void validateSectionsAmount(final List<EventPoint> sections, final int arrangementId) {
		if (!sections.isEmpty()) {
			return;
		}

		final ChartPosition errorPosition = new ChartPosition(chartData, arrangementId);
		errorsTab.addError(new ChartError(Label.NO_SECTIONS_IN_ARRANGEMENT, ChartErrorSeverity.ERROR, errorPosition));
	}

	private void validateSectionsContainPhrases(final List<EventPoint> sections, final int arrangementId) {
		for (final EventPoint section : sections) {
			if (section.hasPhrase() || section.section == SectionType.NO_GUITAR) {
				continue;
			}

			final ChartPosition errorPosition = new ChartPosition(chartData, arrangementId, section.position());
			errorsTab.addError(new ChartError(Label.SECTION_WITHOUT_PHRASE, ChartErrorSeverity.ERROR, errorPosition));
		}
	}

	public void validateSections(final int arrangementId, final Arrangement arrangement) {
		final List<EventPoint> sections = filter(arrangement.eventPoints, p -> p.section != null);

		validateSectionsAmount(sections, arrangementId);
		validateSectionsContainPhrases(sections, arrangementId);
	}
}
