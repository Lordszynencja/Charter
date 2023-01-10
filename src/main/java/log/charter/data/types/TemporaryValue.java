package log.charter.data.types;

import log.charter.song.Anchor;
import log.charter.song.Beat;
import log.charter.song.HandShape;
import log.charter.song.ToneChange;
import log.charter.song.notes.ChordOrNote;
import log.charter.song.vocals.Vocal;

class TemporaryValue {
	private final int position;
	private final int endPosition;
	private Integer id;
	private final PositionType type;
	private boolean existingPosition;

	private Anchor anchor;
	private Beat beat;
	private ChordOrNote chordOrNote;
	private HandShape handShape;
	private ToneChange toneChange;
	private Vocal vocal;

	public TemporaryValue(final int position, final PositionType type) {
		this.position = position;
		endPosition = position;
		this.type = type;
	}

	private TemporaryValue(final int position, final int endPosition, final int id, final PositionType type) {
		this.position = position;
		this.endPosition = endPosition;
		this.id = id;
		this.type = type;
	}

	public TemporaryValue(final int id, final Anchor anchor) {
		this(anchor.position(), anchor.position(), id, PositionType.ANCHOR);
		this.anchor = anchor;
		existingPosition = true;
	}

	public TemporaryValue(final int id, final Beat beat) {
		this(beat.position(), beat.position(), id, PositionType.BEAT);
		this.beat = beat;
		existingPosition = true;
	}

	public TemporaryValue(final int id, final ChordOrNote chordOrNote) {
		this(chordOrNote.position(), chordOrNote.endPosition(), id, PositionType.GUITAR_NOTE);
		this.chordOrNote = chordOrNote;
		existingPosition = true;
	}

	public TemporaryValue(final int id, final HandShape handShape) {
		this(handShape.position(), handShape.endPosition(), id, PositionType.HAND_SHAPE);
		this.handShape = handShape;
		existingPosition = true;
	}

	public TemporaryValue(final int id, final ToneChange toneChange) {
		this(toneChange.position(), toneChange.position(), id, PositionType.TONE_CHANGE);
		this.toneChange = toneChange;
		existingPosition = true;
	}

	public TemporaryValue(final int id, final Vocal vocal) {
		this(vocal.position(), vocal.endPosition(), id, PositionType.VOCAL);
		this.vocal = vocal;
		existingPosition = true;
	}

	public PositionWithIdAndType transform() {
		return new PositionWithIdAndType(position, endPosition, id, type, existingPosition, anchor, beat, chordOrNote,
				handShape, toneChange, vocal);
	}
}