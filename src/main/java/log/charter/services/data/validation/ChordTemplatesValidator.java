package log.charter.services.data.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import log.charter.data.config.Localization.Label;
import log.charter.data.song.Arrangement;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.enums.HOPO;
import log.charter.data.song.notes.Chord;
import log.charter.data.song.notes.ChordNote;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.gui.components.tabs.errorsTab.ChartError;
import log.charter.gui.components.tabs.errorsTab.ChartPositionGenerator;
import log.charter.gui.components.tabs.errorsTab.ChartPositionGenerator.ChartPosition;
import log.charter.gui.components.tabs.errorsTab.ErrorsTab;

public class ChordTemplatesValidator {
	private static class ChordWithPosition {
		public final ChartPosition position;
		public final int levelId;
		public final int id;
		public final Chord chord;

		public ChordWithPosition(final ChartPosition position, final int levelId, final int id, final Chord chord) {
			this.position = position;
			this.levelId = levelId;
			this.id = id;
			this.chord = chord;
		}
	}

	private ChartPositionGenerator chartPositionGenerator;
	private ErrorsTab errorsTab;

	private List<String> validateChordTemplate(final int arrangementId, final Arrangement arrangement,
			final int templateId, final ChordTemplate template) {
		final List<String> errors = new ArrayList<>();
		final int lowestFret = template.getLowestNotOpenFret(arrangement.capo);

		for (final int string : template.frets.keySet()) {
			final int fret = template.frets.get(string);
			final Integer finger = template.fingers.get(string);
			if (fret > lowestFret && finger != null && finger == 1) {
				errors.add(Label.FIRST_FINGER_ON_NOT_LOWEST_FRET.format(templateId, string));
				continue;
			}

			final boolean isOpen = template.frets.get(string) <= arrangement.capo;
			final boolean hasFinger = template.fingers.get(string) != null;
			if (isOpen == hasFinger) {
				final Label label = isOpen ? Label.FINGER_SET_FOR_OPEN_STRING//
						: Label.FINGER_NOT_SET_FOR_FRETTED_STRING;
				errors.add(label.format(templateId, string));
			}
		}

		return errors;
	}

	private Map<Integer, List<ChordWithPosition>> groupChordsByTemplates(final int arrangementId,
			final Arrangement arrangement) {
		final ChartPosition position = chartPositionGenerator.position().arrangement(arrangementId);

		final Map<Integer, List<ChordWithPosition>> chordsByTemplates = new HashMap<>();
		for (int levelId = 0; levelId < arrangement.levels.size(); levelId++) {
			position.level(levelId);

			final List<ChordOrNote> levelSounds = arrangement.levels.get(levelId).sounds;
			for (int i = 0; i < levelSounds.size(); i++) {
				final ChordOrNote sound = levelSounds.get(i);
				if (!sound.isChord()) {
					continue;
				}

				final Chord chord = sound.chord();
				if (!chordsByTemplates.containsKey(chord.templateId())) {
					chordsByTemplates.put(chord.templateId(), new ArrayList<>());
				}

				final ChordWithPosition chordInfo = new ChordWithPosition(position.clone().sound(i), levelId, i, chord);
				chordsByTemplates.get(chord.templateId()).add(chordInfo);
			}
		}

		return chordsByTemplates;
	}

	private boolean chordNoteIsTap(final List<ChordOrNote> sounds, final int string, final int id,
			final ChordNote note) {
		if (note.hopo == HOPO.TAP) {
			return true;
		}

		final ChordOrNote first = ChordOrNote.findFirstLinkedTo(string, id, sounds);
		return first.getString(string).map(n -> n.hopo() == HOPO.TAP).orElse(false);
	}

	private List<ChordWithPosition> getValidateableChords(final Arrangement arrangement,
			final List<ChordWithPosition> chordInfos) {
		final List<ChordWithPosition> validateableChords = new ArrayList<>();

		for (final ChordWithPosition chordInfo : chordInfos) {
			if (chordInfo.chord.chordNotes.entrySet().stream()
					.anyMatch(n -> chordNoteIsTap(arrangement.getLevel(chordInfo.levelId).sounds, n.getKey(),
							chordInfo.id, n.getValue()))) {
				continue;
			}

			validateableChords.add(chordInfo);
		}

		return validateableChords;
	}

	private void addErrorsToTemplate(final int templateId, final List<ChordWithPosition> chords,
			final List<String> errors) {
		final ChordWithPosition firstInstanceOfChord = chords.get(0);

		final ChartPosition position = firstInstanceOfChord.position.chordTemplate(templateId).build();

		for (final String error : errors) {
			errorsTab.addError(new ChartError(error, position));
		}
	}

	private void addErrorsToEveryChord(final List<ChordWithPosition> chords, final List<String> errors) {
		for (final ChordWithPosition invalidChord : chords) {
			final ChartPosition position = invalidChord.position.build();
			for (final String error : errors) {
				errorsTab.addError(new ChartError(error, position));
			}
		}
	}

	public void validate(final int arrangementId, final Arrangement arrangement) {
		final Map<Integer, List<ChordWithPosition>> chordsByTemplates = groupChordsByTemplates(arrangementId,
				arrangement);

		chordsByTemplates.forEach((templateId, chordInfos) -> {
			final List<ChordWithPosition> validateableChords = getValidateableChords(arrangement, chordInfos);

			if (validateableChords.isEmpty()) {
				return;
			}

			final ChordTemplate chordTemplate = arrangement.chordTemplates.get(templateId);
			final List<String> errors = validateChordTemplate(arrangementId, arrangement, arrangementId, chordTemplate);
			if (errors.isEmpty()) {
				return;
			}

			if (validateableChords.size() == chordInfos.size()) {
				addErrorsToTemplate(templateId, validateableChords, errors);
				return;
			}

			addErrorsToEveryChord(validateableChords, errors);
		});
	}
}
