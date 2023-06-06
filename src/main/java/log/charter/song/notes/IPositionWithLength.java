package log.charter.song.notes;

import static java.lang.Math.max;
import static log.charter.data.config.Config.minNoteDistance;

import java.util.List;

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

	public static <T extends IPositionWithLength> int findFirstIdAfterEqual(final ArrayList2<T> list,
			final int position) {
		if (list.isEmpty()) {
			return -1;
		}
		if (position > list.getLast().endPosition()) {
			return -1;
		}

		int minId = 0;
		int maxId = list.size() - 1;
		while (maxId - minId > 1) {
			final int id = (minId + maxId) / 2;
			if (list.get(id).endPosition() < position) {
				minId = id + 1;
			} else {
				maxId = id;
			}
		}

		return list.get(minId).endPosition() < position ? maxId : minId;
	}

	public static <T extends IPositionWithLength> int findLastIdBefore(final List<T> list, final int position) {
		if (list.isEmpty()) {
			return -1;
		}

		if (position <= list.get(0).position()) {
			return -1;
		}

		int minId = 0;
		int maxId = list.size() - 1;
		while (maxId - minId > 1) {
			final int id = (minId + maxId) / 2;
			if (list.get(id).endPosition() >= position) {
				maxId = id - 1;
			} else {
				minId = id;
			}
		}

		return list.get(maxId).endPosition() >= position ? minId : maxId;
	}

	int length();

	void length(int newLength);

	default int endPosition() {
		return position() + length();
	}
}
