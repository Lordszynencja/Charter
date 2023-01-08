package log.charter.data.types;

import log.charter.song.Anchor;
import log.charter.song.Beat;
import log.charter.song.HandShape;
import log.charter.song.ToneChange;
import log.charter.song.notes.ChordOrNote;
import log.charter.song.vocals.Vocal;

class TemporaryValue {
	public int position;
	public int endPosition;
	public Integer id;
	public PositionType type;

	public Anchor anchor;
	public Beat beat;
	public ChordOrNote chordOrNote;
	public HandShape handShape;
	public ToneChange toneChange;
	public Vocal vocal;

	public TemporaryValue(final int position, final PositionType type) {
		this.position = position;
		endPosition = position;
		this.type = type;
	}

	public TemporaryValue(final int position, final int endPosition, final int id, final PositionType type) {
		this.position = position;
		this.endPosition = endPosition;
		this.id = id;
		this.type = type;
	}

	public TemporaryValue(final int id, final Anchor anchor) {
		this(anchor.position(), anchor.position(), id, PositionType.ANCHOR);
		this.anchor = anchor;
	}

	public TemporaryValue(final int id, final Beat beat) {
		this(beat.position(), beat.position(), id, PositionType.BEAT);
		this.beat = beat;
	}

	public TemporaryValue(final int id, final ChordOrNote chordOrNote) {
		this(chordOrNote.position(), chordOrNote.endPosition(), id, PositionType.GUITAR_NOTE);
		this.chordOrNote = chordOrNote;
	}

	public TemporaryValue(final int id, final HandShape handShape) {
		this(handShape.position(), handShape.endPosition(), id, PositionType.HAND_SHAPE);
		this.handShape = handShape;
	}

	public TemporaryValue(final int id, final ToneChange toneChange) {
		this(toneChange.position(), toneChange.position(), id, PositionType.TONE_CHANGE);
		this.toneChange = toneChange;
	}

	public TemporaryValue(final int id, final Vocal vocal) {
		this(vocal.position(), vocal.endPosition(), id, PositionType.VOCAL);
		this.vocal = vocal;
	}

	public PositionWithIdAndType transform() {
		return new PositionWithIdAndType(position, endPosition, id, type, anchor, beat, chordOrNote, handShape,
				toneChange, vocal);
	}
}