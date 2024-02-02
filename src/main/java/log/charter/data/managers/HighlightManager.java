package log.charter.data.managers;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.data.config.Config.gridSize;
import static log.charter.data.config.Config.minNoteDistance;
import static log.charter.data.managers.PositionWithStringOrNoteId.fromNoteId;
import static log.charter.data.managers.PositionWithStringOrNoteId.fromPosition;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.yToString;
import static log.charter.song.notes.IConstantPosition.findClosest;
import static log.charter.song.notes.IConstantPosition.findClosestPosition;
import static log.charter.song.notes.IConstantPosition.findFirstIdAfter;
import static log.charter.song.notes.IConstantPosition.findLastIdBefore;
import static log.charter.util.ScalingUtils.timeToX;
import static log.charter.util.ScalingUtils.xToTime;

import log.charter.data.ChartData;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.types.PositionType;
import log.charter.data.types.PositionWithIdAndType;
import log.charter.song.Beat;
import log.charter.song.notes.ChordOrNote;
import log.charter.util.CollectionUtils.ArrayList2;

public class HighlightManager {
	private class PositionsWithStringsCalculator {
		private final int fromPosition;
		private final int toPosition;
		private final int fromY;
		private final int toY;

		private final ArrayList2<PositionWithStringOrNoteId> positions = new ArrayList2<>();
		private final ArrayList2<PositionWithStringOrNoteId> noteChordPositions = new ArrayList2<>();

		public PositionsWithStringsCalculator(final int fromPosition, final int toPosition, final int fromY,
				final int toY) {
			this.fromPosition = fromPosition;
			this.toPosition = toPosition;
			this.fromY = fromY;
			this.toY = toY;
		}

		private int getLane(final int position) {
			final int distance = position - fromPosition;
			final int maxDistance = toPosition - fromPosition;
			if (distance == 0) {
				return yToString(fromY, data.getCurrentArrangement().tuning.strings);
			}

			final int y = fromY + (toY - fromY) * distance / maxDistance;
			return yToString(y, data.getCurrentArrangement().tuning.strings);
		}

		private void addAvailablePositions() {
			final ArrayList2<Beat> beats = data.songChart.beatsMap.beats;
			final int beatIdFrom = max(0, findLastIdBefore(beats, fromPosition));
			final int beatIdTo = min(beats.size(), findFirstIdAfter(beats, toPosition));

			for (int beatId = beatIdFrom; beatId < beatIdTo; beatId++) {
				final Beat beat = beats.get(beatId);
				final Beat next = beats.get(beatId + 1);
				for (int gridId = 0; gridId < gridSize; gridId++) {
					final int gridPosition = beat.position()
							+ ((next.position() - beat.position()) * gridId / gridSize);
					if (gridPosition >= fromPosition && gridPosition <= toPosition) {
						positions.add(fromPosition(gridPosition, getLane(gridPosition)));
					}
				}
			}
		}

		private void addGuitarNotePositions() {
			final ArrayList2<ChordOrNote> chordsAndNotes = data.getCurrentArrangementLevel().chordsAndNotes;
			int idFrom = findLastIdBefore(chordsAndNotes, fromPosition);
			int idTo = findFirstIdAfter(chordsAndNotes, toPosition);
			if (idFrom == -1) {
				idFrom = 0;
			}
			if (idTo == -1) {
				idTo = chordsAndNotes.size() - 1;
			}
			for (int i = idFrom; i <= idTo; i++) {
				final ChordOrNote chordOrNote = chordsAndNotes.get(i);
				if (chordOrNote.position() >= fromPosition && chordOrNote.position() <= toPosition) {
					noteChordPositions.add(fromNoteId(i, chordOrNote, getLane(chordOrNote.position())));
				}
			}
		}

		public ArrayList2<PositionWithStringOrNoteId> getPositionsWithStrings() {
			addAvailablePositions();
			addGuitarNotePositions();

			final ArrayList2<PositionWithStringOrNoteId> finalPositions = new ArrayList2<>();

			for (final PositionWithStringOrNoteId position : positions) {
				boolean isCloseToNoteOrChord = false;
				for (final PositionWithStringOrNoteId noteOrChord : noteChordPositions) {
					if (abs(noteOrChord.position() - position.position()) < minNoteDistance) {
						isCloseToNoteOrChord = true;
						break;
					}
				}

				if (!isCloseToNoteOrChord) {
					finalPositions.add(position);
				}
			}
			finalPositions.addAll(noteChordPositions);
			finalPositions.sort(null);

			return finalPositions;
		}
	}

	private ChartData data;
	private ModeManager modeManager;
	private SelectionManager selectionManager;

	public void init(final ChartData data, final ModeManager modeManager, final SelectionManager selectionManager) {
		this.data = data;
		this.modeManager = modeManager;
		this.selectionManager = selectionManager;
	}

	private int snapPosition(final PositionType positionType, final int position) {
		if (positionType == PositionType.BEAT) {
			return findClosest(data.songChart.beatsMap.beats, position);
		}
		if (positionType != PositionType.ANCHOR) {
			return data.songChart.beatsMap.getPositionFromGridClosestTo(position);
		}

		final int closestGridPosition = data.songChart.beatsMap.getPositionFromGridClosestTo(position);
		final ChordOrNote closestSound = findClosestPosition(data.getCurrentArrangementLevel().chordsAndNotes,
				position);
		if (closestSound == null) {
			return closestGridPosition;
		}
		final int closestNotePosition = closestSound.position();

		if (abs(closestGridPosition - position) < abs(closestNotePosition - position) + 10) {
			return closestGridPosition;
		}

		return closestNotePosition;
	}

	public PositionWithIdAndType getHighlight(final int x, final int y) {
		final PositionType positionType = PositionType.fromY(y, modeManager.editMode);

		final PositionWithIdAndType existingPosition = selectionManager.findExistingPosition(x, y);
		if (existingPosition != null) {
			return existingPosition;
		}
		int position = xToTime(x, data.time);
		if (positionType == PositionType.BEAT) {
			return PositionWithIdAndType.create(position, positionType);
		}

		position = snapPosition(positionType, position);
		position = max(0, min(data.songChart.beatsMap.beats.getLast().position(), position));

		final PositionWithIdAndType existingPositionCloseToGrid = selectionManager
				.findExistingPosition(timeToX(position, data.time), y);
		if (existingPositionCloseToGrid != null) {
			return existingPositionCloseToGrid;
		}

		return PositionWithIdAndType.create(position, positionType);
	}

	public ArrayList2<PositionWithStringOrNoteId> getPositionsWithStrings(final int fromPosition, final int toPosition,
			final int fromY, final int toY) {
		final PositionsWithStringsCalculator calculator;
		if (fromPosition > toPosition) {
			calculator = new PositionsWithStringsCalculator(toPosition, fromPosition, toY, fromY);
		} else {
			calculator = new PositionsWithStringsCalculator(fromPosition, toPosition, fromY, toY);
		}

		return calculator.getPositionsWithStrings();
	}
}
