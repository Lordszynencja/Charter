package log.charter.io.gp.gp5.transformers;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.util.CollectionUtils.max;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import log.charter.data.config.values.InstrumentConfig;
import log.charter.data.song.Arrangement;
import log.charter.data.song.BendValue;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.HandShape;
import log.charter.data.song.Level;
import log.charter.data.song.enums.HOPO;
import log.charter.data.song.enums.Harmonic;
import log.charter.data.song.enums.Mute;
import log.charter.data.song.notes.Chord;
import log.charter.data.song.notes.ChordNote;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.notes.CommonNote;
import log.charter.data.song.notes.Note;
import log.charter.data.song.position.FractionalPosition;
import log.charter.io.gp.gp5.GP5FractionalPosition;
import log.charter.io.gp.gp5.data.GPBeat;
import log.charter.io.gp.gp5.data.GPBend;
import log.charter.io.gp.gp5.data.GPGraceNote;
import log.charter.io.gp.gp5.data.GPNote;
import log.charter.io.gp.gp5.data.GPNoteEffects;
import log.charter.util.data.Fraction;

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

	private boolean checkPreviousNoteLink(final GPNote gpNote) {
		if (lastSound == null || !lastSound.isNote()) {
			addSlideToLastSound = false;
			return false;
		}

		final Note note = lastSound.note();

		boolean linked = false;
		if (gpNote.tied) {
			note.linkNext = true;
			linked = true;
		}

		if (addSlideToLastSound) {
			lastSound.note().slideTo = gpNote.fret;
			addSlideToLastSound = false;
			linked = true;
		}

		return linked;
	}

	private Note addBends(final GPNote gpNote, final Note note, final FractionalPosition endPosition,
			final FractionalPosition noteLength, final List<Note> afterNotes) {
		final GPNoteEffects effects = gpNote.effects;
		if (effects.bends.isEmpty()) {
			return note;
		}

		note.endPosition(endPosition);

		Note lastNote = note;
		int lastBendValue = 0;
		for (final GPBend bendPoint : effects.bends) {
			if (bendPoint.offset == 0 && bendPoint.value == 0) {
				continue;
			}
			if (bendPoint.offset == 60 && bendPoint.value == lastBendValue) {
				break;
			}

			final FractionalPosition bendPosition = note.position()
					.add(noteLength.multiply(new Fraction(bendPoint.offset, 60)));

			if (bendPoint.vibrato && !lastNote.vibrato) {
				if (bendPoint.offset == 0) {
					lastNote.vibrato = bendPoint.vibrato;
				} else {
					final Note split = new Note(bendPosition, note.string, note.fret);
					split.vibrato = bendPoint.vibrato;
					split.endPosition(endPosition);

					lastNote.linkNext = true;

					lastNote = split;
					afterNotes.add(split);
				}
			}

			final BigDecimal bendValue = new BigDecimal("0.02").multiply(new BigDecimal(bendPoint.value));
			lastNote.bendValues.add(new BendValue(bendPosition, bendValue));

			lastBendValue = bendPoint.value;
		}

		return lastNote;
	}

	private static void addTrill(final GPBeat gpBeat, final GPNote gpNote, final Note note,
			final FractionalPosition noteLength, final List<Note> afterNotes) {
		final GPNoteEffects effects = gpNote.effects;
		if (effects.trill == null) {
			return;
		}

		final int notes = gpBeat.duration.length / effects.trill.speed.length;
		for (int i = 1; i < notes; i++) {
			final FractionalPosition position = note.position().add(noteLength.multiply(new Fraction(i, notes)));
			final int fret = (i % 2 == 0 ? note.fret : effects.trill.value);
			final Note trillNote = new Note(position, note.string, fret);
			trillNote.hopo = i % 2 == 0 ? HOPO.PULL_OFF : HOPO.HAMMER_ON;
			trillNote.ignore = true;
			afterNotes.add(trillNote);
		}
	}

	private void addSlideOut(final GPNoteEffects effects, final Note lastNote, final FractionalPosition endPosition) {
		if (effects.slideOut == null) {
			return;
		}

		switch (effects.slideOut) {
			case OUT_DOWN:
				lastNote.slideTo = max(1, lastNote.fret - 5);
				lastNote.unpitchedSlide = true;
				break;
			case OUT_UP:
				lastNote.slideTo = min(InstrumentConfig.frets, lastNote.fret + 5);
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
				final Note slideInNoteFromAbove = new Note(note.position().add(new Fraction(1, 4)), note.string,
						note.fret);
				slideInNoteFromAbove.endPosition(
						max(slideInNoteFromAbove.position().add(new Fraction(1, 8)), note.endPosition().position()));
				afterNotes.add(slideInNoteFromAbove);
				note.linkNext = true;
				note.slideTo = note.fret;
				note.fret = min(InstrumentConfig.frets, note.fret + 5);
				break;
			case IN_FROM_BELOW:
				final Note slideInNoteFromBelow = new Note(note.position().add(new Fraction(1, 4)), note.string,
						note.fret);
				slideInNoteFromBelow.endPosition(
						max(slideInNoteFromBelow.position().add(new Fraction(1, 8)), note.endPosition().position()));
				afterNotes.add(slideInNoteFromBelow);
				note.linkNext = true;
				note.slideTo = note.fret;
				note.fret = max(1, note.fret - 5);
				break;
			default:
				break;
		}
	}

	private void addGraceNote(final GPBeat gpBeat, final GP5FractionalPosition position, final GPNoteEffects effects,
			final Note note) {
		if (effects.graceNote == null) {
			return;
		}

		Note graceNote = null;
		final GPGraceNote graceNoteData = effects.graceNote;
		if (graceNoteData.beforeBeat) {
			final FractionalPosition graceNotePosition = position.moveBackwards(graceNoteData.duration).position();
			graceNote = new Note(graceNotePosition, note.string, graceNoteData.fret);
		} else {
			graceNote = new Note(note.position(), note.string, graceNoteData.fret);

			final FractionalPosition noteEndPosition = note.endPosition().position();
			note.position(position.move(graceNoteData.duration).position());
			note.endPosition(noteEndPosition);
		}

		if (graceNoteData.dead) {
			graceNote.mute = Mute.FULL;
		}
		if (graceNoteData.slide) {
			graceNote.slideTo = note.fret;
			graceNote.linkNext = true;
			note.endPosition(max(note.endPosition(), note.position().add(new Fraction(1, 8))));
		}
		if (graceNoteData.legato) {
			note.hopo = graceNote.fret < note.fret ? HOPO.HAMMER_ON : HOPO.PULL_OFF;
		}

		if (graceNote != null) {
			level.sounds.add(ChordOrNote.from(graceNote));
		}
	}

	public void addNote(final GPBeat gpBeat, final GP5FractionalPosition position,
			final GP5FractionalPosition endPosition, final boolean[] wasHOPOStart, final int[] hopoFrom) {
		if (gpBeat.notes.size() != 1) {
			return;
		}

		final GPNote gpNote = gpBeat.notes.get(0);
		if (gpNote.fret < 0 || gpNote.fret > InstrumentConfig.frets) {
			return;
		}
		if (gpNote.string < 0 || gpNote.string > InstrumentConfig.maxStrings) {
			return;
		}

		final boolean linked = checkPreviousNoteLink(gpNote);

		final FractionalPosition length = position.distance(endPosition);
		final Note note = new Note(position.position(), gpNote.string - 1, gpNote.fret);
		if (linked || length.compareTo(new Fraction(1, 2)) > 0) {
			note.endPosition(endPosition.position());
		}
		final GPNoteEffects effects = gpNote.effects;

		setStatuses(new CommonNote(note), gpBeat, gpNote, wasHOPOStart, hopoFrom);
		if (note.vibrato || note.tremolo) {
			note.endPosition(endPosition.position());
		}

		note.accent = gpNote.accent;
		note.ignore = gpNote.ghost;

		final List<Note> afterNotes = new ArrayList<>();
		final Note lastNote = addBends(gpNote, note, endPosition.position(), length, afterNotes);

		addTrill(gpBeat, gpNote, note, length, afterNotes);
		addSlideOut(effects, lastNote, endPosition.position());
		addSlideIn(effects, note, afterNotes);
		addGraceNote(gpBeat, position, effects, note);

		level.sounds.add(ChordOrNote.from(note));
		afterNotes.forEach(afterNote -> level.sounds.add(ChordOrNote.from(afterNote)));

		lastSound = level.sounds.get(level.sounds.size() - 1);
		lastSoundTemplate = null;
		lastHandShape = null;
	}

	private void checkPreviousSoundForChord(final GPBeat gpBeat) {
		if (lastSound == null || !lastSound.isChord() || lastSoundTemplate == null) {
			addSlideToLastSound = false;
			return;
		}

		final Chord chord = lastSound.chord();
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
			final ChordNote chordNote, final FractionalPosition endPosition) {
		if (effects.bends.isEmpty()) {
			return;
		}

		chordAddingData.setLength = true;
		final FractionalPosition length = chordNote.position().distance(endPosition);

		int lastBendValue = 0;
		for (final GPBend bendPoint : effects.bends) {
			if (bendPoint.offset == 0 && bendPoint.value == 0) {
				continue;
			}
			if (bendPoint.offset == 60 && bendPoint.value == lastBendValue) {
				break;
			}
			chordNote.vibrato |= bendPoint.vibrato;

			final FractionalPosition bendPosition = length.multiply(new Fraction(bendPoint.offset, 60));
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
			final FractionalPosition endPosition) {
		if (chordAddingData.setLength) {
			chord.chordNotes.values().forEach(n -> n.endPosition(endPosition));
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

	private void addHandShape(final FractionalPosition position, final FractionalPosition endPosition,
			final int templateId) {
		if (lastHandShape != null && lastHandShape.templateId == templateId) {
			lastHandShape.endPosition(endPosition);
		} else {
			lastHandShape = new HandShape(position, endPosition, templateId);
			level.handShapes.add(lastHandShape);
		}
	}

	public void addChord(final GPBeat gpBeat, final FractionalPosition position, final FractionalPosition endPosition,
			final boolean[] wasHOPOStart, final int[] hOPOFrom) {
		final ChordTemplate chordTemplate = new ChordTemplate();
		chordTemplate.chordName = gpBeat.chord == null ? "" : gpBeat.chord.chordName;
		if (chordTemplate.chordName == null) {
			chordTemplate.chordName = "";
		}
		checkPreviousSoundForChord(gpBeat);

		final Chord chord = new Chord(position.position(), -1, chordTemplate);
		final ChordAddingData chordAddingData = new ChordAddingData();

		Map<Integer, Integer> lastFrets = new HashMap<>();
		if (lastSound != null) {
			if (lastSound.isNote()) {
				lastFrets.put(lastSound.note().string, lastSound.note().fret);
			} else if (lastSoundTemplate != null) {
				lastFrets = new HashMap<>(lastSoundTemplate.frets);
			}
		}

		for (final GPNote gpNote : gpBeat.notes) {
			final int string = gpNote.string - 1;
			chordTemplate.fingers.put(string, gpNote.finger == -1 ? null : gpNote.finger);
			final int fret = gpNote.tied && lastFrets.get(string) != null ? lastFrets.get(string) : gpNote.fret;
			chordAddingData.setLength |= gpNote.tied;
			chordTemplate.frets.put(string, fret);

			final ChordNote chordNote = new ChordNote(chord);
			chord.chordNotes.put(string, chordNote);
			setStatuses(new CommonNote(chord, string), gpBeat, gpNote, wasHOPOStart, hOPOFrom);
			if (chordNote.vibrato || chordNote.tremolo) {
				chordAddingData.setLength = true;
			}
			chord.ignore |= gpNote.ghost;

			final GPNoteEffects effects = gpNote.effects;
			addChordNoteBends(effects, chordAddingData, chordNote, endPosition);
			addChordNoteSlideOut(effects, chordAddingData, chordNote, gpNote.fret);
		}

		addChordNotesLengthIfNeeded(chordAddingData, chord, endPosition);
		if (chordAddingData.unpitchedSlideDown) {
			addChordUnpitchedSlide(chord, chordTemplate, -5);
		} else if (chordAddingData.unpitchedSlideUp) {
			addChordUnpitchedSlide(chord, chordTemplate, 5);
		}

		final int templateId = arrangement.getChordTemplateIdWithSave(chordTemplate);
		chord.updateTemplate(templateId, chordTemplate);
		level.sounds.add(ChordOrNote.from(chord));

		lastSound = level.sounds.get(level.sounds.size() - 1);
		lastSoundTemplate = chordTemplate;

		addHandShape(position, endPosition, templateId);
	}
}
