package log.charter.data.types;

import log.charter.data.song.Anchor;
import log.charter.data.song.Beat;
import log.charter.data.song.EventPoint;
import log.charter.data.song.HandShape;
import log.charter.data.song.ToneChange;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.position.Position;
import log.charter.data.song.vocals.Vocal;

public class PositionWithIdAndType extends Position {
	public static PositionWithIdAndType forNone() {
		return new PositionWithIdAndType(0, PositionType.NONE);
	}

	public final int endPosition;
	public final Integer id;
	public final PositionType type;
	public final boolean existingPosition;

	public final Anchor anchor;
	public final Beat beat;
	public final EventPoint eventPoint;
	public final ChordOrNote chordOrNote;
	public final HandShape handShape;
	public final ToneChange toneChange;
	public final Vocal vocal;

	public PositionWithIdAndType(final int position, final int endPosition, final Integer id, final PositionType type,
			final boolean existingPosition, final Anchor anchor, final Beat beat, final EventPoint eventPoint,
			final ChordOrNote chordOrNote, final HandShape handShape, final ToneChange toneChange, final Vocal vocal) {
		super(position);
		this.endPosition = endPosition;
		this.id = id;
		this.type = type;
		this.existingPosition = existingPosition;

		this.anchor = anchor;
		this.beat = beat;
		this.eventPoint = eventPoint;
		this.chordOrNote = chordOrNote;
		this.handShape = handShape;
		this.toneChange = toneChange;
		this.vocal = vocal;
	}

	public PositionWithIdAndType(final int position, final PositionType type) {
		this(position, position, null, type, false, null, null, null, null, null, null, null);
	}

	public PositionWithIdAndType(final int id, final Anchor anchor) {
		this(anchor.position(), anchor.position(), id, PositionType.ANCHOR, true, anchor, null, null, null, null, null,
				null);
	}

	public PositionWithIdAndType(final int id, final Beat beat) {
		this(beat.position(), beat.position(), id, PositionType.BEAT, true, null, beat, null, null, null, null, null);
	}

	public PositionWithIdAndType(final int id, final EventPoint eventPoint) {
		this(eventPoint.position(), eventPoint.position(), id, PositionType.EVENT_POINT, true, null, null, eventPoint,
				null, null, null, null);
	}

	public PositionWithIdAndType(final int id, final ChordOrNote chordOrNote) {
		this(chordOrNote.position(), chordOrNote.endPosition(), id, PositionType.GUITAR_NOTE, true, null, null, null,
				chordOrNote, null, null, null);
	}

	public PositionWithIdAndType(final int id, final HandShape handShape) {
		this(handShape.position(), handShape.endPosition(), id, PositionType.HAND_SHAPE, true, null, null, null, null,
				handShape, null, null);
	}

	public PositionWithIdAndType(final int id, final ToneChange toneChange) {
		this(toneChange.position(), toneChange.position(), id, PositionType.TONE_CHANGE, true, null, null, null, null,
				null, toneChange, null);
	}

	public PositionWithIdAndType(final int id, final Vocal vocal) {
		this(vocal.position(), vocal.endPosition(), id, PositionType.VOCAL, true, null, null, null, null, null, null,
				vocal);
	}

	@SuppressWarnings("unchecked")
	public <T> T get() {
		return switch (type) {
			case ANCHOR -> (T) anchor;
			case BEAT -> (T) beat;
			case EVENT_POINT -> (T) eventPoint;
			case GUITAR_NOTE -> (T) chordOrNote;
			case HAND_SHAPE -> (T) handShape;
			case NONE -> null;
			case TONE_CHANGE -> (T) toneChange;
			case VOCAL -> (T) vocal;
			default -> null;
		};
	}
}