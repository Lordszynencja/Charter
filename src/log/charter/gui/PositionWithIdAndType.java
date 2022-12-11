package log.charter.gui;

import static log.charter.gui.ChartPanel.lanesBottom;
import static log.charter.gui.ChartPanel.lanesTop;

import log.charter.data.ChartData;
import log.charter.data.EditMode;
import log.charter.song.Beat;
import log.charter.song.Chord;
import log.charter.song.HandShape;
import log.charter.song.Level;
import log.charter.song.Note;
import log.charter.song.Position;
import log.charter.song.Vocal;
import log.charter.util.CollectionUtils.ArrayList2;

public class PositionWithIdAndType extends Position {
	public enum PositionType {
		BEAT(data -> data.songChart.beatsMap.beats.mapWithId(PositionWithIdAndType::new)), //
		GUITAR_NOTE(data -> {
			final Level currentLevel = data.getCurrentArrangementLevel();
			final ArrayList2<PositionWithIdAndType> list = currentLevel.notes.mapWithId(PositionWithIdAndType::new);
			list.addAll(currentLevel.chords.mapWithId(PositionWithIdAndType::new));
			list.sort(null);
			return list;
		}), //
		HAND_SHAPE(data -> data.getCurrentArrangementLevel().handShapes.mapWithId(PositionWithIdAndType::new)), //
		NONE(data -> new ArrayList2<>()), //
		VOCAL(data -> data.songChart.vocals.vocals.mapWithId(PositionWithIdAndType::new));

		public static interface PositionChooser {
			ArrayList2<PositionWithIdAndType> getAvailablePositionsForSelection(ChartData data);
		}

		public final PositionChooser positionChooser;

		private PositionType(final PositionChooser positionChooser) {
			this.positionChooser = positionChooser;
		}

		public static PositionType fromY(final int y, final ChartData data) {
			if (y < lanesTop) {
				return BEAT;
			}

			if (data.editMode == EditMode.VOCALS) {
				if (y >= lanesTop && y < lanesBottom) {
					return VOCAL;
				}

				return NONE;
			}
			if (data.editMode == EditMode.GUITAR) {
				if (y >= lanesTop && y < lanesBottom) {
					return GUITAR_NOTE;
				}

				if (y >= lanesBottom && y < ChartPanel.handShapesY) {
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

	public final Beat beat;
	public final Chord chord;
	public final HandShape handShape;
	public final Note note;
	public final Vocal vocal;

	private PositionWithIdAndType(final int position, final int endPosition, final Integer id, final PositionType type,
			final Beat beat, final Chord chord, final HandShape handShape, final Note note, final Vocal vocal) {
		super(position);
		this.endPosition = endPosition;
		this.id = id;
		this.type = type;

		this.beat = beat;
		this.chord = chord;
		this.handShape = handShape;
		this.note = note;
		this.vocal = vocal;
	}

	public PositionWithIdAndType(final int position, final PositionType type) {
		this(position, position, null, type, null, null, null, null, null);
	}

	public PositionWithIdAndType(final int id, final Beat beat) {
		this(beat.position, beat.position, id, PositionType.BEAT, beat, null, null, null, null);
	}

	public PositionWithIdAndType(final int id, final Chord chord) {
		this(chord.position, chord.position + chord.length(), id, PositionType.GUITAR_NOTE, null, chord, null, null,
				null);
	}

	public PositionWithIdAndType(final int id, final HandShape handShape) {
		this(handShape.position, handShape.position + handShape.length, id, PositionType.HAND_SHAPE, null, null,
				handShape, null, null);
	}

	public PositionWithIdAndType(final int id, final Note note) {
		this(note.position, note.position + note.sustain, id, PositionType.GUITAR_NOTE, null, null, null, note, null);
	}

	public PositionWithIdAndType(final int id, final Vocal vocal) {
		this(vocal.position, vocal.position + vocal.length, id, PositionType.VOCAL, null, null, null, null, vocal);
	}
}