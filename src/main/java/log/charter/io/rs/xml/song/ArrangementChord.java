package log.charter.io.rs.xml.song;

import static java.util.stream.Collectors.minBy;

import java.util.Map.Entry;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamInclude;

import log.charter.io.rs.xml.converters.CountedListConverter.CountedList;
import log.charter.io.rs.xml.converters.TimeConverter;
import log.charter.song.BendValue;
import log.charter.song.ChordTemplate;
import log.charter.song.enums.HOPO;
import log.charter.song.enums.Mute;
import log.charter.song.notes.Chord;
import log.charter.song.notes.IPosition;
import log.charter.util.CollectionUtils.ArrayList2;

@XStreamAlias("chord")
@XStreamInclude(ArrangementChordNote.class)
public class ArrangementChord implements IPosition {
	@XStreamAsAttribute
	@XStreamConverter(TimeConverter.class)
	public int time;
	@XStreamAsAttribute
	public int chordId;
	@XStreamAsAttribute
	public Integer palmMute;
	@XStreamAsAttribute
	public Integer fretHandMute;
	@XStreamAsAttribute
	public Integer accent;
	@XStreamAsAttribute
	public Integer linkNext;
	@XStreamImplicit
	public ArrayList2<ArrangementNote> chordNotes;

	public ArrangementChord() {
	}

	public ArrangementChord(final Chord chord, final ChordTemplate chordTemplate, final int nextPosition) {
		time = chord.position();
		chordId = chord.chordId;
		accent = chord.accent ? 1 : null;
		linkNext = chord.linkNext ? 1 : null;

		setUpMute(chord);
		setChordNotes(chord, chordTemplate, nextPosition);
	}

	private void setUpMute(final Chord chord) {
		if (chord.mute == Mute.STRING) {
			fretHandMute = 1;
		} else if (chord.mute == Mute.PALM) {
			palmMute = 1;
		}
	}

	private void setChordNoteLengths(final int length) {
		for (final ArrangementNote chordNote : chordNotes) {
			chordNote.sustain = length;
		}
	}

	private void setChordNotesSlide(final Chord chord, final ChordTemplate chordTemplate) {
		if (chord.slideTo == null) {
			return;
		}

		populateChordNotes(chordTemplate);
		setChordNoteLengths(chord.length());

		final int minFret = chordTemplate.frets.values().stream().collect(minBy(Integer::compare)).get();
		final int slideDifference = chord.slideTo - minFret;
		for (final ArrangementNote chordNote : chordNotes) {
			if (chord.unpitchedSlide) {
				chordNote.slideUnpitchTo = chordNote.fret + slideDifference;
			} else {
				chordNote.slideTo = chordNote.fret + slideDifference;
			}
		}
	}

	private void setChordNotesTremolo(final Chord chord, final ChordTemplate chordTemplate) {
		if (!chord.tremolo) {
			return;
		}

		populateChordNotes(chordTemplate);
		setChordNoteLengths(chord.length());

		for (final ArrangementNote chordNote : chordNotes) {
			chordNote.tremolo = 1;
		}
	}

	private void setChordNotesHOPO(final Chord chord, final ChordTemplate chordTemplate) {
		if (chord.hopo == HOPO.NONE) {
			return;
		}

		populateChordNotes(chordTemplate);

		for (final ArrangementNote chordNote : chordNotes) {
			if (chord.hopo == HOPO.HAMMER_ON) {
				chordNote.hopo = 1;
				chordNote.hammerOn = 1;
			} else if (chord.hopo == HOPO.PULL_OFF) {
				chordNote.hopo = 1;
				chordNote.pullOff = 1;
			} else if (chord.hopo == HOPO.TAP) {
				chordNote.tap = 1;
			}
		}
	}

	private void setChordNotesBends(final Chord chord, final ChordTemplate chordTemplate) {
		if (chord.bendValues.isEmpty() || chord.bendValues.values().stream().allMatch(list -> list.isEmpty())) {
			return;
		}

		populateChordNotes(chordTemplate);
		setChordNoteLengths(chord.length());

		for (final ArrangementNote chordNote : chordNotes) {
			final ArrayList2<BendValue> bendValues = chord.bendValues.get(chordNote.string);
			if (bendValues == null || bendValues.isEmpty()) {
				continue;
			}

			chordNote.bendValues = new CountedList<>();
			for (final BendValue bendValue : bendValues) {
				if (bendValue.position() >= chordNote.time
						&& bendValue.position() <= chordNote.time + chordNote.sustain) {
					chordNote.bendValues.list.add(new ArrangementBendValue(bendValue, chord.position()));
				}
			}
		}
	}

	private void setChordNotes(final Chord chord, final ChordTemplate chordTemplate, final int nextPosition) {
		setChordNotesSlide(chord, chordTemplate);
		setChordNotesTremolo(chord, chordTemplate);
		setChordNotesHOPO(chord, chordTemplate);
		setChordNotesBends(chord, chordTemplate);

		if (chord.linkNext) {
			populateChordNotes(chordTemplate);

			for (final ArrangementNote chordNote : chordNotes) {
				chordNote.linkNext = 1;
				chordNote.sustain = nextPosition - time;
			}
		}
	}

	@Override
	public int position() {
		return time;
	}

	@Override
	public void position(final int newPosition) {
		time = newPosition;
	}

	public void populateChordNotes(final ChordTemplate chordTemplate) {
		if (chordNotes != null && !chordNotes.isEmpty()) {
			return;
		}
		chordNotes = new ArrayList2<>();
		if (chordTemplate.frets.isEmpty()) {
			return;
		}

		for (final Entry<Integer, Integer> chordFret : chordTemplate.frets.entrySet()) {
			final int string = chordFret.getKey();
			final int fret = chordFret.getValue();

			chordNotes.add(new ArrangementChordNote(time, string, fret));
		}
	}

}
