package log.charter.song.notes;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.util.Utils.mapInteger;

import log.charter.io.rs.xml.song.ArrangementBendValue;
import log.charter.io.rs.xml.song.ArrangementChord;
import log.charter.io.rs.xml.song.ArrangementNote;
import log.charter.song.BendValue;
import log.charter.song.enums.HOPO;
import log.charter.song.enums.Harmonic;
import log.charter.song.enums.Mute;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashMap2;

public class Chord extends GuitarSound {
	public int chordId;
	public HashMap2<Integer, ArrayList2<BendValue>> bendValues = new HashMap2<>();

	public Chord(final int pos, final int chordId) {
		super(pos);
		this.chordId = chordId;
	}

	public Chord(final ArrangementChord arrangementChord) {
		super(arrangementChord.time);
		chordId = arrangementChord.chordId;
		mute = Mute.fromArrangmentChord(arrangementChord);
		accent = mapInteger(arrangementChord.accent);
		linkNext = mapInteger(arrangementChord.linkNext);
		ignore = mapInteger(arrangementChord.ignore);

		if (arrangementChord.chordNotes != null) {
			for (final ArrangementNote arrangementNote : arrangementChord.chordNotes) {
				length(max(arrangementNote.sustain == null ? 0 : arrangementNote.sustain, length()));
				if (arrangementNote.slideTo != null) {
					slideTo = slideTo == null ? arrangementNote.slideTo : min(slideTo, arrangementNote.slideTo);
				}
				if (arrangementNote.slideUnpitchTo != null) {
					slideTo = slideTo == null ? arrangementNote.slideUnpitchTo
							: min(slideTo, arrangementNote.slideUnpitchTo);
					unpitchedSlide = true;
				}

				if (mapInteger(arrangementNote.tremolo)) {
					tremolo = true;
				}

				if (mapInteger(arrangementNote.hammerOn)) {
					hopo = HOPO.HAMMER_ON;
				}
				if (mapInteger(arrangementNote.pullOff)) {
					hopo = HOPO.PULL_OFF;
				}
				if (mapInteger(arrangementNote.tap)) {
					hopo = HOPO.TAP;
				}

				if (mapInteger(arrangementNote.harmonic)) {
					harmonic = Harmonic.NORMAL;
				}
				if (mapInteger(arrangementNote.harmonicPinch)) {
					harmonic = Harmonic.PINCH;
				}

				if (arrangementNote.bendValues != null && !arrangementNote.bendValues.list.isEmpty()) {
					final ArrayList2<BendValue> noteBendValues = new ArrayList2<>();
					bendValues.put(arrangementNote.string, noteBendValues);
					for (final ArrangementBendValue bendValue : arrangementNote.bendValues.list) {
						noteBendValues.add(new BendValue(bendValue, arrangementChord.time));
					}
				}
			}
		}
	}

	public Chord(final Chord other) {
		super(other);
		chordId = other.chordId;
		bendValues = other.bendValues.map(i -> i, list -> list.map(BendValue::new));
	}

	public Chord(final int templateId, final Note note) {
		super(note);
		chordId = templateId;
		bendValues.put(note.string, note.bendValues);
	}
}
