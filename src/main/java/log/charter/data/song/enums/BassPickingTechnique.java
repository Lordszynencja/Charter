package log.charter.data.song.enums;

import log.charter.io.rs.xml.song.ArrangementNote;

public enum BassPickingTechnique {
	POP, SLAP, NONE;

	public static BassPickingTechnique fromArrangmentNote(final ArrangementNote note) {
		if (note.pluck != null && note.pluck == 1) {
			return POP;
		}
		if (note.slap != null && note.slap == 1) {
			return SLAP;
		}

		return NONE;
	}
}
