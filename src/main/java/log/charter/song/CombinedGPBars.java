package log.charter.song;

import log.charter.io.gp.gp5.GPMasterBar;
import log.charter.io.gp.gp5.GPBeat;
import log.charter.util.CollectionUtils.ArrayList2;

public class CombinedGPBars {
	public GPMasterBar bar;
	public ArrayList2<Beat> bar_beats;
	public ArrayList2<GPBeat> note_beats;
	public int gp_bar_id;
	public int available_space_in_64ths;
	public int notes_in_bar;

	public CombinedGPBars(final GPMasterBar bar, final int id) {
		this.bar = bar;
		this.gp_bar_id = id;
		this.bar_beats = new ArrayList2<>();
		this.note_beats = new ArrayList2<>();
	}
}
