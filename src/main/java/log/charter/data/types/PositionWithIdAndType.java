package log.charter.data.types;

import log.charter.data.song.Anchor;
import log.charter.data.song.Beat;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.EventPoint;
import log.charter.data.song.HandShape;
import log.charter.data.song.ToneChange;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.position.ConstantPosition;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.IConstantFractionalPosition;
import log.charter.data.song.position.IConstantFractionalPositionWithEnd;
import log.charter.data.song.position.IConstantPosition;
import log.charter.data.song.vocals.Vocal;

public class PositionWithIdAndType extends ConstantPosition implements IConstantFractionalPositionWithEnd {
	public static PositionWithIdAndType none() {
		return new Builder().build();
	}

	public static PositionWithIdAndType of(final ImmutableBeatsMap beats, final int position, final PositionType type) {
		return new Builder(beats, position).type(type).build();
	}

	public static PositionWithIdAndType of(final ImmutableBeatsMap beats, final int id, final Anchor anchor) {
		return new Builder(beats, anchor.fractionalPosition()).id(id).anchor(anchor).build();
	}

	public static PositionWithIdAndType of(final ImmutableBeatsMap beats, final int id, final Beat beat) {
		return new Builder(beats, beat.position()).id(id).beat(beat).build();
	}

	public static PositionWithIdAndType of(final ImmutableBeatsMap beats, final int id, final EventPoint eventPoint) {
		return new Builder(beats, eventPoint.fractionalPosition()).id(id).eventPoint(eventPoint).build();
	}

	public static PositionWithIdAndType of(final ImmutableBeatsMap beats, final int id, final ChordOrNote sound) {
		return new Builder(beats, sound.position(), sound.endPosition().position()).id(id).sound(sound).build();
	}

	public static PositionWithIdAndType of(final ImmutableBeatsMap beats, final int id, final HandShape handShape) {
		return new Builder(beats, handShape.position(), handShape.endPosition().position()).id(id).handShape(handShape)
				.build();
	}

	public static PositionWithIdAndType of(final ImmutableBeatsMap beats, final int id, final ToneChange toneChange) {
		return new Builder(beats, toneChange.fractionalPosition()).id(id).toneChange(toneChange).build();
	}

	public static PositionWithIdAndType of(final ImmutableBeatsMap beats, final int id, final Vocal vocal) {
		return new Builder(beats, vocal.fractionalPosition(), vocal.endPosition()).id(id).vocal(vocal).build();
	}

	private static class Builder {
		public int position = 0;
		public FractionalPosition fractionalPosition = new FractionalPosition(0);
		public int endPosition = 0;
		public FractionalPosition fractionalEndPosition = new FractionalPosition(0);
		public Integer id = null;
		public PositionType type = PositionType.NONE;
		public boolean existingPosition = false;

		public Anchor anchor = null;
		public Beat beat = null;
		public EventPoint eventPoint = null;
		public ChordOrNote sound = null;
		public HandShape handShape = null;
		public ToneChange toneChange = null;
		public Vocal vocal = null;

		public Builder() {
		}

		public Builder(final ImmutableBeatsMap beats, final int position, final int endPosition) {
			this.position = position;
			fractionalPosition = FractionalPosition.fromTime(beats, position);
			this.endPosition = endPosition;
			fractionalEndPosition = FractionalPosition.fromTime(beats, endPosition);
		}

		public Builder(final ImmutableBeatsMap beats, final int position) {
			this.position = position;
			fractionalPosition = FractionalPosition.fromTime(beats, position);
			endPosition = position;
			fractionalEndPosition = fractionalPosition;
		}

		public Builder(final ImmutableBeatsMap beats, final FractionalPosition fractionalPosition) {
			position = fractionalPosition.getPosition(beats);
			this.fractionalPosition = fractionalPosition;
			endPosition = position;
			fractionalEndPosition = fractionalPosition;
		}

		public Builder(final ImmutableBeatsMap beats, final FractionalPosition fractionalPosition,
				final FractionalPosition fractionalEndPosition) {
			position = fractionalPosition.getPosition(beats);
			this.fractionalPosition = fractionalPosition;
			endPosition = fractionalEndPosition.getPosition(beats);
			this.fractionalEndPosition = fractionalEndPosition;
		}

		public Builder type(final PositionType type) {
			this.type = type;

			return this;
		}

		public Builder id(final int id) {
			this.id = id;
			existingPosition = true;

			return this;
		}

		public Builder anchor(final Anchor anchor) {
			type = PositionType.ANCHOR;
			this.anchor = anchor;

			return this;
		}

		public Builder beat(final Beat beat) {
			type = PositionType.BEAT;
			this.beat = beat;

			return this;
		}

		public Builder eventPoint(final EventPoint eventPoint) {
			type = PositionType.EVENT_POINT;
			this.eventPoint = eventPoint;

			return this;
		}

		public Builder sound(final ChordOrNote sound) {
			type = PositionType.GUITAR_NOTE;
			this.sound = sound;

			return this;
		}

		public Builder handShape(final HandShape handShape) {
			type = PositionType.HAND_SHAPE;
			this.handShape = handShape;

			return this;
		}

		public Builder toneChange(final ToneChange toneChange) {
			type = PositionType.TONE_CHANGE;
			this.toneChange = toneChange;

			return this;
		}

		public Builder vocal(final Vocal vocal) {
			type = PositionType.VOCAL;
			this.vocal = vocal;

			return this;
		}

		public PositionWithIdAndType build() {
			return new PositionWithIdAndType(position, fractionalPosition, endPosition, fractionalEndPosition, id, type,
					existingPosition, anchor, beat, eventPoint, sound, handShape, toneChange, vocal);
		}
	}

	public final FractionalPosition fractionalPosition;
	public final int endPosition;
	public final FractionalPosition fractionalEndPosition;
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

	public PositionWithIdAndType(final int position, final FractionalPosition fractionalPosition, final int endPosition,
			final FractionalPosition fractionalEndPosition, final Integer id, final PositionType type,
			final boolean existingPosition, final Anchor anchor, final Beat beat, final EventPoint eventPoint,
			final ChordOrNote chordOrNote, final HandShape handShape, final ToneChange toneChange, final Vocal vocal) {
		super(position);
		this.fractionalPosition = fractionalPosition;
		this.endPosition = endPosition;
		this.fractionalEndPosition = fractionalEndPosition;
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

	@Override
	public IConstantPosition toPosition(final ImmutableBeatsMap beats) {
		return this;
	}

	@Override
	public IConstantFractionalPosition toFraction(final ImmutableBeatsMap beats) {
		return this;
	}

	@Override
	public IConstantPosition asConstantPosition() {
		return this;
	}

	@Override
	public IConstantFractionalPosition asConstantFraction() {
		return this;
	}

	@Override
	public FractionalPosition fractionalPosition() {
		return fractionalPosition;
	}

	@Override
	public FractionalPosition endPosition() {
		return fractionalEndPosition;
	}

}