package log.charter.services.data.validation;

import static log.charter.gui.components.utils.ComponentUtils.askYesNoCancel;
import static log.charter.util.CollectionUtils.filter;
import static log.charter.util.Utils.formatTime;

import java.util.List;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.Arrangement;
import log.charter.data.song.EventPoint;
import log.charter.data.song.SectionType;
import log.charter.gui.CharterFrame;
import log.charter.services.data.ChartTimeHandler;
import log.charter.util.Utils.TimeUnit;

public class SectionsValidator {
	private ChartData chartData;
	private CharterFrame charterFrame;
	private ChartTimeHandler chartTimeHandler;

	private boolean validateSectionsAmount(final List<EventPoint> sections, final int arrangementId,
			final String arrangementName) {
		if (!sections.isEmpty()) {
			return true;
		}

		switch (askYesNoCancel(charterFrame, Label.WARNING, Label.NO_SECTIONS_MOVE_TO_ARRANGEMENT_QUESTION,
				arrangementName)) {
			case YES:
				chartTimeHandler.moveTo(arrangementId, null, null);
				return false;
			case NO:
				return true;
			case CANCEL:
			case EXIT:
			default:
				return false;
		}
	}

	private boolean validateSectionsContainPhrases(final List<EventPoint> sections, final int arrangementId,
			final String arrangementName) {
		for (final EventPoint section : sections) {
			if (section.hasPhrase() || section.section == SectionType.NO_GUITAR) {
				continue;
			}

			switch (askYesNoCancel(charterFrame, Label.WARNING, Label.SECTION_WITHOUT_PHRASE_MOVE_QUESTION,
					section.section.label, formatTime(section.position(chartData.beats()), TimeUnit.MILISECONDS,
							TimeUnit.MINUTES, TimeUnit.YEARS),
					arrangementName)) {
				case YES:
					chartTimeHandler.moveTo(arrangementId, null, section.position());
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

	public boolean validateSections(final int arrangementId, final Arrangement arrangement) {
		final List<EventPoint> sections = filter(arrangement.eventPoints, p -> p.section != null);
		final String arrangementName = arrangement.getTypeNameLabel(arrangementId);

		if (!validateSectionsAmount(sections, arrangementId, arrangementName)) {
			return false;
		}
		if (!validateSectionsContainPhrases(sections, arrangementId, arrangementName)) {
			return false;
		}

		return true;
	}
}
