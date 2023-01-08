package log.charter.data.types;

import log.charter.song.Anchor;
import log.charter.song.Beat;
import log.charter.song.HandShape;
import log.charter.song.ToneChange;
import log.charter.song.notes.ChordOrNote;
import log.charter.song.notes.Position;
import log.charter.song.vocals.Vocal;

public class PositionWithIdAndType extends Position {
	public static PositionWithIdAndType create(final int position, final PositionType type) {
		return new TemporaryValue(position, type).transform();
	}

	public static PositionWithIdAndType create(final int id, final Anchor anchor) {
		return new TemporaryValue(id, anchor).transform();
	}

	public static PositionWithIdAndType create(final int id, final Beat beat) {
		return new TemporaryValue(id, beat).transform();
	}

	public static PositionWithIdAndType create(final int id, final ChordOrNote chordOrNote) {
		return new TemporaryValue(id, chordOrNote).transform();
	}

	public static PositionWithIdAndType create(final int id, final HandShape handShape) {
		return new TemporaryValue(id, handShape).transform();
	}

	public static PositionWithIdAndType create(final int id, final ToneChange toneChange) {
		return new TemporaryValue(id, toneChange).transform();
	}

	public static PositionWithIdAndType create(final int id, final Vocal vocal) {
		return new TemporaryValue(id, vocal).transform();
	}

	public final int endPosition;
	public final Integer id;
	public final PositionType type;

	public final Anchor anchor;
	public final Beat beat;
	public final ChordOrNote chordOrNote;
	public final HandShape handShape;
	public final ToneChange toneChange;
	public final Vocal vocal;

	PositionWithIdAndType(final int position, final int endPosition, final Integer id, final PositionType type,
			final Anchor anchor, final Beat beat, final ChordOrNote chordOrNote, final HandShape handShape,
			final ToneChange toneChange, final Vocal vocal) {
		super(position);
		this.endPosition = endPosition;
		this.id = id;
		this.type = type;

		this.anchor = anchor;
		this.beat = beat;
		this.chordOrNote = chordOrNote;
		this.handShape = handShape;
		this.toneChange = toneChange;
		this.vocal = vocal;
	}
}