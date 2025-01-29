package log.charter.io.gp.gp7.transformers;

import java.util.List;

class Track {
	public final TrackInfo trackInfo;
	public final List<GP7NotesWithPosition> notes;

	public Track(final TrackInfo trackInfo, final List<GP7NotesWithPosition> notes) {
		this.trackInfo = trackInfo;
		this.notes = notes;
	}
}