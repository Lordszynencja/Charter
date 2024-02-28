package log.charter.io.gp.gp5.transformers;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.data.config.Config.maxStrings;
import static log.charter.data.config.Config.minTailLength;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import log.charter.data.config.Config;
import log.charter.io.gp.gp5.data.GPBeat;
import log.charter.io.gp.gp5.data.GPBend;
import log.charter.io.gp.gp5.data.GPDuration;
import log.charter.io.gp.gp5.data.GPGraceNote;
import log.charter.io.gp.gp5.data.GPNote;
import log.charter.io.gp.gp5.data.GPNoteEffects;
import log.charter.song.Arrangement;
import log.charter.song.BendValue;
import log.charter.song.ChordTemplate;
import log.charter.song.FractionalPosition;
import log.charter.song.HandShape;
import log.charter.song.Level;
import log.charter.song.enums.HOPO;
import log.charter.song.enums.Harmonic;
import log.charter.song.enums.Mute;
import log.charter.song.notes.Chord;
import log.charter.song.notes.ChordNote;
import log.charter.song.notes.ChordOrNote;
import log.charter.song.notes.CommonNote;
import log.charter.song.notes.Note;

public class GP5SoundsTransformer {
	private static class ChordAddingData {
		public boolean setLength = false;
		public boolean unpitchedSlideDown = false;
		public boolean unpitchedSlideUp = false;
	}

	private static void setHOPO(final CommonNote note, final GPBeat gpBeat, final GPNote gpNote,
			final boolean[] wasHOPOStart, final int[] hopoFrom) {
		if (gpBeat.beatEffects.hopo != null && gpBeat.beatEffects.hopo != HOPO.NONE) {
			note.hopo(gpBeat.beatEffects.hopo);
		} else if (wasHOPOStart[gpNote.string]) {
			note.hopo(hopoFrom[gpNote.string] > gpNote.fret ? HOPO.PULL_OFF : HOPO.HAMMER_ON);
		}
	}

	private static void setHarmonic(final CommonNote note, final GPBeat gpBeat, final GPNote gpNote) {
		final GPNoteEffects effects = gpNote.effects;
		if (gpBeat.beatEffects.harmonic != null && gpBeat.beatEffects.harmonic != Harmonic.NONE) {
			note.harmonic(gpBeat.beatEffects.harmonic);
		} else if (effects.harmonic != null) {
			note.harmonic(effects.harmonic);
		}
	}

	private static void setMute(final CommonNote note, final GPNote gpNote) {
		if (gpNote.dead) {
			note.mute(Mute.FULL);
		} else if (gpNote.effects.palmMute) {
			note.mute(Mute.PALM);
		}
	}

	private static void setStatuses(final CommonNote note, final GPBeat gpBeat, final GPNote gpNote,
			final boolean[] wasHOPOStart, final int[] hopoFrom) {
		final GPNoteEffects effects = gpNote.effects;

		note.vibrato(gpBeat.beatEffects.vibrato || effects.vibrato);
		note.tremolo(effects.tremoloPickingSpeed != null);
		if (gpBeat.beatEffects.bassPickingTechnique != null) {
			note.bassPicking(gpBeat.beatEffects.bassPickingTechnique);
		}

		setHOPO(note, gpBeat, gpNote, wasHOPOStart, hopoFrom);
		setHarmonic(note, gpBeat, gpNote);
		setMute(note, gpNote);
	}

	private final Level level;
	private final Arrangement arrangement;

	private ChordOrNote lastSound = null;
	private ChordTemplate lastSoundTemplate = null;
	HandShape lastHandShape = null;

	private boolean addSlideToLastSound = false;

	public GP5SoundsTransformer(final Level level, final Arrangement arrangement) {
		this.level = level;
		this.arrangement = arrangement;
	}

	private void checkPreviousNoteLink(final GPNote gpNote) {
		if (lastSound == null || !lastSound.isNote()) {
			addSlideToLastSound = false;
			return;
		}

		final Note note = lastSound.note;

		if (gpNote.tied) {
			note.linkNext = true;
		}

		if (addSlideToLastSound) {
			lastSound.note.slideTo = gpNote.fret;
			addSlideToLastSound = false;
		}
	}

	private Note addBends(final GPNote gpNote, final Note note, final int noteLength, final List<Note> afterNotes) {
		final GPNoteEffects effects = gpNote.effects;
		if (effects.bends.isEmpty()) {
			return note;
		}

		note.length(noteLength);
		final int endPosition = note.endPosition();

		Note lastNote = note;
		int lastBendValue = 0;
		for (final GPBend bendPoint : effects.bends) {
			if (bendPoint.offset == 0 && bendPoint.value == 0) {
				continue;
			}
			if (bendPoint.offset == 60 && bendPoint.value == lastBendValue) {
				break;
			}

			final int bendPositionOffset = noteLength * bendPoint.offset / 60;

			if (bendPoint.vibrato && !lastNote.vibrato) {
				if (bendPoint.offset == 0) {
					lastNote.vibrato = bendPoint.vibrato;
				} else {
					final Note split = new Note(note.position() + bendPositionOffset, note.string, note.fret);
					split.vibrato = bendPoint.vibrato;
					split.endPosition(endPosition);

					lastNote.linkNext = true;
					lastNote.endPosition(split.position() - 1);

					lastNote = split;
					afterNotes.add(split);
				}
			}

			final int bendPosition = bendPositionOffset - lastNote.position() + note.position();
			final BigDecimal bendValue = new BigDecimal("0.02").multiply(new BigDecimal(bendPoint.value));
			lastNote.bendValues.add(new BendValue(bendPosition, bendValue));

			lastBendValue = bendPoint.value;
		}

		return lastNote;
	}

	private static void addTrill(final GPBeat gpBeat, final GPNote gpNote, final Note note, final int noteStartPosition,
			final int noteLength, final List<Note> afterNotes) {
		final GPNoteEffects effects = gpNote.effects;
		if (effects.trill == null) {
			return;
		}

		final int notes = gpBeat.duration.length / effects.trill.speed.length;
		int trillNotePosition = (int) ((double) noteStartPosition + noteLength / (double) notes);
//			NotePositionInformation trillNotePosition = position.move(effects.trill.speed);
		for (int i = 1; i < notes; i++) {
			final int fret = gpNote.fret + (i % 2) * effects.trill.value;
			final Note trillNote = new Note(trillNotePosition, note.string, fret);
//				final Note trillNote = new Note(trillNotePosition.getPosition(), note.string, fret);
			trillNote.hopo = i % 2 == 0 ? HOPO.PULL_OFF : HOPO.HAMMER_ON;
			trillNote.ignore = true;
			afterNotes.add(trillNote);

			trillNotePosition += (int) (noteLength / (double) notes);
//				trillNotePosition = trillNotePosition.move(effects.trill.speed);
		}
	}

	private void addSlideOut(final GPNoteEffects effects, final Note lastNote, final int endPosition) {
		if (effects.slideOut == null) {
			return;
		}

		switch (effects.slideOut) {
			case OUT_DOWN:
				lastNote.slideTo = max(1, lastNote.fret - 5);
				lastNote.unpitchedSlide = true;
				break;
			case OUT_UP:
				lastNote.slideTo = min(Config.frets, lastNote.fret + 5);
				lastNote.unpitchedSlide = true;
				break;
			case OUT_WITHOUT_PLUCK:
				lastNote.linkNext = true;
				addSlideToLastSound = true;
				break;
			case OUT_WITH_PLUCK:
				lastNote.endPosition(endPosition);
				addSlideToLastSound = true;
				break;
			default:
				break;
		}
	}

	private void addSlideIn(final GPNoteEffects effects, final Note note, final List<Note> afterNotes) {
		if (effects.slideIn == null) {
			return;
		}

		switch (effects.slideIn) {
			case IN_FROM_ABOVE:
				final Note slideInNoteFromAbove = new Note(note.position() + 50, note.string, note.fret);
				slideInNoteFromAbove.endPosition(max(slideInNoteFromAbove.position() + 25, note.endPosition()));
				afterNotes.add(slideInNoteFromAbove);
				note.linkNext = true;
				note.slideTo = note.fret;
				note.fret = min(Config.frets, note.fret + 5);
				note.endPosition(slideInNoteFromAbove.position() - 1);
				break;
			case IN_FROM_BELOW:
				final Note slideInNoteFromBelow = new Note(note.position() + 50, note.string, note.fret);
				slideInNoteFromBelow.endPosition(max(slideInNoteFromBelow.position() + 25, note.endPosition()));
				afterNotes.add(slideInNoteFromBelow);
				note.linkNext = true;
				note.slideTo = note.fret;
				note.fret = max(1, note.fret - 5);
				note.endPosition(slideInNoteFromBelow.position() - 1);
				break;
			default:
				break;
		}
	}

	private void addGraceNote(final GPBeat gpBeat, final FractionalPosition position, final GPNoteEffects effects,
			final Note note) {
		if (effects.graceNote == null) {
			return;
		}

		Note graceNote = null;
		final GPGraceNote graceNoteData = effects.graceNote;
		if (graceNoteData.beforeBeat) {
			final int graceNotePosition = position.moveBackwards(graceNoteData.duration).getPosition();
			graceNote = new Note(graceNotePosition, note.string, graceNoteData.fret);
		} else {
			graceNote = new Note(note.position(), note.string, graceNoteData.fret);

			final int noteEndPosition = note.endPosition();
			note.position(position.move(graceNoteData.duration).getPosition());
			note.endPosition(noteEndPosition);
		}

		if (graceNoteData.dead) {
			graceNote.mute = Mute.FULL;
		}
		if (graceNoteData.slide) {
			graceNote.slideTo = note.fret;
			graceNote.linkNext = true;
			graceNote.endPosition(note.position() - 1);
			note.length(max(note.length(), minTailLength));
		}
		if (graceNoteData.legato) {
			note.hopo = graceNote.fret < note.fret ? HOPO.HAMMER_ON : HOPO.PULL_OFF;
		}

		if (graceNote != null) {
			level.sounds.add(new ChordOrNote(graceNote));
		}
	}

	public void addNote(final GPBeat gpBeat, final FractionalPosition position, final FractionalPosition endPosition,
			final boolean[] wasHOPOStart, final int[] hopoFrom) {
		if (gpBeat.notes.size() != 1) {
			return;
		}

		final GPNote gpNote = gpBeat.notes.get(0);
		if (gpNote.fret < 0 || gpNote.fret > Config.frets) {
			return;
		}
		if (gpNote.string < 0 || gpNote.string > maxStrings) {
			return;
		}

		checkPreviousNoteLink(gpNote);

		final int noteStartPosition = position.getPosition();
		final Note note = new Note(position.getPosition(), gpNote.string - 1, gpNote.fret);
		final int noteLength = endPosition.getPosition() - note.position();
		final GPNoteEffects effects = gpNote.effects;

		setStatuses(CommonNote.create(note), gpBeat, gpNote, wasHOPOStart, hopoFrom);
		if (note.vibrato || note.tremolo) {
			note.length(noteLength);
		}

		note.accent = gpNote.accent;
		note.ignore = gpNote.ghost;

		final List<Note> afterNotes = new ArrayList<>();
		final Note lastNote = addBends(gpNote, note, noteLength, afterNotes);

		addTrill(gpBeat, gpNote, note, noteStartPosition, noteLength, afterNotes);
		addSlideOut(effects, lastNote, noteStartPosition + noteLength);
		addSlideIn(effects, note, afterNotes);
		addGraceNote(gpBeat, position, effects, note);

		level.sounds.add(new ChordOrNote(note));
		afterNotes.forEach(afterNote -> level.sounds.add(new ChordOrNote(afterNote)));

		lastSound = level.sounds.getLast();
		lastSoundTemplate = null;
		lastHandShape = null;
	}

	private void checkPreviousSoundForChord(final GPBeat gpBeat) {
		if (lastSound == null || !lastSound.isChord() || lastSoundTemplate == null) {
			addSlideToLastSound = false;
			return;
		}

		final Chord chord = lastSound.chord;
		for (final GPNote gpNote : gpBeat.notes) {
			if (gpNote.tied) {
				chord.chordNotes.values().forEach(n -> n.linkNext = true);
				break;
			}
		}

		for (final GPNote gpNote : gpBeat.notes) {
			if (addSlideToLastSound) {
				final int string = gpNote.string - 1;
				if (lastSoundTemplate.frets.get(string) != null && lastSoundTemplate.frets.get(string) != 0) {
					final ChordNote previousNote = chord.chordNotes.get(string);
					previousNote.slideTo = gpNote.fret;
				}
			}
		}

		addSlideToLastSound = false;
	}

	private void addChordNoteBends(final GPNoteEffects effects, final ChordAddingData chordAddingData,
			final ChordNote chordNote, final int length) {
		if (effects.bends.isEmpty()) {
			return;
		}

		chordAddingData.setLength = true;

		int lastBendValue = 0;
		for (final GPBend bendPoint : effects.bends) {
			if (bendPoint.offset == 0 && bendPoint.value == 0) {
				continue;
			}
			if (bendPoint.offset == 60 && bendPoint.value == lastBendValue) {
				break;
			}
			chordNote.vibrato |= bendPoint.vibrato;

			final int bendPosition = length * bendPoint.offset / 60;
			final BigDecimal bendValue = new BigDecimal("0.02").multiply(new BigDecimal(bendPoint.value));
			chordNote.bendValues.add(new BendValue(bendPosition, bendValue));

			lastBendValue = bendPoint.value;
		}
	}

	private void addChordNoteSlideOut(final GPNoteEffects effects, final ChordAddingData chordAddingData,
			final ChordNote chordNote, final int fret) {
		if (effects.slideOut == null || fret == 0) {
			return;
		}

		switch (effects.slideOut) {
			case OUT_DOWN:
				chordAddingData.unpitchedSlideDown = true;
				break;
			case OUT_UP:
				chordAddingData.unpitchedSlideUp = true;
				break;
			case OUT_WITHOUT_PLUCK:
				chordNote.linkNext = true;
				addSlideToLastSound = true;
				break;
			case OUT_WITH_PLUCK:
				chordAddingData.setLength = true;
				addSlideToLastSound = true;
				break;
			default:
				break;
		}
	}

	private void addChordNotesLengthIfNeeded(final ChordAddingData chordAddingData, final Chord chord,
			final int length) {
		if (chordAddingData.setLength) {
			chord.chordNotes.values().forEach(n -> n.length = length);
		}
	}

	private void addChordUnpitchedSlide(final Chord chord, final ChordTemplate chordTemplate, final int fretDistance) {
		chord.chordNotes.forEach((string, note) -> {
			final int fret = chordTemplate.frets.get(string);
			if (fret == 0) {
				return;
			}

			note.slideTo = max(1, fret + fretDistance);
			note.unpitchedSlide = true;
		});
	}

	public void addChord(final GPBeat gpBeat, final FractionalPosition position, final FractionalPosition endPosition,
			final boolean[] wasHOPOStart, final int[] hOPOFrom) {
		final ChordTemplate chordTemplate = new ChordTemplate();
		chordTemplate.chordName = gpBeat.chord == null ? "" : gpBeat.chord.chordName;
		if (chordTemplate.chordName == null) {
			chordTemplate.chordName = "";
		}
		checkPreviousSoundForChord(gpBeat);

		final Chord chord = new Chord(position.getPosition(), -1, chordTemplate);
		final int length = endPosition.getPosition() - chord.position();
		final ChordAddingData chordAddingData = new ChordAddingData();

		for (final GPNote gpNote : gpBeat.notes) {
			final int string = gpNote.string - 1;
			chordTemplate.fingers.put(string, gpNote.finger == -1 ? null : gpNote.finger);
			chordTemplate.frets.put(string, gpNote.fret);

			final ChordNote chordNote = new ChordNote();
			chord.chordNotes.put(string, chordNote);
			setStatuses(CommonNote.create(chord, string, chordNote), gpBeat, gpNote, wasHOPOStart, hOPOFrom);
			if (chordNote.vibrato || chordNote.tremolo) {
				chordAddingData.setLength = true;
			}
			chord.ignore |= gpNote.ghost;

			final GPNoteEffects effects = gpNote.effects;
			addChordNoteBends(effects, chordAddingData, chordNote, length);
			addChordNoteSlideOut(effects, chordAddingData, chordNote, gpNote.fret);
		}

		addChordNotesLengthIfNeeded(chordAddingData, chord, length);
		if (chordAddingData.unpitchedSlideDown) {
			addChordUnpitchedSlide(chord, chordTemplate, -5);
		} else if (chordAddingData.unpitchedSlideUp) {
			addChordUnpitchedSlide(chord, chordTemplate, 5);
		}

		final int templateId = arrangement.getChordTemplateIdWithSave(chordTemplate);
		chord.updateTemplate(templateId, chordTemplate);
		level.sounds.add(new ChordOrNote(chord));

		lastSound = level.sounds.getLast();
		lastSoundTemplate = chordTemplate;

		final int handshapeEndPosition = endPosition.moveBackwards(GPDuration.NOTE_32).getPosition();

		if (lastHandShape != null && lastHandShape.templateId == chord.templateId()) {
			lastHandShape.endPosition(handshapeEndPosition);
		} else {
			lastHandShape = new HandShape(chord, handshapeEndPosition - chord.position());
			level.handShapes.add(lastHandShape);
		}
	}
}
