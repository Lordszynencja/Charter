package log.charter.io.rs.xml.song;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.util.CollectionUtils.map;

import java.math.BigDecimal;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.BendValue;
import log.charter.data.song.enums.HOPO;
import log.charter.data.song.enums.Harmonic;
import log.charter.data.song.enums.Mute;
import log.charter.data.song.notes.ChordNote;
import log.charter.io.rs.xml.converters.CountedListConverter.CountedList;

@XStreamAlias("chordNote")
public class ArrangementChordNote extends ArrangementNote {

	private Integer bend(final List<BendValue> bendValues) {
		if (bendValues.isEmpty()) {
			return null;
		}

		int bend = 0;
		for (final BendValue bendValue : bendValues) {
			if (bendValue.bendValue.compareTo(BigDecimal.ZERO) == 0) {
				continue;
			}
			bend = max(1, min(3, max(bend, bendValue.bendValue.intValue())));
		}

		if (bend == 0) {
			return null;
		}

		return bend;
	}

	private CountedList<ArrangementBendValue> bendValues(final ImmutableBeatsMap beats,
			final List<BendValue> bendValues) {
		if (bendValues.isEmpty() || bend == null) {
			return null;
		}

		return new CountedList<>(map(bendValues, b -> new ArrangementBendValue(beats, b)));
	}

	public ArrangementChordNote(final ImmutableBeatsMap beats, final int string, final int fret,
			final ChordNote chordNote, final boolean ignore) {
		time = (int) chordNote.position(beats);
		sustain = (int) chordNote.endPosition(beats) - time;
		this.string = string;
		this.fret = fret;

		vibrato = chordNote.vibrato ? 1 : null;
		tremolo = chordNote.tremolo ? 1 : null;

		bend = bend(chordNote.bendValues);
		bendValues = bendValues(beats, chordNote.bendValues);

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
