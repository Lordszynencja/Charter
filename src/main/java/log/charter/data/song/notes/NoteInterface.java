package log.charter.data.song.notes;

import log.charter.data.song.BendValue;
import log.charter.data.song.enums.BassPickingTechnique;
import log.charter.data.song.enums.HOPO;
import log.charter.data.song.enums.Harmonic;
import log.charter.data.song.enums.Mute;
import log.charter.util.collections.ArrayList2;

public interface NoteInterface {
	int length();

	void length(int value);

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

	ArrayList2<BendValue> bendValues();

	void bendValues(ArrayList2<BendValue> value);
}
