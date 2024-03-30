package log.charter.services.mouseAndKeyboard;

import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.fractional.IConstantFractionalPosition;

public class PositionWithStringOrNoteId implements IConstantFractionalPosition {
	public static PositionWithStringOrNoteId fromNoteId(final int noteId, final ChordOrNote sound, final int string) {
		return new PositionWithStringOrNoteId(sound.position(), noteId, sound, string);
	}

	public static PositionWithStringOrNoteId fromPosition(final FractionalPosition position, final int string) {
		return new PositionWithStringOrNoteId(position, null, null, string);
	}

	public final FractionalPosition position;
	public final Integer noteId;
	public final ChordOrNote sound;
	public final int string;

	private PositionWithStringOrNoteId(final FractionalPosition position, final Integer noteId, final ChordOrNote sound,
			final int string) {
		this.position = position;
		this.noteId = noteId;
		this.sound = sound;
		this.string = string;
	}

	@Override
	public FractionalPosition position() {
		return position;
	}
}
