package log.charter.services.data.validation;

import java.util.HashSet;
import java.util.Set;

import log.charter.data.config.Localization.Label;
import log.charter.data.song.Arrangement;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.Level;
import log.charter.data.song.enums.HOPO;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.gui.components.tabs.errorsTab.ChartError;
import log.charter.gui.components.tabs.errorsTab.ErrorsTab;
import log.charter.gui.components.tabs.errorsTab.position.ChartPositionGenerator;
import log.charter.gui.components.tabs.errorsTab.position.ChartPositionGenerator.ChartPosition;

public class ChordTemplatesValidator {
	private ChartPositionGenerator chartPositionGenerator;
	private ErrorsTab errorsTab;

	private Set<Integer> getChordTemplateIdsToSkip(final Arrangement arrangement) {
		final Set<Integer> idsToSkip = new HashSet<>();

		for (final Level level : arrangement.levels) {
			for (final ChordOrNote sound : level.sounds) {
				if (sound.isChord() && sound.notes().anyMatch(n -> n.hopo() == HOPO.TAP)) {
					idsToSkip.add(sound.chord().templateId());
				}
			}
		}

		return idsToSkip;
	}

	private void addError(final int arrangementId, final Label label, final int templateId, final int string) {
		final String message = label.format(templateId, string);
		final ChartPosition position = chartPositionGenerator.position().arrangement(arrangementId)
				.chordTemplate(templateId).build();
		errorsTab.addError(new ChartError(message, position));
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
		final Set<Integer> chordTemplateIdsToSkip = getChordTemplateIdsToSkip(arrangement);

		for (int i = 0; i < arrangement.chordTemplates.size(); i++) {
			if (chordTemplateIdsToSkip.contains(i)) {
				continue;
			}

			validateChordTemplate(arrangementId, arrangement, i, arrangement.chordTemplates.get(i));
		}
	}
}
