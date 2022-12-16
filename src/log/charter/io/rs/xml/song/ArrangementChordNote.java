package log.charter.io.rs.xml.song;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.song.BendValue;
import log.charter.util.CollectionUtils.ArrayList2;

@XStreamAlias("chordNote")
public class ArrangementChordNote extends ArrangementNote {
	public static ArrangementChordNote forSlide(final int time, final int string, final int fret, final int sustain,
			final int slideTo) {
		final ArrangementChordNote note = new ArrangementChordNote(time, string, fret);
		note.sustain = sustain;
		note.slideTo = slideTo;

		return note;
	}

	public static ArrangementChordNote forUnpitchedSlide(final int time, final int string, final int fret,
			final int sustain, final int slideTo) {
		final ArrangementChordNote note = new ArrangementChordNote(time, string, fret);
		note.sustain = sustain;
		note.slideUnpitchTo = slideTo;

		return note;
	}

	public static ArrangementChordNote forBend(final int time, final int string, final int fret, final int sustain,
			final ArrayList2<BendValue> bendValues) {
		final ArrangementChordNote note = new ArrangementChordNote(time, string, fret);
		note.sustain = sustain;
		for (final BendValue bendValue : bendValues) {
			if (bendValue.position >= time && bendValue.position <= time + sustain) {
				note.bendValues.list.add(new ArrangementBendValue(bendValue));
			}
		}

		return note;
	}

	public ArrangementChordNote() {
	}

	public ArrangementChordNote(final int time, final int string, final int fret) {
		this.string = string;
		this.fret = fret;
	}
}
