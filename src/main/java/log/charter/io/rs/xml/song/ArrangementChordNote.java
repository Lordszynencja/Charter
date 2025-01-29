package log.charter.io.rs.xml.song;

import static log.charter.util.CollectionUtils.map;

import java.util.stream.Collectors;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.enums.HOPO;
import log.charter.data.song.enums.Harmonic;
import log.charter.data.song.enums.Mute;
import log.charter.data.song.notes.ChordNote;
import log.charter.io.rs.xml.converters.CountedListConverter.CountedList;

@XStreamAlias("chordNote")
public class ArrangementChordNote extends ArrangementNote {
	public ArrangementChordNote(final ImmutableBeatsMap beats, final int string, final int fret,
			final ChordNote chordNote, final boolean ignore) {
		time = (int) chordNote.position(beats);
		sustain = (int) chordNote.endPosition(beats) - time;
		this.string = string;
		this.fret = fret;

		vibrato = chordNote.vibrato ? 1 : null;
		tremolo = chordNote.tremolo ? 1 : null;

		bend = chordNote.bendValues.isEmpty() ? null
				: chordNote.bendValues.stream()
						.map(bendValue -> bendValue.bendValue == null ? 0 : bendValue.bendValue.intValue())
						.collect(Collectors.maxBy(Integer::compare)).orElse(null);
		bendValues = chordNote.bendValues.isEmpty() ? null
				: new CountedList<>(map(chordNote.bendValues, b -> new ArrangementBendValue(beats, b)));

		linkNext = chordNote.linkNext ? 1 : null;
		this.ignore = ignore || fret > 22 ? 1 : null;

		setUpMute(chordNote);
		setUpHOPO(chordNote);
		setUpHarmonic(chordNote);
		setUpSlide(chordNote);
	}

	private void setUpMute(final ChordNote chordNote) {
		if (chordNote.mute == Mute.FULL) {
			mute = 1;
		} else if (chordNote.mute == Mute.PALM) {
			palmMute = 1;
		}
	}

	private void setUpHOPO(final ChordNote chordNote) {
		if (chordNote.hopo == HOPO.HAMMER_ON) {
			hopo = 1;
			hammerOn = 1;
		} else if (chordNote.hopo == HOPO.PULL_OFF) {
			hopo = 1;
			pullOff = 1;
		} else if (chordNote.hopo == HOPO.TAP) {
			tap = 1;
		}
	}

	private void setUpHarmonic(final ChordNote chordNote) {
		if (chordNote.harmonic == Harmonic.NORMAL) {
			harmonic = 1;
		} else if (chordNote.harmonic == Harmonic.PINCH) {
			harmonicPinch = 1;
		}
	}

	private void setUpSlide(final ChordNote chordNote) {
		if (chordNote.slideTo == null) {
			return;
		}

		if (chordNote.unpitchedSlide) {
			slideUnpitchTo = chordNote.slideTo;
		} else {
			slideTo = chordNote.slideTo;
		}
	}
}
