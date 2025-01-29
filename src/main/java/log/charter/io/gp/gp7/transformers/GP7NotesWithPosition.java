package log.charter.io.gp.gp7.transformers;

import java.util.List;

import log.charter.data.song.position.FractionalPosition;
import log.charter.io.gp.gp7.data.GP7Beat;
import log.charter.io.gp.gp7.data.GP7Note;

class GP7NotesWithPosition implements Comparable<GP7NotesWithPosition> {
	public static class GP7BeatWithNoteAndEndPosition {
		public final GP7Beat beat;
		public final GP7Note note;
		public final FractionalPosition endPosition;

		public GP7BeatWithNoteAndEndPosition(final GP7Beat beat, final GP7Note note,
				final FractionalPosition endPosition) {
			this.beat = beat;
			this.note = note;
			this.endPosition = endPosition;
		}
	}

	public final FractionalPosition position;
	public final List<GP7BeatWithNoteAndEndPosition> notes;

	public GP7NotesWithPosition(final FractionalPosition position, final List<GP7BeatWithNoteAndEndPosition> notes) {
		this.position = position;
		this.notes = notes;
	}

	@Override
	public int compareTo(final GP7NotesWithPosition o) {
		return position.compareTo(o.position);
	}
}