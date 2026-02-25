package log.charter.services.data.validation;

import java.util.Objects;
import java.util.Optional;

import log.charter.data.config.Localization.Label;
import log.charter.data.song.Arrangement;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.FHP;
import log.charter.data.song.HandShape;
import log.charter.data.song.Level;
import log.charter.data.song.enums.HOPO;
import log.charter.data.song.notes.Chord;
import log.charter.data.song.notes.ChordNote;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.notes.CommonNoteWithFret;
import log.charter.gui.CharterFrame.TabType;
import log.charter.gui.components.tabs.errorsTab.ChartError;
import log.charter.gui.components.tabs.errorsTab.ChartPositionGenerator;
import log.charter.gui.components.tabs.errorsTab.ChartPositionGenerator.ChartPosition;
import log.charter.gui.components.tabs.errorsTab.ErrorsTab;
import log.charter.util.CollectionUtils;
import log.charter.util.collections.Pair;

public class GuitarSoundsValidator {
	private ChartPositionGenerator chartPositionGenerator;
	private ErrorsTab errorsTab;

	private class LevelValidator {
		private final int arrangementId;
		private final Arrangement arrangement;
		private final int levelId;
		private final Level level;

		public LevelValidator(final int arrangementId, final Arrangement arrangement, final int levelId,
				final Level level) {
			this.arrangementId = arrangementId;
			this.arrangement = arrangement;
			this.levelId = levelId;
			this.level = level;
		}

		private ChartError generateErrorWithTab(final String label, final int id) {
			final ChartPosition position = chartPositionGenerator.position().arrangement(arrangementId).level(levelId)
					.sound(id).tab(TabType.QUICK_EDIT).build();
			return new ChartError(label, position);
		}

		private ChartError generateErrorWithTab(final Label label, final int id) {
			return generateErrorWithTab(label.label(), id);
		}

		private ChartError generateError(final String label, final int id) {
			final ChartPosition position = chartPositionGenerator.position().arrangement(arrangementId).level(levelId)
					.sound(id).tab(TabType.QUICK_EDIT).build();
			return new ChartError(label, position);
		}

		private ChartError generateError(final Label label, final int id) {
			return generateError(label.label(), id);
		}

		private void validateFrets(final int id, final ChordOrNote sound) {
			sound.notesWithFrets(arrangement.chordTemplates).forEach(note -> {
				if (note.fret() < arrangement.capo) {
					generateErrorWithTab(Label.NOTE_FRET_BELOW_CAPO, id);
				}
			});
		}

		private void validateSlideFret(final int id, final CommonNoteWithFret note) {
			if (note.fret() == 0) {
				errorsTab.addError(generateErrorWithTab(Label.NOTE_SLIDE_FROM_OPEN_STRING, id));
			}
		}

		private void validatePitchedWithoutLinkNext(final int id, final CommonNoteWithFret note) {
			if (!note.linkNext() && !note.unpitchedSlide()) {
				errorsTab.addError(generateErrorWithTab(Label.NOTE_SLIDE_NOT_LINKED, id));
			}
		}

		private void validateUnpitchedWithLinkNext(final int id, final CommonNoteWithFret note) {
			if (note.unpitchedSlide()) {
				errorsTab.addError(generateErrorWithTab(Label.UNPITCHED_NOTE_SLIDE_LINKED, id));
			}
		}

		private void validateSlideToNextNote(final int id, final CommonNoteWithFret note,
				final CommonNoteWithFret nextNote) {
			if (nextNote.fret() != note.slideTo()) {
				errorsTab.addError(generateErrorWithTab(Label.NOTE_SLIDES_INTO_WRONG_FRET.format(note.string()), id));
			}
			if (note.finger() != null && nextNote.finger() != null && !note.finger().equals(nextNote.finger())) {
				errorsTab.addError(
						generateErrorWithTab(Label.NOTE_SLIDE_ENDS_ON_DIFFERENT_FINGER.format(note.string()), id));
			}
		}

		private void validateCorrectSlide(final int id, final ChordOrNote sound) {
			sound.notesWithFrets(arrangement.chordTemplates)//
					.filter(n -> n.slideTo() != null)//
					.peek(n -> validateSlideFret(id, n))//
					.peek(n -> validatePitchedWithoutLinkNext(id, n))//
					.filter(n -> n.linkNext())//
					.peek(n -> validateUnpitchedWithLinkNext(id, n))//
					.filter(n -> !n.unpitchedSlide())//
					.map(n -> new Pair<>(n, ChordOrNote.findNextSoundOnString(n.string(), id + 1, level.sounds)))//
					.filter(v -> v.b != null)//
					.map(v -> new Pair<>(v.a, v.b.noteWithFret(v.a.string(), arrangement.chordTemplates).get()))//
					.forEach(v -> validateSlideToNextNote(id, v.a, v.b));
		}

		private void validateCorrectLength(final int id, final ChordOrNote sound) {
			sound.notes().forEach(note -> {
				note.string();
				final ChordOrNote previousSound = ChordOrNote.findPreviousSoundOnString(note.string(), id - 1,
						level.sounds);
				if (previousSound == null || !previousSound.linkNext(note.string())) {
					return;
				}

				if (note.position().equals(note.endPosition())) {
					errorsTab.addError(generateError(Label.LINKED_NOTE_HAS_NO_LENGTH, id));
				}
			});
		}

		private void validateHammerOnFret(final int id, final CommonNoteWithFret n) {
			if (n.hopo() == HOPO.HAMMER_ON && n.fret() == 0) {
				errorsTab.addError(generateError(Label.HAMMER_ON_ON_FRET_ZERO, id));
			}
		}

		private void validateTapFret(final int id, final CommonNoteWithFret n) {
			if (n.hopo() == HOPO.TAP && n.fret() == 0) {
				errorsTab.addError(generateError(Label.TAP_ON_FRET_ZERO, id));
			}
		}

		private void validateCorrectHOPO(final int id, final ChordOrNote sound) {
			sound.notesWithFrets(arrangement.chordTemplates)//
					.filter(n -> n.hopo() != HOPO.NONE)//
					.peek(n -> validateHammerOnFret(id, n))//
					.filter(n -> n.hopo() != HOPO.HAMMER_ON)//
					.peek(n -> validateTapFret(id, n))//
					.filter(n -> n.hopo() == HOPO.PULL_OFF)//
					.forEach(n -> {
						final ChordOrNote previousSound = ChordOrNote.findPreviousSoundOnString(n.string(), id - 1,
								level.sounds);
						final Optional<Integer> previousFret = Optional.ofNullable(previousSound)//
								.flatMap(s -> s.noteWithFret(n.string(), arrangement.chordTemplates))//
								.map(previousNote -> previousNote.fret());
						if (previousFret.isEmpty()) {
							errorsTab.addError(generateError(Label.PULL_OFF_WITHOUT_NOTE_BEFORE, id));
							return;
						}

						if (previousFret.get() <= n.fret()) {
							errorsTab.addError(generateError(Label.PULL_OFF_ON_HIGHER_EQUAL_FRET, id));
						}
					});
		}

		private void validateCorrectHandshape(final int id, final ChordOrNote sound) {
			final HandShape lastHandShape = CollectionUtils.lastBeforeEqual(level.handShapes, sound).find();
			if (lastHandShape == null || lastHandShape.templateId == null
					|| lastHandShape.endPosition().compareTo(sound) < 0) {
				return;
			}

			final ChordTemplate handShapeTemplate = arrangement.chordTemplates.get(lastHandShape.templateId);
			final boolean wrongFrets = sound.notesWithFrets(arrangement.chordTemplates).anyMatch(soundNote -> {
				final Integer fret = handShapeTemplate.frets.get(soundNote.string());

				return fret == null || fret != soundNote.fret();
			});
			final boolean wrongFingers = sound.isNote() ? false
					: sound.notesWithFrets(arrangement.chordTemplates).anyMatch(soundNote -> {
						final Integer finger = handShapeTemplate.fingers.get(soundNote.string());

						return !Objects.equals(finger, soundNote.finger());
					});

			if (wrongFrets) {
				errorsTab.addError(generateErrorWithTab(Label.FRET_DIFFERENT_THAN_IN_ARPEGGIO_HANDSHAPE, id));
			}
			if (wrongFingers) {
				errorsTab.addError(generateErrorWithTab(Label.FINGER_DIFFERENT_THAN_IN_ARPEGGIO_HANDSHAPE, id));
			}
		}

		private void validateNoteFHP(final int id, final ChordOrNote sound) {
			final FHP fhp = CollectionUtils.lastBeforeEqual(level.fhps, sound).find();
			if (fhp == null) {
				errorsTab.addError(generateError(Label.NOTE_WITHOUT_FHP, id));
				return;
			}

			if (sound.isNote()) {
				final int fret = sound.note().fret;
				if (fret > arrangement.capo && (fret < fhp.fret || fret > fhp.topFret())) {
					errorsTab.addError(generateError(Label.NOTE_IN_WRONG_FHP, id));
				}

				return;
			}

			final Chord chord = sound.chord();
			final ChordTemplate template = arrangement.chordTemplates.get(chord.templateId());
			boolean fretOutsideOfFHP = false;
			for (final int string : template.getStrings()) {
				final Integer finger = template.fingers.get(string);
				final int fret = template.frets.get(string);
				if (finger != null && finger == 1 && fret != fhp.fret) {
					errorsTab.addError(generateErrorWithTab(Label.FIRST_FINGER_NOT_ON_FIRST_FHP_FRET, id));
					return;
				}

				if (fret > arrangement.capo && (fret < fhp.fret || fret > fhp.topFret())) {
					fretOutsideOfFHP = true;
				}
			}

			if (fretOutsideOfFHP) {
				errorsTab.addError(generateError(Label.NOTE_IN_WRONG_FHP, id));
			}
		}

		private void validateChordTail(final int id, final ChordOrNote sound) {
			if (!sound.isChord()) {
				return;
			}

			final Chord chord = sound.chord();
			if (chord.chordNotes.size() <= 2) {
				return;
			}

			for (final int string : chord.chordNotes.keySet()) {
				final ChordNote chordNote = chord.chordNotes.get(string);
				if (chordNote.endPosition().equals(chord.position())) {
					continue;
				}

				if (chordNote.linkNext || chordNote.tremolo || chordNote.vibrato || chordNote.slideTo != null
						|| !chordNote.bendValues.isEmpty()) {
					return;
				}

				final ChordOrNote previousSound = ChordOrNote.findPreviousSoundOnString(string, id - 1, level.sounds);
				if (previousSound != null && previousSound.getString(string).get().linkNext()) {
					return;
				}

				errorsTab.addError(generateError(Label.CHORD_WITH_NOTE_TAILS, id));
				return;
			}
		}

		private void validateSounds() {
			for (int i = 0; i < level.sounds.size(); i++) {
				final ChordOrNote sound = level.sounds.get(i);

				validateFrets(i, sound);
				validateCorrectSlide(i, sound);
				validateCorrectLength(i, sound);
				validateCorrectHandshape(i, sound);
				validateCorrectHOPO(i, sound);
				validateNoteFHP(i, sound);
				validateChordTail(i, sound);
			}
		}

		@Override
		public String toString() {
			return "LevelValidator [arrangementId=" + arrangementId + ", levelId=" + levelId + "]";
		}
	}

	public void validate(final int arrangementId, final Arrangement arrangement, final int levelId, final Level level) {
		final LevelValidator levelValidator = new LevelValidator(arrangementId, arrangement, levelId, level);

		levelValidator.validateSounds();
	}
}
