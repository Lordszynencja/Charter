package log.charter.io.rs.xml.song;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamInclude;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.enums.Mute;
import log.charter.data.song.notes.Chord;
import log.charter.data.song.notes.Chord.ChordNotesVisibility;
import log.charter.data.song.notes.ChordNote;
import log.charter.data.song.position.time.IPosition;
import log.charter.io.rs.xml.converters.TimeConverter;

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
	@XStreamAsAttribute
	public Integer ignore;
	@XStreamImplicit
	public List<ArrangementChordNote> chordNotes;

	public ArrangementChord(final ImmutableBeatsMap beats, final Chord chord, final ChordTemplate chordTemplate,
			final boolean forceAddNotes) {
		time = (int) chord.position(beats);
		chordId = chord.templateId();
		accent = chord.accent ? 1 : null;
		linkNext = chord.linkNext() ? 1 : null;
		ignore = chord.ignore ? 1 : null;
		for (final int fret : chordTemplate.frets.values()) {
			if (fret > 22) {
				ignore = 1;
			}
		}

		final ChordNotesVisibility chordNotesVisibility = chord.chordNotesVisibility(forceAddNotes);
		if (chordNotesVisibility == ChordNotesVisibility.NONE) {
			setUpMute(chord);
		} else {
			populateChordNotes(beats, chordTemplate, chord);
		}
	}

	private void setUpMute(final Chord chord) {
		final Mute mute = chord.chordNotesValue(n -> n.mute, Mute.NONE);
		if (mute == Mute.FULL) {
			fretHandMute = 1;
		} else if (mute == Mute.PALM) {
			palmMute = 1;
		}
	}

	@Override
	public double position() {
		return time;
	}

	@Override
	public void position(final double newPosition) {
		time = (int) newPosition;
	}

	public void populateChordNotes(final ImmutableBeatsMap beats, final ChordTemplate chordTemplate,
			final Chord chord) {
		if (chordNotes != null && !chordNotes.isEmpty()) {
			return;
		}

		chordNotes = new ArrayList<>();
		if (chordTemplate.frets.isEmpty()) {
			return;
		}

		for (final Entry<Integer, Integer> chordFret : chordTemplate.frets.entrySet()) {
			final int string = chordFret.getKey();
			final int fret = chordFret.getValue();

			final ChordNote chordNote = chord.chordNotes.get(string);
			final ArrangementChordNote arrangementChordNote = new ArrangementChordNote(beats, string, fret, chordNote,
					chord.ignore);

			chordNotes.add(arrangementChordNote);
		}
	}

}
