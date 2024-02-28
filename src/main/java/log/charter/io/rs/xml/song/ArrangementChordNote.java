package log.charter.io.rs.xml.song;

import java.util.stream.Collectors;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.io.rs.xml.converters.CountedListConverter.CountedList;
import log.charter.song.enums.HOPO;
import log.charter.song.enums.Harmonic;
import log.charter.song.enums.Mute;
import log.charter.song.notes.ChordNote;

@XStreamAlias("chordNote")
public class ArrangementChordNote extends ArrangementNote {
	public ArrangementChordNote(final int time, final int length, final int string, final int fret,
			final ChordNote chordNote, final boolean ignore) {
		this.time = time;
		sustain = length;
		this.string = string;
		this.fret = fret;

		vibrato = chordNote.vibrato ? 1 : null;
		tremolo = chordNote.tremolo ? 1 : null;

		bend = chordNote.bendValues.isEmpty() ? null
				: chordNote.bendValues.stream()
						.map(bendValue -> bendValue.bendValue == null ? 0 : bendValue.bendValue.intValue())
						.collect(Collectors.maxBy(Integer::compare)).orElse(null);
		bendValues = chordNote.bendValues.isEmpty() ? null
				: new CountedList<>(chordNote.bendValues.map(bendValue -> new ArrangementBendValue(bendValue, time)));

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
