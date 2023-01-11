package log.charter.song.notes;

import static java.lang.Math.max;
import static log.charter.data.config.Config.minNoteDistance;

import log.charter.data.managers.selection.Selection;
import log.charter.song.BeatsMap;
import log.charter.util.CollectionUtils.ArrayList2;

public interface IPositionWithLength extends IPosition {
	public static <PwL extends IPositionWithLength, P extends IPosition> void changePositionsWithLengthsLength(
			final BeatsMap beatsMap, final ArrayList2<Selection<PwL>> toChange, final ArrayList2<P> allPositions,
			final int change) {
		for (final Selection<PwL> selected : toChange) {
			final IPositionWithLength positionWithLength = selected.selectable;
			int endPosition = positionWithLength.endPosition();
			endPosition = change > 0 ? beatsMap.getPositionWithAddedGrid(endPosition, change)
					: beatsMap.getPositionWithRemovedGrid(endPosition, -change);

			if (allPositions.size() > selected.id + 1) {
				final IPosition next = allPositions.get(selected.id + 1);
				if (next.position() - endPosition < minNoteDistance) {
					endPosition = next.position() - minNoteDistance;
				}
			}

			final int length = max(0, endPosition - positionWithLength.position());
			positionWithLength.length(length);
		}
	}

	public static void changeNotesLength(final BeatsMap beatsMap, final ArrayList2<Selection<ChordOrNote>> toChange,
			final ArrayList2<ChordOrNote> allPositions, final int change, final boolean cutBeforeNext) {
		for (final Selection<ChordOrNote> selected : toChange) {
			final GuitarSound sound = selected.selectable.asGuitarSound();
			int endPosition = sound.endPosition();
			endPosition = change > 0 ? beatsMap.getPositionWithAddedGrid(endPosition, change)
					: beatsMap.getPositionWithRemovedGrid(endPosition, -change);

			if ((sound.linkNext || cutBeforeNext) && allPositions.size() > selected.id + 1) {
				final IPosition next = allPositions.get(selected.id + 1);
				if (sound.linkNext) {
					endPosition = next.position();
				} else {
					if (next.position() - endPosition < minNoteDistance) {
						endPosition = next.position() - minNoteDistance;
					}
				}
			}

			final int length = max(0, endPosition - sound.position());
			sound.length(length);
		}
	}

	// TODO
//	public static <PwL extends PositionWithLength, P extends Position> void snapToGrid(
//			final BeatsMap beatsMap, final ArrayList2<Selection<PwL>> toChange, final ArrayList2<P> allPositions) {
//		for (final Selection<PwL> selected : toChange) {
//			final PositionWithLength positionWithLength = selected.selectable;
//			int position = positionWithLength.position();
//			position =beatsMap.getPositionFromGridClosestTo(position);
//			if (selected.id>0 && allPositions.get(selected.id-1).position == )
//			int endPosition = positionWithLength.endPosition();
//			endPosition = change > 0 ? beatsMap.getPositionWithAddedGrid(endPosition, change)
//					: beatsMap.getPositionWithRemovedGrid(endPosition, -change);
//
//			if (allPositions.size() > selected.id + 1) {
//				final Position next = allPositions.get(selected.id + 1);
//				if (next.position - endPosition < minNoteDistance) {
//					endPosition = next.position - minNoteDistance;
//				}
//			}
//
//			final int length = max(0, endPosition - positionWithLength.position());
//			positionWithLength.length(length);
//		}
//	}

	int length();

	void length(int newLength);

	default int endPosition() {
		return position() + length();
	}
}
