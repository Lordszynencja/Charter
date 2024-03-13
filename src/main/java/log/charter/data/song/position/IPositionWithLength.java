package log.charter.data.song.position;

import static java.lang.Math.max;
import static log.charter.data.config.Config.minNoteDistance;
import static log.charter.services.data.fixers.ArrangementFixer.fixNoteLength;

import java.util.List;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.notes.CommonNote;
import log.charter.services.data.selection.Selection;

public interface IPositionWithLength extends IPosition, IConstantPositionWithLength {
	public static class EndPosition implements IPosition, Comparable<IConstantPosition> {
		private final IPositionWithLength position;

		public EndPosition(final IPositionWithLength position) {
			this.position = position;
		}

		@Override
		public int position() {
			return position.endPosition();
		}

		@Override
		public void position(final int newPosition) {
			position.endPosition(newPosition);
		}

		@Override
		public int compareTo(final IConstantPosition o) {
			return Integer.compare(position.endPosition(), o.position());
		}
	}

	public static <PwL extends IPositionWithLength, P extends IPosition> void changePositionsWithLengthsLength(
			final ImmutableBeatsMap beats, final List<Selection<PwL>> toChange, final List<P> allPositions,
			final int change) {
		for (final Selection<PwL> selected : toChange) {
			final IPositionWithLength positionWithLength = selected.selectable;
			int endPosition = positionWithLength.endPosition();
			endPosition = change > 0 ? beats.getPositionWithAddedGrid(endPosition, change)
					: beats.getPositionWithRemovedGrid(endPosition, -change);

			if (selected.id + 1 < allPositions.size()) {
				final IPosition next = allPositions.get(selected.id + 1);
				if (next.position() - endPosition < minNoteDistance) {
					endPosition = next.position() - minNoteDistance;
				}
			}

			final int length = max(0, endPosition - positionWithLength.position());
			positionWithLength.length(length);
		}
	}

	private static void changeNoteLength(final ImmutableBeatsMap beats, final List<ChordOrNote> allPositions,
			final CommonNote note, final int id, final int change) {
		if (note.linkNext()) {
			fixNoteLength(note, id, allPositions);
			return;
		}

		final int newEndPosition = change > 0 ? beats.getPositionWithAddedGrid(note.endPosition(), change)
				: beats.getPositionWithRemovedGrid(note.endPosition(), -change);
		note.endPosition(newEndPosition);

		fixNoteLength(note, id, allPositions);
	}

	public static void changeSoundsLength(final ImmutableBeatsMap beats, final List<Selection<ChordOrNote>> toChange,
			final List<ChordOrNote> allPositions, final int change, final boolean cutBeforeNext,
			final List<Integer> selectedStrings) {
		for (final Selection<ChordOrNote> selected : toChange) {
			selected.selectable.notes().forEach(note -> {
				if (!selectedStrings.contains(note.string())) {
					return;
				}

				changeNoteLength(beats, allPositions, note, selected.id, change);
			});
		}
	}

	void length(int newLength);

	default void endPosition(final int newEndPosition) {
		length(newEndPosition - position());
	}

	default EndPosition asEndPosition() {
		return new EndPosition(this);
	}
}
