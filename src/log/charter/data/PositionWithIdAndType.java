package log.charter.data;

import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.anchorY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.handShapesY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesBottom;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesTop;

import log.charter.song.Anchor;
import log.charter.song.Beat;
import log.charter.song.Chord;
import log.charter.song.HandShape;
import log.charter.song.Level;
import log.charter.song.Note;
import log.charter.song.Position;
import log.charter.song.Vocal;
import log.charter.util.CollectionUtils.ArrayList2;

public class PositionWithIdAndType extends Position {
	private static class TemporaryValue {
		public int position;
		public int endPosition;
		public Integer id;
		public PositionType type;

		public Anchor anchor;
		public Beat beat;
		public Chord chord;
		public HandShape handShape;
		public Note note;
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
			this(anchor.position, anchor.position, id, PositionType.ANCHOR);
			this.anchor = anchor;
		}

		public TemporaryValue(final int id, final Beat beat) {
			this(beat.position, beat.position, id, PositionType.BEAT);
			this.beat = beat;
		}

		public TemporaryValue(final int id, final Chord chord) {
			this(chord.position, chord.position + chord.length(), id, PositionType.GUITAR_NOTE);
			this.chord = chord;
		}

		public TemporaryValue(final int id, final HandShape handShape) {
			this(handShape.position, handShape.position + handShape.length, id, PositionType.HAND_SHAPE);
			this.handShape = handShape;
		}

		public TemporaryValue(final int id, final Note note) {
			this(note.position, note.position + note.sustain, id, PositionType.GUITAR_NOTE);
			this.note = note;
		}

		public TemporaryValue(final int id, final Vocal vocal) {
			this(vocal.position, vocal.position + vocal.length, id, PositionType.VOCAL);
			this.vocal = vocal;
		}

		public PositionWithIdAndType transform() {
			return new PositionWithIdAndType(position, endPosition, id, type, anchor, beat, chord, handShape, note,
					vocal);
		}
	}

	public static PositionWithIdAndType create(final int position, final PositionType type) {
		return new TemporaryValue(position, type).transform();
	}

	public static PositionWithIdAndType create(final int id, final Anchor anchor) {
		return new TemporaryValue(id, anchor).transform();
	}

	public static PositionWithIdAndType create(final int id, final Beat beat) {
		return new TemporaryValue(id, beat).transform();
	}

	public static PositionWithIdAndType create(final int id, final Chord chord) {
		return new TemporaryValue(id, chord).transform();
	}

	public static PositionWithIdAndType create(final int id, final HandShape handShape) {
		return new TemporaryValue(id, handShape).transform();
	}

	public static PositionWithIdAndType create(final int id, final Note note) {
		return new TemporaryValue(id, note).transform();
	}

	public static PositionWithIdAndType create(final int id, final Vocal vocal) {
		return new TemporaryValue(id, vocal).transform();
	}

	public enum PositionType {
		ANCHOR(data -> data.getCurrentArrangementLevel().anchors.mapWithId(PositionWithIdAndType::create)), //
		BEAT(data -> data.songChart.beatsMap.beats.mapWithId(PositionWithIdAndType::create)), //
		GUITAR_NOTE(data -> {
			final Level currentLevel = data.getCurrentArrangementLevel();
			final ArrayList2<PositionWithIdAndType> list = currentLevel.notes.mapWithId(PositionWithIdAndType::create);
			list.addAll(currentLevel.chords.mapWithId(PositionWithIdAndType::create));
			list.sort(null);
			return list;
		}), //
		HAND_SHAPE(data -> data.getCurrentArrangementLevel().handShapes.mapWithId(PositionWithIdAndType::create)), //
		NONE(data -> new ArrayList2<>()), //
		VOCAL(data -> data.songChart.vocals.vocals.mapWithId(PositionWithIdAndType::create));

		public static interface PositionChooser {
			ArrayList2<PositionWithIdAndType> getAvailablePositionsForSelection(ChartData data);
		}

		public final PositionChooser positionChooser;

		private PositionType(final PositionChooser positionChooser) {
			this.positionChooser = positionChooser;
		}

		public static PositionType fromY(final int y, final EditMode mode) {
			if (y < anchorY) {
				return BEAT;
			}

			if (mode == EditMode.VOCALS) {
				if (y >= lanesTop && y < lanesBottom) {
					return VOCAL;
				}

				return NONE;
			}

			if (mode == EditMode.GUITAR) {
				if (y < lanesTop) {
					return ANCHOR;
				}

				if (y >= lanesTop && y < lanesBottom) {
					return GUITAR_NOTE;
				}

				if (y >= lanesBottom && y < handShapesY) {
					return HAND_SHAPE;
				}

				return NONE;
			}

			return NONE;
		}
	}

	public final int endPosition;
	public final Integer id;
	public final PositionType type;

	public final Anchor anchor;
	public final Beat beat;
	public final Chord chord;
	public final HandShape handShape;
	public final Note note;
	public final Vocal vocal;

	private PositionWithIdAndType(final int position, final int endPosition, final Integer id, final PositionType type,
			final Anchor anchor, final Beat beat, final Chord chord, final HandShape handShape, final Note note,
			final Vocal vocal) {
		super(position);
		this.endPosition = endPosition;
		this.id = id;
		this.type = type;

		this.anchor = anchor;
		this.beat = beat;
		this.chord = chord;
		this.handShape = handShape;
		this.note = note;
		this.vocal = vocal;
	}
}