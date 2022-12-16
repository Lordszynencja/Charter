package log.charter.song;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.util.Utils.mapInteger;

import log.charter.io.rs.xml.song.ArrangementBendValue;
import log.charter.io.rs.xml.song.ArrangementChord;
import log.charter.io.rs.xml.song.ArrangementNote;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashMap2;

public class Chord extends Position {
	public int chordId;
	public int length;
	public boolean palmMute;
	public boolean fretHandMute;
	public boolean accent;
	public boolean linkNext;
	public Integer slideTo;
	public Integer unpitchedSlideTo;
	public HashMap2<Integer, ArrayList2<BendValue>> bendValues = new HashMap2<>();

	public Chord(final int pos, final int chordId) {
		super(pos);
		this.chordId = chordId;
	}

	public Chord(final ArrangementChord arrangementChord) {// TODO
		super(arrangementChord.time);
		chordId = arrangementChord.chordId;
		palmMute = mapInteger(arrangementChord.palmMute);
		fretHandMute = mapInteger(arrangementChord.fretHandMute);
		accent = mapInteger(arrangementChord.accent);
		linkNext = mapInteger(arrangementChord.linkNext);

		if (arrangementChord.chordNotes != null) {
			for (final ArrangementNote arrangementNote : arrangementChord.chordNotes) {
				length = max(arrangementNote.sustain == null ? 0 : arrangementNote.sustain, length);
				if (arrangementNote.slideTo != null) {
					slideTo = slideTo == null ? arrangementNote.slideTo : min(slideTo, arrangementNote.slideTo);
				}
				if (arrangementNote.slideUnpitchTo != null) {
					unpitchedSlideTo = unpitchedSlideTo == null ? arrangementNote.slideUnpitchTo
							: min(unpitchedSlideTo, arrangementNote.slideUnpitchTo);
				}

				if (arrangementNote.bendValues != null && !arrangementNote.bendValues.list.isEmpty()) {
					final ArrayList2<BendValue> noteBendValues = new ArrayList2<>();
					bendValues.put(arrangementNote.string, noteBendValues);
					for (final ArrangementBendValue bendValue : arrangementNote.bendValues.list) {
						noteBendValues.add(new BendValue(bendValue));
					}
				}
			}
		}
	}

	public Chord(final Chord other) {
		super(other);
		chordId = other.chordId;
		length = other.length;
		palmMute = other.palmMute;
		fretHandMute = other.fretHandMute;
		accent = other.accent;
		linkNext = other.linkNext;
		slideTo = other.slideTo;
		unpitchedSlideTo = other.unpitchedSlideTo;
		bendValues = other.bendValues.map(i -> i, list -> list.map(BendValue::new));
	}
}
