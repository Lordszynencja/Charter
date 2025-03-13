package log.charter.io.gp.gp7.transformers;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.data.ChordTemplateFingerSetter.setSuggestedFingers;
import static log.charter.data.song.configs.Tuning.getStringDistance;
import static log.charter.services.ArrangementFretHandPositionsCreator.createFHPs;
import static log.charter.util.CollectionUtils.lastBeforeEqual;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import log.charter.data.config.values.InstrumentConfig;
import log.charter.data.song.Arrangement;
import log.charter.data.song.Beat;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.BendValue;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.Level;
import log.charter.data.song.configs.Tuning;
import log.charter.data.song.configs.Tuning.TuningType;
import log.charter.data.song.enums.BassPickingTechnique;
import log.charter.data.song.enums.HOPO;
import log.charter.data.song.enums.Harmonic;
import log.charter.data.song.enums.Mute;
import log.charter.data.song.notes.Chord;
import log.charter.data.song.notes.ChordNote;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.notes.Note;
import log.charter.data.song.notes.NoteInterface;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.time.Position;
import log.charter.io.gp.gp7.data.GP7Beat;
import log.charter.io.gp.gp7.data.GP7Note;
import log.charter.io.gp.gp7.data.GP7Note.GP7HarmonicType;
import log.charter.io.gp.gp7.data.GP7Note.SlideInType;
import log.charter.io.gp.gp7.data.GP7Note.SlideOutType;
import log.charter.io.gp.gp7.transformers.GP7NotesWithPosition.GP7BeatWithNoteAndEndPosition;
import log.charter.io.rs.xml.song.ArrangementType;
import log.charter.util.data.Fraction;

class GP7TrackToArrangementTransformer {
	public static Arrangement transform(final ImmutableBeatsMap beats, final Track track) {
		return new GP7TrackToArrangementTransformer(beats, track).transform();
	}

	private final ImmutableBeatsMap beats;
	private final Track track;

	private final Arrangement arrangement = new Arrangement();

	private final Map<Integer, NoteInterface> shouldSetSlideTo = new HashMap<>();

	private GP7TrackToArrangementTransformer(final ImmutableBeatsMap beats, final Track track) {
		this.beats = beats;
		this.track = track;
	}

	private void setTuning() {
		if (track.trackInfo.tuningValues.length == 0) {
			return;
		}

		final boolean bass = track.trackInfo.type == ArrangementType.Bass;
		final int[] tuningValues = track.trackInfo.tuningValues;

		final int strings = tuningValues.length;
		final int[] convertedTuning = new int[strings];

		for (int string = 0; string < strings; string++) {
			convertedTuning[string] = tuningValues[string] - (bass ? 28 : 40) - getStringDistance(string, strings)
					+ track.trackInfo.capoFret;
		}

		final TuningType tuningType = TuningType.fromTuning(convertedTuning);
		arrangement.tuning = new Tuning(tuningType, strings, convertedTuning);
	}

	private class SingleNoteCreator {
		private final FractionalPosition position;
		private final GP7Beat gp7Beat;
		private final GP7Note gp7Note;
		private final FractionalPosition endPosition;
		private List<Note> notesCreated;
		private Note note = new Note();

		public SingleNoteCreator(final GP7NotesWithPosition notes) {
			position = notes.position;

			final GP7BeatWithNoteAndEndPosition gp7NoteWithEnd = notes.notes.get(0);
			gp7Beat = gp7NoteWithEnd.beat;
			gp7Note = gp7NoteWithEnd.note;
			endPosition = gp7NoteWithEnd.endPosition;
		}

		private void setNoteHOPO() {
			if (gp7Note.tapped) {
				note.hopo = HOPO.TAP;
				return;
			}

			if (gp7Note.leftHandTapped) {
				note.hopo = HOPO.HAMMER_ON;
				return;
			}

			if (!gp7Note.hopoDestination) {
				return;
			}

			final ChordOrNote previousNote = ChordOrNote.findPreviousSoundOnString(note.string,
					arrangement.getLevel(0).sounds.size() - 1, arrangement.getLevel(0).sounds);

			if (previousNote == null) {
				note.hopo = HOPO.HAMMER_ON;
				return;
			}

			if (previousNote.isNote()) {
				note.hopo = previousNote.note().fret > note.fret ? HOPO.PULL_OFF : HOPO.HAMMER_ON;
				return;
			}

			final ChordTemplate template = arrangement.chordTemplates.get(previousNote.chord().templateId());
			note.hopo = template.frets.get(note.string) > note.fret ? HOPO.PULL_OFF : HOPO.HAMMER_ON;
		}

		private void setNoteAccent() {
			note.accent = gp7Note.accent;
			note.mute = gp7Note.mute ? Mute.FULL : //
					gp7Note.palmMute ? Mute.PALM : //
							Mute.NONE;

			if (gp7Note.popped) {
				note.bassPicking = BassPickingTechnique.POP;
			} else if (gp7Note.slapped) {
				note.bassPicking = BassPickingTechnique.SLAP;
			}
		}

		private void setNoteHarmonic() {
			if (!gp7Note.harmonic) {
				return;
			}

			note.harmonic = gp7Note.harmonicType == GP7HarmonicType.PINCH ? Harmonic.PINCH : Harmonic.NORMAL;
		}

		private void setNoteLinkNext() {
			if (!gp7Note.tieOrigin) {
				return;
			}

			note.linkNext = true;
		}

		private void setNoteVibrato() {
			if (!gp7Note.vibrato) {
				return;
			}

			note.vibrato = true;
			note.endPosition(endPosition);
		}

		private void setNoteTremolo() {
			if (!gp7Beat.tremolo) {
				return;
			}

			note.tremolo = true;
			note.endPosition(endPosition);
		}

		private void addSlideInFromBelow() {
			Beat beat = lastBeforeEqual(beats, new Position(position.position(beats))).find();
			if (beat == null) {
				beat = new Beat(0);
			}
			final Fraction toAdd = new Fraction(beat.noteDenominator, 16);

			note.position(position.add(toAdd));
			final Note preNote = new Note(position, note.position());
			preNote.string = note.string;
			preNote.fret = max(1, note.fret - 2);
			preNote.linkNext = true;
			preNote.slideTo = note.fret;
			notesCreated.add(0, preNote);
		}

		private void addSlideInFromAbove() {
			Beat beat = lastBeforeEqual(beats, new Position(position.position(beats))).find();
			if (beat == null) {
				beat = new Beat(0);
			}
			final Fraction toAdd = new Fraction(beat.noteDenominator, 16);

			note.position(position.add(toAdd));
			final Note preNote = new Note(position, note.position());
			preNote.string = note.string;
			preNote.fret = min(InstrumentConfig.frets, note.fret + 2);
			preNote.linkNext = true;
			preNote.slideTo = note.fret;
			notesCreated.add(0, preNote);
		}

		private void setNoteSlide() {
			if (gp7Note.slideIn == SlideInType.NONE && gp7Note.slideOut == SlideOutType.NONE) {
				return;
			}

			note.endPosition(endPosition);

			switch (gp7Note.slideIn) {
				case FROM_BELOW -> addSlideInFromBelow();
				case FROM_ABOVE -> addSlideInFromAbove();
				default -> {}
			}

			switch (gp7Note.slideOut) {
				case TO_NEXT_NOTE_LINKED:
					note.linkNext = true;
				case TO_NEXT_NOTE:
					shouldSetSlideTo.put(note.string, note);
					break;
				case DOWN:
					note.slideTo = max(1, note.fret - 5);
					note.unpitchedSlide = true;
					break;
				case UP:
					note.slideTo = min(InstrumentConfig.frets, note.fret + 5);
					note.unpitchedSlide = true;
					break;
				default:
					break;
			}
		}

		private BendValue generateBendValue(final double offset, final double value) {
			final double startPosition = note.position(beats);
			final double endPosition = note.endPosition(beats);
			final int bendTime = (int) (startPosition * (100 - offset) + endPosition * offset) / 100;
			final FractionalPosition bendPosition = FractionalPosition.fromTime(beats, bendTime);
			final BigDecimal bendValue = new BigDecimal(value / 25).setScale(2, RoundingMode.HALF_UP);

			return new BendValue(bendPosition, bendValue);
		}

		private void setNoteBend() {
			if (!gp7Note.bend) {
				return;
			}

			note.endPosition(endPosition);

			if (gp7Note.bendOriginValue != 0) {
				note.bendValues.add(generateBendValue(0, gp7Note.bendOriginValue));
			}

			if (gp7Note.bendOriginOffset > 0) {
				note.bendValues.add(generateBendValue(gp7Note.bendOriginOffset, gp7Note.bendOriginValue));
			}

			note.bendValues.add(generateBendValue(gp7Note.bendDestinationOffset, gp7Note.bendDestinationValue));
		}

		public SingleNoteCreator generateNote() {
			notesCreated = new ArrayList<>();
			notesCreated.add(new Note(position, gp7Note.string, gp7Note.fret));
			note = notesCreated.get(0);

			if (shouldSetSlideTo.containsKey(note.string)) {
				final NoteInterface previousNote = shouldSetSlideTo.remove(note.string);
				previousNote.slideTo(note.fret);
				note.endPosition(endPosition);
			}

			setNoteHOPO();
			setNoteAccent();
			setNoteHarmonic();
			setNoteLinkNext();
			setNoteVibrato();
			setNoteTremolo();
			setNoteSlide();
			setNoteBend();

			return this;
		}

		public List<ChordOrNote> getCreatedSounds() {
			return notesCreated.stream().<ChordOrNote>map(ChordOrNote::from).toList();
		}
	}

	private class ChordCreator {
		private final GP7NotesWithPosition notes;
		private final Chord chord = new Chord(-1);
		private final ChordTemplate template = new ChordTemplate();
		private ChordOrNote preSlide = null;
		private final List<ChordOrNote> notesCreated = new ArrayList<>();

		private GP7Beat gp7Beat;
		private GP7Note gp7Note;
		private FractionalPosition endPosition;
		private ChordNote chordNote;

		public ChordCreator(final GP7NotesWithPosition notes) {
			this.notes = notes;

			chord.position(notes.position);
		}

		private int string() {
			return gp7Note.string;
		}

		private int fret() {
			return gp7Note.fret;
		}

		private void setNoteHOPO() {
			if (gp7Note.tapped) {
				chordNote.hopo = HOPO.TAP;
				return;
			}

			if (gp7Note.leftHandTapped) {
				chordNote.hopo = HOPO.HAMMER_ON;
				return;
			}

			if (!gp7Note.hopoDestination) {
				return;
			}

			final ChordOrNote previousNote = ChordOrNote.findPreviousSoundOnString(string(),
					arrangement.getLevel(0).sounds.size() - 1, arrangement.getLevel(0).sounds);

			if (previousNote == null) {
				chordNote.hopo = HOPO.HAMMER_ON;
				return;
			}

			if (previousNote.isNote()) {
				chordNote.hopo = previousNote.note().fret > fret() ? HOPO.PULL_OFF : HOPO.HAMMER_ON;
				return;
			}

			final ChordTemplate template = arrangement.chordTemplates.get(previousNote.chord().templateId());
			chordNote.hopo = template.frets.get(string()) > fret() ? HOPO.PULL_OFF : HOPO.HAMMER_ON;
		}

		private void setNoteAccent() {
			if (gp7Note.accent) {
				chord.accent = true;
			}

			chordNote.mute = gp7Note.mute ? Mute.FULL : //
					gp7Note.palmMute ? Mute.PALM : //
							Mute.NONE;
		}

		private void setNoteHarmonic() {
			if (!gp7Note.harmonic) {
				return;
			}

			chordNote.harmonic = gp7Note.harmonicType == GP7HarmonicType.PINCH ? Harmonic.PINCH : Harmonic.NORMAL;
		}

		private void setNoteLinkNext() {
			if (!gp7Note.tieOrigin) {
				return;
			}

			chordNote.linkNext = true;
		}

		private void setNoteVibrato() {
			if (!gp7Note.vibrato) {
				return;
			}

			chordNote.vibrato = true;
			chordNote.endPosition(endPosition);
		}

		private void setNoteTremolo() {
			if (!gp7Beat.tremolo) {
				return;
			}

			chordNote.tremolo = true;
			chordNote.endPosition(endPosition);
		}

		private void addPreSlideNote(final Note preNote) {
			if (preSlide == null) {
				preSlide = ChordOrNote.from(preNote);
				notesCreated.add(0, preSlide);
				return;
			}

			final ChordOrNote sound = preSlide;
			final Chord preChord;
			final ChordTemplate preChordTemplate;
			if (sound.isNote()) {
				preChordTemplate = new ChordTemplate();
				preChordTemplate.frets.put(sound.note().string, sound.note().fret);
				preChord = sound.asChord(-1, preChordTemplate).chord();
			} else {
				preChord = sound.chord();
				preChordTemplate = new ChordTemplate(arrangement.chordTemplates.get(preChord.templateId()));
			}

			preChordTemplate.frets.put(preNote.string, preNote.fret);
			final ChordNote preChordNote = chord.chordNotes.get(preNote.string);
			preChordNote.linkNext = preNote.linkNext;
			preChordNote.slideTo = preNote.slideTo;
			setSuggestedFingers(preChordTemplate);

			final int preChordId = arrangement.getChordTemplateIdWithSave(preChordTemplate);
			notesCreated.set(0, sound.asChord(preChordId, preChordTemplate));
		}

		private void addSlideInFromBelow() {
			Beat beat = lastBeforeEqual(beats, new Position(notes.position.position(beats))).find();
			if (beat == null) {
				beat = new Beat(0);
			}

			if (preSlide == null) {
				final Fraction toAdd = new Fraction(beat.noteDenominator, 16);
				chord.position(notes.position.add(toAdd));
			}

			final Note preNote = new Note(notes.position, chord.position());
			preNote.string = string();
			preNote.fret = max(1, fret() - 2);
			preNote.linkNext = true;
			preNote.slideTo = fret();
			addPreSlideNote(preNote);
		}

		private void addSlideInFromAbove() {
			Beat beat = lastBeforeEqual(beats, new Position(notes.position.position(beats))).find();
			if (beat == null) {
				beat = new Beat(0);
			}

			if (preSlide == null) {
				final Fraction toAdd = new Fraction(beat.noteDenominator, 16);
				chord.position(notes.position.add(toAdd));
			}

			final Note preNote = new Note(notes.position, chord.position());
			preNote.string = string();
			preNote.fret = min(InstrumentConfig.frets, fret() + 2);
			preNote.linkNext = true;
			preNote.slideTo = fret();
			addPreSlideNote(preNote);
		}

		private void setNoteSlide() {
			if (gp7Note.slideIn == SlideInType.NONE && gp7Note.slideOut == SlideOutType.NONE) {
				return;
			}

			chordNote.endPosition(endPosition);

			switch (gp7Note.slideIn) {
				case FROM_BELOW -> addSlideInFromBelow();
				case FROM_ABOVE -> addSlideInFromAbove();
				default -> {}
			}

			switch (gp7Note.slideOut) {
				case TO_NEXT_NOTE_LINKED:
					chordNote.linkNext = true;
				case TO_NEXT_NOTE:
					shouldSetSlideTo.put(string(), chordNote);
					break;
				case DOWN:
					chordNote.slideTo = max(1, fret() - 5);
					chordNote.unpitchedSlide = true;
					break;
				case UP:
					chordNote.slideTo = min(InstrumentConfig.frets, fret() + 5);
					chordNote.unpitchedSlide = true;
					break;
				default:
					break;
			}
		}

		private BendValue generateBendValue(final double offset, final double value) {
			final double startPosition = chord.position(beats);
			final double endPosition = chordNote.endPosition(beats);
			final int bendTime = (int) (startPosition * (100 - offset) + endPosition * offset) / 100;
			final FractionalPosition bendPosition = FractionalPosition.fromTime(beats, bendTime);
			final BigDecimal bendValue = new BigDecimal(value / 25).setScale(2, RoundingMode.HALF_UP);

			return new BendValue(bendPosition, bendValue);
		}

		private void setNoteBend() {
			if (!gp7Note.bend) {
				return;
			}

			chordNote.endPosition(endPosition);

			if (gp7Note.bendOriginValue != 0) {
				chordNote.bendValues.add(generateBendValue(0, gp7Note.bendOriginValue));
			}

			if (gp7Note.bendOriginOffset > 0) {
				chordNote.bendValues.add(generateBendValue(gp7Note.bendOriginOffset, gp7Note.bendOriginValue));
			}

			chordNote.bendValues.add(generateBendValue(gp7Note.bendDestinationOffset, gp7Note.bendDestinationValue));
		}

		public ChordCreator setChordValues() {
			notesCreated.add(ChordOrNote.from(chord));

			for (final GP7BeatWithNoteAndEndPosition note : notes.notes) {
				gp7Beat = note.beat;
				gp7Note = note.note;
				endPosition = note.endPosition;

				if (gp7Note.finger >= 0) {
					template.fingers.put(string(), gp7Note.finger);
				}
				template.frets.put(string(), fret());

				chordNote = new ChordNote(chord);
				chord.chordNotes.put(string(), chordNote);

				if (shouldSetSlideTo.containsKey(string())) {
					final NoteInterface previousNote = shouldSetSlideTo.remove(string());
					previousNote.slideTo(fret());
					chordNote.endPosition(endPosition);
				}

				setNoteHOPO();
				setNoteAccent();
				setNoteHarmonic();
				setNoteLinkNext();
				setNoteVibrato();
				setNoteTremolo();
				setNoteSlide();
				setNoteBend();
			}

			chord.updateTemplate(arrangement.getChordTemplateIdWithSave(template), template);

			return this;
		}

		public List<ChordOrNote> getCreatedSounds() {
			return notesCreated;
		}
	}

	private void addNotes() {
		final Level level = arrangement.getLevel(0);

		for (final GP7NotesWithPosition notes : track.notes) {
			if (notes.notes.isEmpty()) {
				continue;
			}

			final List<ChordOrNote> sounds = switch (notes.notes.size()) {
				case 1 -> new SingleNoteCreator(notes).generateNote().getCreatedSounds();
				default -> new ChordCreator(notes).setChordValues().getCreatedSounds();
			};

			level.sounds.addAll(sounds);
		}

		createFHPs(beats, arrangement.chordTemplates, level.sounds, level.fhps);
	}

	private Arrangement transform() {
		arrangement.arrangementType = track.trackInfo.type;
		setTuning();
		arrangement.capo = track.trackInfo.capoFret;
		addNotes();

		return arrangement;
	}
}
