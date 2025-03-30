package log.charter.io.gp.gp7.transformers;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.util.CollectionUtils.lastBeforeEqual;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import log.charter.data.config.values.InstrumentConfig;
import log.charter.data.song.Arrangement;
import log.charter.data.song.Beat;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.enums.BassPickingTechnique;
import log.charter.data.song.enums.HOPO;
import log.charter.data.song.enums.Harmonic;
import log.charter.data.song.enums.Mute;
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

public class SingleNoteCreator extends GP7NoteCreator {
	private final Arrangement arrangement;
	private final Map<Integer, NoteInterface> shouldSetSlideTo;

	private final FractionalPosition position;
	private final GP7Beat gp7Beat;
	private final GP7Note gp7Note;
	private final FractionalPosition endPosition;
	private List<Note> notesCreated;
	private Note note = new Note();

	public SingleNoteCreator(final ImmutableBeatsMap beats, final Arrangement arrangement,
			final Map<Integer, NoteInterface> shouldSetSlideTo, final GP7NotesWithPosition notes) {
		super(beats);
		this.arrangement = arrangement;
		this.shouldSetSlideTo = shouldSetSlideTo;

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
		setNoteBend(gp7Note, note, position, endPosition);

		return this;
	}

	public List<ChordOrNote> getCreatedSounds() {
		return notesCreated.stream().<ChordOrNote>map(ChordOrNote::from).toList();
	}

}
