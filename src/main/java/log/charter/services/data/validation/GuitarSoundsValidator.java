package log.charter.services.data.validation;

import java.util.Optional;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.Arrangement;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.HandShape;
import log.charter.data.song.Level;
import log.charter.data.song.enums.HOPO;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.notes.Note;
import log.charter.data.song.position.fractional.IConstantFractionalPosition;
import log.charter.gui.components.tabs.errorsTab.ChartError;
import log.charter.gui.components.tabs.errorsTab.ChartError.ChartErrorSeverity;
import log.charter.gui.components.tabs.errorsTab.ChartError.ChartPosition;
import log.charter.gui.components.tabs.errorsTab.ErrorsTab;
import log.charter.util.CollectionUtils;

public class GuitarSoundsValidator {
	private ChartData chartData;
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

		private ChartError generateError(final Label label, final IConstantFractionalPosition position) {
			return new ChartError(label, ChartErrorSeverity.ERROR,
					new ChartPosition(chartData, arrangementId, levelId, position));
		}

		private void validateCorrectNoteSlide(final int id, final Note note) {
			if (note.slideTo == null) {
				return;
			}
			if (note.fret == 0) {
				errorsTab.addError(generateError(Label.NOTE_SLIDE_FROM_OPEN_STRING, note));
			}

			if (!note.linkNext) {
				if (!note.unpitchedSlide) {
					errorsTab.addError(generateError(Label.NOTE_SLIDE_NOT_LINKED, note));
				}
				return;
			}

			if (note.unpitchedSlide) {
				errorsTab.addError(generateError(Label.UNPITCHED_NOTE_SLIDE_LINKED, note));
				return;
			}

			final ChordOrNote nextSound = ChordOrNote.findNextSoundOnString(note.string, id + 1, level.sounds);

			if (nextSound.isChord()) {
				errorsTab.addError(generateError(Label.NOTE_SLIDES_INTO_CHORD, note));
			} else if (nextSound.note().fret != note.slideTo) {
				errorsTab.addError(generateError(Label.NOTE_SLIDES_INTO_WRONG_FRET, note));
			}
		}

		private void validateCorrectSlide(final int id, final ChordOrNote sound) {
			if (sound.isNote()) {
				validateCorrectNoteSlide(id, sound.note());
			} else {
				// TODO add chords' slides validation
			}
		}

		private void validateCorrectHOPO(final int id, final ChordOrNote sound) {
			sound.notesWithFrets(arrangement.chordTemplates).forEach(n -> {
				if (n.hopo() == HOPO.NONE) {
					return;
				}

				if (n.hopo() == HOPO.HAMMER_ON) {
					if (n.fret() == 0) {
						errorsTab.addError(generateError(Label.HAMMER_ON_ON_FRET_ZERO, n));
					}

					return;
				}
				if (n.hopo() == HOPO.TAP) {
					if (n.fret() == 0) {
						errorsTab.addError(generateError(Label.TAP_ON_FRET_ZERO, n));
					}

					return;
				}
				if (n.hopo() != HOPO.PULL_OFF) {
					return;
				}

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

		private void validateChordsInsideCorrectHandshapes(final ChordOrNote sound) {
			if (!sound.isChord()) {
				return;
			}

			final int templateId = sound.chord().templateId();
			final HandShape lastHandShape = CollectionUtils.lastBeforeEqual(level.handShapes, sound).find();
			if (lastHandShape == null || lastHandShape.templateId == null
					|| lastHandShape.endPosition().compareTo(sound) < 0) {
				return;// is fixed by ArrangementFixer
			}

			final ChordTemplate handShapeTemplate = arrangement.chordTemplates.get(lastHandShape.templateId);
			if (handShapeTemplate.arpeggio) {
				return;
			}

			if (lastHandShape.templateId != templateId) {
				errorsTab.addError(generateError(Label.CHORD_INSIDE_WRONG_HANDSHAPE, sound));
			}
		}

		private void validateSounds() {
			for (int i = 0; i < level.sounds.size(); i++) {
				final ChordOrNote sound = level.sounds.get(i);

				validateCorrectSlide(i, sound);
				validateChordsInsideCorrectHandshapes(sound);
				validateCorrectHOPO(i, sound);
			}
		}

		@Override
		public String toString() {
			return "LevelValidator [arrangementId=" + arrangementId + ", levelId=" + levelId + "]";
		}
	}

	public void validateGuitarSounds(final int arrangementId, final Arrangement arrangement, final int levelId,
			final Level level) {
		final LevelValidator levelValidator = new LevelValidator(arrangementId, arrangement, levelId, level);

		levelValidator.validateSounds();
	}
}
