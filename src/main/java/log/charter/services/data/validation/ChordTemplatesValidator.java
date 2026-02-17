package log.charter.services.data.validation;

import java.util.HashSet;
import java.util.Set;

import log.charter.data.config.Localization.Label;
import log.charter.data.song.Arrangement;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.HandShape;
import log.charter.data.song.Level;
import log.charter.data.song.enums.HOPO;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.gui.components.tabs.errorsTab.ChartError;
import log.charter.gui.components.tabs.errorsTab.ChartPositionGenerator;
import log.charter.gui.components.tabs.errorsTab.ChartPositionGenerator.ChartPosition;
import log.charter.gui.components.tabs.errorsTab.ErrorsTab;

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

	private void addFirstItemUsingTemplate(final ChartPosition position, final Arrangement arrangement,
			final int templateId) {
		for (int i = arrangement.levels.size() - 1; i >= 0; i--) {
			final Level level = arrangement.levels.get(i);

			for (int soundId = 0; soundId < level.sounds.size(); soundId++) {
				final ChordOrNote sound = level.sounds.get(soundId);
				if (sound.isChord() && sound.chord().templateId() == templateId) {
					position.level(i).sound(soundId);
					return;
				}
			}
			for (int handShapeId = 0; handShapeId < level.handShapes.size(); handShapeId++) {
				final HandShape handShape = level.handShapes.get(handShapeId);
				if (handShape.templateId == templateId) {
					position.level(i).handShape(handShapeId);
					return;
				}
			}
		}
	}

	private void addError(final int arrangementId, final Arrangement arrangement, final Label label,
			final int templateId, final int string) {
		final String message = label.format(templateId, string);
		final ChartPosition position = chartPositionGenerator.position().arrangement(arrangementId)
				.chordTemplate(templateId);
		addFirstItemUsingTemplate(position, arrangement, templateId);

		errorsTab.addError(new ChartError(message, position.build()));
	}

	private void validateChordTemplate(final int arrangementId, final Arrangement arrangement, final int templateId,
			final ChordTemplate template) {
		final int lowestFret = template.getLowestNotOpenFret(arrangement.capo);

		for (final int string : template.frets.keySet()) {
			final int fret = template.frets.get(string);
			final Integer finger = template.fingers.get(string);
			if (fret > lowestFret && finger != null && finger == 1) {
				addError(arrangementId, arrangement, Label.FIRST_FINGER_ON_NOT_LOWEST_FRET, templateId, string);
				continue;
			}

			final boolean isOpen = template.frets.get(string) <= arrangement.capo;
			final boolean hasFinger = template.fingers.get(string) != null;
			if (isOpen == hasFinger) {
				final Label label = isOpen ? Label.FINGER_SET_FOR_OPEN_STRING//
						: Label.FINGER_NOT_SET_FOR_FRETTED_STRING;
				addError(arrangementId, arrangement, label, templateId, string);
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
