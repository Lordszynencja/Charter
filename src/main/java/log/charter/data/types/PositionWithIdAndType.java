package log.charter.data.types;

import log.charter.data.song.Anchor;
import log.charter.data.song.Beat;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.EventPoint;
import log.charter.data.song.HandShape;
import log.charter.data.song.ToneChange;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.fractional.IConstantFractionalPosition;
import log.charter.data.song.position.fractional.IConstantFractionalPositionWithEnd;
import log.charter.data.song.position.fractional.IConstantFractionalPositionWithEnd.ConstantFractionalPositionWithEnd;
import log.charter.data.song.position.time.ConstantPositionWithLength;
import log.charter.data.song.position.time.IConstantPosition;
import log.charter.data.song.position.time.IConstantPositionWithLength;
import log.charter.data.song.position.virtual.IVirtualConstantPosition;
import log.charter.data.song.position.virtual.IVirtualConstantPositionWithEnd;
import log.charter.data.song.vocals.Vocal;

public class PositionWithIdAndType implements IVirtualConstantPositionWithEnd {
	public static PositionWithIdAndType none() {
		return new Builder().build();
	}

	public static PositionWithIdAndType of(final ImmutableBeatsMap beats, final IVirtualConstantPosition position,
			final PositionType type) {
		return new Builder(beats, position).type(type).build();
	}

	public static PositionWithIdAndType of(final ImmutableBeatsMap beats, final int position, final PositionType type) {
		return new Builder(beats, position).type(type).build();
	}

	public static PositionWithIdAndType of(final ImmutableBeatsMap beats, final int id, final Anchor anchor) {
		return new Builder(beats, anchor.position()).id(id).anchor(anchor).build();
	}

	public static PositionWithIdAndType of(final ImmutableBeatsMap beats, final int id, final Beat beat) {
		return new Builder(beats, beat.position()).id(id).beat(beat).build();
	}

	public static PositionWithIdAndType of(final ImmutableBeatsMap beats, final int id, final EventPoint eventPoint) {
		return new Builder(beats, eventPoint.position()).id(id).eventPoint(eventPoint).build();
	}

	public static PositionWithIdAndType of(final ImmutableBeatsMap beats, final int id, final ChordOrNote sound) {
		return new Builder(beats, sound.position(), sound.endPosition()).id(id).sound(sound).build();
	}

	public static PositionWithIdAndType of(final ImmutableBeatsMap beats, final int id, final HandShape handShape) {
		return new Builder(beats, handShape.position(), handShape.endPosition()).id(id).handShape(handShape).build();
	}

	public static PositionWithIdAndType of(final ImmutableBeatsMap beats, final int id, final ToneChange toneChange) {
		return new Builder(beats, toneChange.position()).id(id).toneChange(toneChange).build();
	}

	public static PositionWithIdAndType of(final ImmutableBeatsMap beats, final int id, final Vocal vocal) {
		return new Builder(beats, vocal.position(), vocal.endPosition()).id(id).vocal(vocal).build();
	}

	private static class Builder {
		public int position = 0;
		public FractionalPosition fractionalPosition = new FractionalPosition();
		public int endPosition = 0;
		public FractionalPosition fractionalEndPosition = new FractionalPosition();
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

		public Builder(final ImmutableBeatsMap beats, final IVirtualConstantPosition position) {
			this.position = position.toPosition(beats).position();
			fractionalPosition = position.toFraction(beats).position();
			endPosition = this.position;
			fractionalEndPosition = fractionalPosition;
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

	private final ConstantPositionWithLength position;
	private final ConstantFractionalPositionWithEnd fractionalPosition;
	private final IVirtualConstantPosition endPosition;

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

	private PositionWithIdAndType(final int position, final FractionalPosition fractionalPosition,
			final int endPosition, final FractionalPosition fractionalEndPosition, final Integer id,
			final PositionType type, final boolean existingPosition, final Anchor anchor, final Beat beat,
			final EventPoint eventPoint, final ChordOrNote chordOrNote, final HandShape handShape,
			final ToneChange toneChange, final Vocal vocal) {
		this.position = new ConstantPositionWithLength(position, endPosition - position);
		this.fractionalPosition = new ConstantFractionalPositionWithEnd(fractionalPosition, fractionalEndPosition);
		this.endPosition = new IVirtualConstantPosition() {

			@Override
			public IConstantPosition asConstantPosition() {
				return PositionWithIdAndType.this.position.endPosition();
			}

			@Override
			public IConstantFractionalPosition asConstantFraction() {
				return PositionWithIdAndType.this.fractionalPosition.endPosition();
			}

			@Override
			public IConstantPosition toPosition(final ImmutableBeatsMap beats) {
				return PositionWithIdAndType.this.position.endPosition();
			}

			@Override
			public IConstantFractionalPosition toFraction(final ImmutableBeatsMap beats) {
				return PositionWithIdAndType.this.fractionalPosition.endPosition();
			}

		};

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
	public IConstantPositionWithLength toPosition(final ImmutableBeatsMap beats) {
		return position;
	}

	@Override
	public IConstantFractionalPositionWithEnd toFraction(final ImmutableBeatsMap beats) {
		return fractionalPosition;
	}

	@Override
	public IConstantPosition asConstantPosition() {
		return position;
	}

	@Override
	public IConstantFractionalPosition asConstantFraction() {
		return fractionalPosition;
	}

	@Override
	public IConstantPositionWithLength asConstantPositionWithLength() {
		return position;
	}

	@Override
	public IConstantFractionalPositionWithEnd asConstantFractionalPositionWithEnd() {
		return fractionalPosition;
	}

	@Override
	public IVirtualConstantPosition endPosition() {
		return endPosition;
	}

}