package log.charter.io.gp.gp7.transformers;

import log.charter.io.gp.gp7.data.GP7Staff;
import log.charter.io.gp.gp7.data.GP7Track;
import log.charter.io.rs.xml.song.ArrangementType;

class TrackInfo {
	public static boolean isImportableTrack(final GP7Track gp7Track) {
		if (gp7Track.staves == null || gp7Track.staves.size() != 1) {
			return false;
		}
		final String instrument = gp7Track.staves.get(0).getInstrument();
		return "Guitar".equals(instrument) || "Bass".equals(instrument);
	}

	public final String name;
	public final int capoFret;
	public final int fretCount;
	public final int partialCapoFret;
	public final boolean[] partialCapoStringFlags;
	public final int[] tuningValues;
	public final ArrangementType type;

	public TrackInfo(final GP7Track gp7Track) {
		name = gp7Track.name;

		final GP7Staff staff = gp7Track.staves.get(0);
		capoFret = staff.capoFret();
		fretCount = staff.fretCount();
		partialCapoFret = staff.partialCapoFret();
		partialCapoStringFlags = staff.partialCapoStringFlags();
		tuningValues = staff.getTuningValues();
		if ("Guitar".equals(staff.getInstrument())) {
			if (gp7Track.name.toLowerCase().contains("rhythm") || gp7Track.name.toLowerCase().contains("rythm")) {
				type = ArrangementType.Rhythm;
			} else {
				type = ArrangementType.Lead;
			}
		} else if ("Bass".equals(staff.getInstrument())) {
			type = ArrangementType.Bass;
		} else {
			type = ArrangementType.Combo;
		}
	}

}
