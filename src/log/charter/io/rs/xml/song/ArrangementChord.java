package log.charter.io.rs.xml.song;

import static java.util.stream.Collectors.minBy;
import static log.charter.io.rs.xml.song.ArrangementChordNote.forBend;
import static log.charter.io.rs.xml.song.ArrangementChordNote.forSlide;
import static log.charter.io.rs.xml.song.ArrangementChordNote.forUnpitchedSlide;

import java.util.Map.Entry;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamInclude;

import log.charter.io.rs.xml.converters.TimeConverter;
import log.charter.song.Chord;
import log.charter.util.CollectionUtils.ArrayList2;

@XStreamAlias("chord")
@XStreamInclude(ArrangementChordNote.class)
public class ArrangementChord {
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
	public ArrayList2<ArrangementChordNote> chordNotes;

	public ArrangementChord() {
	}

	private void setChordNotes(final Chord chord, final ChordTemplate chordTemplate) {
		final int position = chord.position;
		final int length = chord.length;

		if (chord.slideTo != null) {
			chordNotes = new ArrayList2<>();
			if (chordTemplate.frets.isEmpty()) {
				return;
			}

			final int minFret = chordTemplate.frets.values().stream().collect(minBy(Integer::compare)).get();
			final int slideDifference = chord.slideTo - minFret;
			for (final Entry<Integer, Integer> chordFret : chordTemplate.frets.entrySet()) {
				final int string = chordFret.getKey();
				final int fret = chordFret.getValue();
				chordNotes.add(forSlide(position, string, fret, length, fret + slideDifference));
			}

			return;
		}

		if (chord.unpitchedSlideTo != null) {
			chordNotes = new ArrayList2<>();
			if (chordTemplate.frets.isEmpty()) {
				return;
			}

			final int minFret = chordTemplate.frets.values().stream().collect(minBy(Integer::compare)).get();
			final int slideDifference = chord.unpitchedSlideTo - minFret;
			for (final Entry<Integer, Integer> chordFret : chordTemplate.frets.entrySet()) {
				final int string = chordFret.getKey();
				final int fret = chordFret.getValue();
				chordNotes.add(forUnpitchedSlide(position, string, fret, length, fret + slideDifference));
			}

			return;
		}

		if (!chord.bendValues.isEmpty()) {
			chordNotes = new ArrayList2<>();
			for (final Entry<Integer, Integer> chordFret : chordTemplate.frets.entrySet()) {
				final int string = chordFret.getKey();
				final int fret = chordFret.getValue();
				chordNotes.add(forBend(position, string, fret, length, chord.bendValues.get(string)));
			}

			return;
		}

		chordNotes = null;
	}

	public ArrangementChord(final Chord chord, final ChordTemplate chordTemplate) {
		time = chord.position;
		chordId = chord.chordId;
		palmMute = chord.palmMute ? 1 : null;
		fretHandMute = chord.fretHandMute ? 1 : null;
		accent = chord.accent ? 1 : null;
		linkNext = chord.linkNext ? 1 : null;

		setChordNotes(chord, chordTemplate);
	}
}
