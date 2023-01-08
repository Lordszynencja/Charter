package log.charter.data.managers;

import log.charter.song.notes.Position;

public class PositionWithStringOrNoteId extends Position {
	public static PositionWithStringOrNoteId fromNoteId(final int noteId, final int position, final int string) {
		return new PositionWithStringOrNoteId(position, noteId, string);
	}

	public static PositionWithStringOrNoteId fromPosition(final int position, final int string) {
		return new PositionWithStringOrNoteId(position, null, string);
	}

	public final Integer noteId;
	public final int string;

	private PositionWithStringOrNoteId(final int position, final Integer noteId, final int string) {
		super(position);
		this.noteId = noteId;
		this.string = string;
	}
}
