package log.charter.services.data.validation;

import static log.charter.gui.components.utils.ComponentUtils.askYesNoCancel;
import static log.charter.util.CollectionUtils.filter;
import static log.charter.util.Utils.formatTime;

import log.charter.data.config.Localization.Label;
import log.charter.data.song.Arrangement;
import log.charter.data.song.EventPoint;
import log.charter.gui.CharterFrame;
import log.charter.services.data.ChartTimeHandler;
import log.charter.services.editModes.ModeManager;
import log.charter.util.Utils.TimeUnit;
import log.charter.util.collections.ArrayList2;

public class SectionsValidator {
	private CharterFrame charterFrame;
	private ChartTimeHandler chartTimeHandler;
	private ModeManager modeManager;

	private void moveToTimeOnArrangement(final int arrangementId, final int time) {
		modeManager.setArrangement(arrangementId);
		chartTimeHandler.nextTime(time);
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

	public boolean validateSections(final int arrangementId, final Arrangement arrangement) {
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
}
