package log.charter.services.data.validation;

import java.util.Optional;

import log.charter.data.ChartData;
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
import log.charter.data.song.position.fractional.IConstantFractionalPosition;
import log.charter.gui.components.tabs.errorsTab.ChartError;
import log.charter.gui.components.tabs.errorsTab.ChartError.ChartErrorSeverity;
import log.charter.gui.components.tabs.errorsTab.ChartPositionOnLevel;
import log.charter.gui.components.tabs.errorsTab.ErrorsTab;
import log.charter.services.data.ChartTimeHandler;
import log.charter.services.editModes.ModeManager;
import log.charter.util.CollectionUtils;
import log.charter.util.collections.Pair;

public class GuitarSoundsValidator {
	private ChartData chartData;
	private ChartTimeHandler chartTimeHandler;
	private ErrorsTab errorsTab;
	private ModeManager modeManager;

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

		private ChartError generateError(final String label, final IConstantFractionalPosition position) {
			final ChartPositionOnLevel errorPosition = new ChartPositionOnLevel(chartData, arrangementId, levelId,
					position, chartTimeHandler, modeManager);
			return new ChartError(label, ChartErrorSeverity.ERROR, errorPosition);
		}

		private ChartError generateError(final Label label, final IConstantFractionalPosition position) {
			return generateError(label.label(), position);
		}

		private void validateFrets(final ChordOrNote sound) {
			sound.notesWithFrets(arrangement.chordTemplates).forEach(note -> {
				if (note.fret() < arrangement.capo) {
					final ChartPositionOnLevel errorPosition = new ChartPositionOnLevel(chartData, arrangementId,
							levelId, note.position(), chartTimeHandler, modeManager);
					errorsTab.addError(
							new ChartError(Label.NOTE_FRET_BELOW_CAPO, ChartErrorSeverity.WARNING, errorPosition));
				}
			});
		}

		private void validateSlideFret(final CommonNoteWithFret note) {
			if (note.fret() == 0) {
				errorsTab.addError(generateError(Label.NOTE_SLIDE_FROM_OPEN_STRING, note));
			}
		}

		private void validatePitchedWithoutLinkNext(final CommonNoteWithFret note) {
			if (!note.linkNext() && !note.unpitchedSlide()) {
				errorsTab.addError(generateError(Label.NOTE_SLIDE_NOT_LINKED, note));
			}
		}

		private void validateUnpitchedWithLinkNext(final CommonNoteWithFret note) {
			if (note.unpitchedSlide()) {
				errorsTab.addError(generateError(Label.UNPITCHED_NOTE_SLIDE_LINKED, note));
			}
		}

		private void validateSlideToNextNote(final CommonNoteWithFret note, final CommonNoteWithFret nextNote) {
			if (nextNote.fret() != note.slideTo()) {
				errorsTab.addError(generateError(Label.NOTE_SLIDES_INTO_WRONG_FRET.format(note.string()), note));
			}
			if (note.finger() != null && !note.finger().equals(nextNote.finger())) {
				errorsTab
						.addError(generateError(Label.NOTE_SLIDE_ENDS_ON_DIFFERENT_FINGER.format(note.string()), note));
			}
		}

		private void validateCorrectSlide(final int id, final ChordOrNote sound) {
			sound.notesWithFrets(arrangement.chordTemplates)//
					.filter(n -> n.slideTo() != null)//
					.peek(this::validateSlideFret)//
					.peek(this::validatePitchedWithoutLinkNext)//
					.filter(n -> n.linkNext())//
					.peek(this::validateUnpitchedWithLinkNext)//
					.filter(n -> !n.unpitchedSlide())//
					.map(n -> new Pair<>(n, ChordOrNote.findNextSoundOnString(n.string(), id + 1, level.sounds)))//
					.filter(v -> v.b != null)//
					.map(v -> new Pair<>(v.a, v.b.noteWithFret(v.a.string(), arrangement.chordTemplates).get()))//
					.forEach(v -> validateSlideToNextNote(v.a, v.b));
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
					errorsTab.addError(generateError(Label.LINKED_NOTE_HAS_NO_LENGTH, note));
				}
			});
		}

		private void validateHammerOnFret(final CommonNoteWithFret n) {
			if (n.hopo() == HOPO.HAMMER_ON && n.fret() == 0) {
				errorsTab.addError(generateError(Label.HAMMER_ON_ON_FRET_ZERO, n));
			}
		}

		private void validateTapFret(final CommonNoteWithFret n) {
			if (n.hopo() == HOPO.TAP && n.fret() == 0) {
				errorsTab.addError(generateError(Label.TAP_ON_FRET_ZERO, n));
			}
		}

		private void validateCorrectHOPO(final int id, final ChordOrNote sound) {
			sound.notesWithFrets(arrangement.chordTemplates)//
					.filter(n -> n.hopo() != HOPO.NONE)//
					.peek(this::validateHammerOnFret)//
					.filter(n -> n.hopo() != HOPO.HAMMER_ON)//
					.peek(this::validateTapFret)//
					.filter(n -> n.hopo() == HOPO.PULL_OFF)//
					.forEach(n -> {
						final ChordOrNote previousSound = ChordOrNote.findPreviousSoundOnString(n.string(), id - 1,
								level.sounds);
						final Optional<Integer> previousFret = Optional.ofNullable(previousSound)//
								.flatMap(s -> s.noteWithFret(n.string(), arrangement.chordTemplates))//
								.map(previousNote -> previousNote.fret());
						if (previousFret.isEmpty()) {
							errorsTab.addError(generateError(Label.PULL_OFF_WITHOUT_NOTE_BEFORE, n));
							return;
						}

						if (previousFret.get() <= n.fret()) {
							errorsTab.addError(generateError(Label.PULL_OFF_ON_HIGHER_EQUAL_FRET, n));
						}
					});
		}

		private void validateCorrectHandshape(final ChordOrNote sound) {
			final HandShape lastHandShape = CollectionUtils.lastBeforeEqual(level.handShapes, sound).find();
			if (lastHandShape == null || lastHandShape.templateId == null
					|| lastHandShape.endPosition().compareTo(sound) < 0) {
				return;
			}

			final ChordTemplate handShapeTemplate = arrangement.chordTemplates.get(lastHandShape.templateId);
			if (!handShapeTemplate.arpeggio) {
				if (sound.isNote()) {
					errorsTab.addError(generateError(Label.NOTE_INSIDE_NON_ARPEGGIO_HAND_SHAPE, sound));
				} else if (lastHandShape.templateId != sound.chord().templateId()) {
					errorsTab.addError(generateError(Label.CHORD_INSIDE_WRONG_HANDSHAPE, sound));
				}

				return;
			}

			final boolean wrongFrets = sound.notesWithFrets(arrangement.chordTemplates).anyMatch(soundNote -> {
				final Integer fret = handShapeTemplate.frets.get(soundNote.string());

				return fret == null || fret != soundNote.fret();
			});

			if (wrongFrets) {
				errorsTab.addError(generateError(Label.FRET_DIFFERENT_THAN_IN_ARPEGGIO_HANDSHAPE, sound));
			}
		}

		private void validateNoteFHP(final ChordOrNote sound) {
			final FHP fhp = CollectionUtils.lastBeforeEqual(level.fhps, sound).find();
			if (fhp == null) {
				errorsTab.addError(generateError(Label.NOTE_WITHOUT_FHP, sound));
				return;
			}

			if (sound.isNote()) {
				final int fret = sound.note().fret;
				if (fret > arrangement.capo && (fret < fhp.fret || fret > fhp.topFret())) {
					errorsTab.addError(generateError(Label.NOTE_IN_WRONG_FHP, sound));
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
					errorsTab.addError(generateError(Label.FIRST_FINGER_NOT_ON_FIRST_FHP_FRET, sound));
					return;
				}

				if (fret > arrangement.capo && (fret < fhp.fret || fret > fhp.topFret())) {
					fretOutsideOfFHP = true;
				}
			}

			if (fretOutsideOfFHP) {
				errorsTab.addError(generateError(Label.NOTE_IN_WRONG_FHP, sound));
			}
		}

		private void validateChordTail(final int id, final ChordOrNote sound) {
			if (!sound.isChord()) {
				return;
			}

			final Chord chord = sound.chord();
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

				errorsTab.addError(generateError(Label.CHORD_WITH_NOTE_TAILS, sound));
				return;
			}
		}

		private void validateSounds() {
			for (int i = 0; i < level.sounds.size(); i++) {
				final ChordOrNote sound = level.sounds.get(i);

				validateFrets(sound);
				validateCorrectSlide(i, sound);
				validateCorrectLength(i, sound);
				validateCorrectHandshape(sound);
				validateCorrectHOPO(i, sound);
				validateNoteFHP(sound);
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
