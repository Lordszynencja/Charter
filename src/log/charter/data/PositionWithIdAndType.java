package log.charter.data;

import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.anchorY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.handShapesY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesBottom;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesTop;

import java.util.function.BiFunction;

import log.charter.data.managers.selection.ChordOrNote;
import log.charter.song.Anchor;
import log.charter.song.Beat;
import log.charter.song.HandShape;
import log.charter.song.Level;
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
		public ChordOrNote chordOrNote;
		public HandShape handShape;
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

		public TemporaryValue(final int id, final ChordOrNote chordOrNote) {
			this(chordOrNote.position, chordOrNote.position + chordOrNote.length(), id, PositionType.GUITAR_NOTE);
			this.chordOrNote = chordOrNote;
		}

		public TemporaryValue(final int id, final HandShape handShape) {
			this(handShape.position, handShape.position + handShape.length, id, PositionType.HAND_SHAPE);
			this.handShape = handShape;
		}

		public TemporaryValue(final int id, final Vocal vocal) {
			this(vocal.position, vocal.position + vocal.length, id, PositionType.VOCAL);
			this.vocal = vocal;
		}

		public PositionWithIdAndType transform() {
			return new PositionWithIdAndType(position, endPosition, id, type, anchor, beat, chordOrNote, handShape,
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

	public static PositionWithIdAndType create(final int id, final ChordOrNote chordOrNote) {
		return new TemporaryValue(id, chordOrNote).transform();
	}

	public static PositionWithIdAndType create(final int id, final HandShape handShape) {
		return new TemporaryValue(id, handShape).transform();
	}

	public static PositionWithIdAndType create(final int id, final Vocal vocal) {
		return new TemporaryValue(id, vocal).transform();
	}

	public enum PositionType {
		ANCHOR(data -> data.getCurrentArrangementLevel().anchors, PositionWithIdAndType::create), //
		BEAT(data -> data.songChart.beatsMap.beats, PositionWithIdAndType::create), //
		GUITAR_NOTE(data -> {
			final Level currentLevel = data.getCurrentArrangementLevel();
			final ArrayList2<ChordOrNote> list = currentLevel.notes.map(ChordOrNote::new);
			list.addAll(currentLevel.chords.map(ChordOrNote::new));
			list.sort(null);

			return list;
		}, PositionWithIdAndType::create), //
		HAND_SHAPE(data -> data.getCurrentArrangementLevel().handShapes, PositionWithIdAndType::create), //
		NONE(data -> new ArrayList2<>(), (id, nothing) -> null), //
		VOCAL(data -> data.songChart.vocals.vocals, PositionWithIdAndType::create);

		public static interface PositionChooser<T extends Position> {
			ArrayList2<T> getAvailablePositionsForSelection(ChartData data);
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

		private final PositionChooser<?> positionChooser;
		public final PositionChooser<PositionWithIdAndType> positionWithIdAndTypeChooser;

		private <T extends Position> PositionType(final PositionChooser<T> positionChooser,
				final BiFunction<Integer, T, PositionWithIdAndType> mapper) {
			this.positionChooser = positionChooser;
			positionWithIdAndTypeChooser = data -> positionChooser.getAvailablePositionsForSelection(data)
					.mapWithId(mapper);
		}

		@SuppressWarnings("unchecked")
		public <T> ArrayList2<T> getPositions(final ChartData data) {
			return (ArrayList2<T>) positionChooser.getAvailablePositionsForSelection(data);
		}
	}

	public final int endPosition;
	public final Integer id;
	public final PositionType type;

	public final Anchor anchor;
	public final Beat beat;
	public final ChordOrNote chordOrNote;
	public final HandShape handShape;
	public final Vocal vocal;

	private PositionWithIdAndType(final int position, final int endPosition, final Integer id, final PositionType type,
			final Anchor anchor, final Beat beat, final ChordOrNote chordOrNote, final HandShape handShape,
			final Vocal vocal) {
		super(position);
		this.endPosition = endPosition;
		this.id = id;
		this.type = type;

		this.anchor = anchor;
		this.beat = beat;
		this.chordOrNote = chordOrNote;
		this.handShape = handShape;
		this.vocal = vocal;
	}
}