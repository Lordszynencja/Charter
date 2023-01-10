package log.charter.song.notes;

import static log.charter.util.Utils.mapInteger;

import log.charter.io.rs.xml.song.ArrangementNote;
import log.charter.song.BendValue;
import log.charter.song.enums.BassPickingTechnique;
import log.charter.song.enums.HOPO;
import log.charter.song.enums.Harmonic;
import log.charter.song.enums.Mute;
import log.charter.util.CollectionUtils.ArrayList2;

public class Note extends GuitarSound {
	public int string;
	public int fret;
	public Integer vibrato;
	public boolean tremolo;
	public BassPickingTechnique bassPicking = BassPickingTechnique.NONE;
	public ArrayList2<BendValue> bendValues = new ArrayList2<>();

	public Note(final int pos, final int string, final int fret) {
		super(pos);
		this.string = string;
		this.fret = fret;
	}

	public Note(final ArrangementNote arrangementNote) {
		super(arrangementNote.time, arrangementNote.sustain == null ? 0 : arrangementNote.sustain,
				Mute.fromArrangmentNote(arrangementNote), HOPO.fromArrangmentNote(arrangementNote),
				Harmonic.fromArrangmentNote(arrangementNote), mapInteger(arrangementNote.accent),
				mapInteger(arrangementNote.tremolo), mapInteger(arrangementNote.linkNext),
				arrangementNote.slideTo == null ? arrangementNote.slideUnpitchTo : arrangementNote.slideTo,
				arrangementNote.slideUnpitchTo != null);
		string = arrangementNote.string;
		fret = arrangementNote.fret;
		vibrato = arrangementNote.vibrato;
		bassPicking = BassPickingTechnique.fromArrangmentNote(arrangementNote);
		bendValues = arrangementNote.bendValues == null ? new ArrayList2<>()
				: arrangementNote.bendValues.list
						.map(arrangementBendValue -> new BendValue(arrangementBendValue, arrangementNote.time));
	}

	public Note(final Note other) {
		super(other);
		string = other.string;
		fret = other.fret;
		vibrato = other.vibrato;
		bassPicking = other.bassPicking;
		bendValues = other.bendValues.map(BendValue::new);
	}
}
