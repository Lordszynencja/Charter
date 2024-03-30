package log.charter.data.song.notes;

import java.util.List;

import log.charter.data.song.BendValue;
import log.charter.data.song.enums.BassPickingTechnique;
import log.charter.data.song.enums.HOPO;
import log.charter.data.song.enums.Harmonic;
import log.charter.data.song.enums.Mute;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.fractional.IConstantFractionalPositionWithEnd;

public interface NoteInterface extends IConstantFractionalPositionWithEnd {
	void endPosition(FractionalPosition newEndPosition);

	BassPickingTechnique bassPicking();

	void bassPicking(BassPickingTechnique value);

	Mute mute();

	void mute(Mute value);

	HOPO hopo();

	void hopo(HOPO value);

	Harmonic harmonic();

	void harmonic(Harmonic value);

	boolean vibrato();

	void vibrato(boolean value);

	boolean tremolo();

	void tremolo(boolean value);

	boolean linkNext();

	void linkNext(boolean value);

	Integer slideTo();

	void slideTo(Integer value);

	boolean unpitchedSlide();

	void unpitchedSlide(boolean value);

	List<BendValue> bendValues();

	void bendValues(List<BendValue> value);
}
