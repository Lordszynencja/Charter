package log.charter.io.gp.gp7.transformers;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.data.ChordTemplateFingerSetter.setSuggestedFingers;
import static log.charter.util.CollectionUtils.lastBeforeEqual;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import log.charter.data.config.values.InstrumentConfig;
import log.charter.data.song.Arrangement;
import log.charter.data.song.Beat;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.ChordTemplate;
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
import log.charter.util.data.Fraction;

public class ChordCreator extends GP7NoteCreator {
	private final Arrangement arrangement;
	private final Map<Integer, NoteInterface> shouldSetSlideTo;

	private final GP7NotesWithPosition notes;
	private final Chord chord = new Chord(-1);
	private final ChordTemplate template = new ChordTemplate();
	private ChordOrNote preSlide = null;
	private final List<ChordOrNote> notesCreated = new ArrayList<>();

	private GP7Beat gp7Beat;
	private GP7Note gp7Note;
	private FractionalPosition endPosition;
	private ChordNote chordNote;

	public ChordCreator(final ImmutableBeatsMap beats, final Arrangement arrangement,
			final Map<Integer, NoteInterface> shouldSetSlideTo, final GP7NotesWithPosition notes) {
		super(beats);
		this.arrangement = arrangement;
		this.shouldSetSlideTo = shouldSetSlideTo;

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
			setNoteBend(gp7Note, chordNote, chord.position(), endPosition);
		}

		chord.updateTemplate(arrangement.getChordTemplateIdWithSave(template), template);

		return this;
	}

	public List<ChordOrNote> getCreatedSounds() {
		return notesCreated;
	}

}
