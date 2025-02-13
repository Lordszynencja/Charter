package log.charter.services.data.validation;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.Arrangement;
import log.charter.data.song.ChordTemplate;
import log.charter.gui.components.tabs.errorsTab.ChartError;
import log.charter.gui.components.tabs.errorsTab.ChartError.ChartErrorSeverity;
import log.charter.gui.components.tabs.errorsTab.ChartPositionOnArrangement;
import log.charter.gui.components.tabs.errorsTab.ErrorsTab;
import log.charter.services.editModes.ModeManager;

public class ChordTemplatesValidator {
	private ChartData chartData;
	private ErrorsTab errorsTab;
	private ModeManager modeManager;

	private void addError(final int arrangementId, final Label label, final int templateId, final int string) {
		final ChartPositionOnArrangement errorPosition = new ChartPositionOnArrangement(chartData, arrangementId,
				modeManager);
		errorsTab.addError(new ChartError(label.format(templateId, string), ChartErrorSeverity.ERROR, errorPosition));
	}

	private void validateChordTemplate(final int arrangementId, final Arrangement arrangement, final int templateId,
			final ChordTemplate template) {
		final int lowestFret = template.getLowestNotOpenFret(arrangement.capo);

		for (final int string : template.frets.keySet()) {
			final int fret = template.frets.get(string);
			final Integer finger = template.fingers.get(string);
			if (fret > lowestFret && finger != null && finger == 1) {
				addError(arrangementId, Label.FIRST_FINGER_ON_NOT_LOWEST_FRET, templateId, string);
				continue;
			}

			final boolean isOpen = template.frets.get(string) <= arrangement.capo;
			final boolean hasFinger = template.fingers.get(string) != null;
			if (isOpen == hasFinger) {
				final Label label = isOpen ? Label.FINGER_SET_FOR_OPEN_STRING//
						: Label.FINGER_NOT_SET_FOR_FRETTED_STRING;
				addError(arrangementId, label, templateId, string);
			}

		}
	}

	public void validate(final int arrangementId, final Arrangement arrangement) {
		for (int i = 0; i < arrangement.chordTemplates.size(); i++) {
			validateChordTemplate(arrangementId, arrangement, i, arrangement.chordTemplates.get(i));
		}
	}
}
