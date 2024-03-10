package log.charter.services.mouseAndKeyboard;

import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.position.Position;

public class PositionWithStringOrNoteId extends Position {
	public static PositionWithStringOrNoteId fromNoteId(final int noteId, final ChordOrNote sound, final int string) {
		return new PositionWithStringOrNoteId(sound.position(), noteId, sound, string);
	}

	public static PositionWithStringOrNoteId fromPosition(final int position, final int string) {
		return new PositionWithStringOrNoteId(position, null, null, string);
	}

	public final Integer noteId;
	public final ChordOrNote sound;
	public final int string;

	private PositionWithStringOrNoteId(final int position, final Integer noteId, final ChordOrNote sound,
			final int string) {
		super(position);
		this.noteId = noteId;
		this.sound = sound;
		this.string = string;
	}
}
