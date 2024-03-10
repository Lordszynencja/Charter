package log.charter.services.mouseAndKeyboard;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.data.config.Config.gridSize;
import static log.charter.data.config.Config.minNoteDistance;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.yToString;
import static log.charter.services.mouseAndKeyboard.PositionWithStringOrNoteId.fromNoteId;
import static log.charter.services.mouseAndKeyboard.PositionWithStringOrNoteId.fromPosition;
import static log.charter.song.notes.IConstantPosition.findClosest;
import static log.charter.song.notes.IConstantPosition.findFirstIdAfter;
import static log.charter.song.notes.IConstantPosition.findLastIdBefore;
import static log.charter.util.ScalingUtils.timeToX;
import static log.charter.util.ScalingUtils.xToTime;

import log.charter.data.ChartData;
import log.charter.data.types.PositionType;
import log.charter.data.types.PositionWithIdAndType;
import log.charter.services.data.ChartTimeHandler;
import log.charter.services.data.selection.SelectionManager;
import log.charter.services.editModes.ModeManager;
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
				return yToString(fromY, chartData.getCurrentArrangement().tuning.strings());
			}

			final int y = fromY + (toY - fromY) * distance / maxDistance;
			return yToString(y, chartData.getCurrentArrangement().tuning.strings());
		}

		private void addAvailablePositions() {
			final ArrayList2<Beat> beats = chartData.songChart.beatsMap.beats;
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
			final ArrayList2<ChordOrNote> chordsAndNotes = chartData.getCurrentArrangementLevel().sounds;
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

	private ChartData chartData;
	private ChartTimeHandler chartTimeHandler;
	private ModeManager modeManager;
	private SelectionManager selectionManager;

	private int snapPosition(final PositionType positionType, final int position) {
		if (positionType == PositionType.BEAT) {
			final Beat beat = findClosest(chartData.songChart.beatsMap.beats, position);
			return beat == null ? position : beat.position();
		}
		if (positionType != PositionType.ANCHOR) {
			return chartData.songChart.beatsMap.getPositionFromGridClosestTo(position);
		}

		final int closestGridPosition = chartData.songChart.beatsMap.getPositionFromGridClosestTo(position);
		final ChordOrNote closestSound = findClosest(chartData.getCurrentArrangementLevel().sounds, position);
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
		final PositionType positionType = PositionType.fromY(y, modeManager.getMode());

		final PositionWithIdAndType existingPosition = selectionManager.findExistingPosition(x, y);
		if (existingPosition != null) {
			return existingPosition;
		}

		int position = xToTime(x, chartTimeHandler.time());
		if (positionType != PositionType.BEAT) {
			position = snapPosition(positionType, position);
			position = max(0, min(chartData.songChart.beatsMap.beats.getLast().position(), position));

			final PositionWithIdAndType existingPositionCloseToGrid = selectionManager
					.findExistingPosition(timeToX(position, chartTimeHandler.time()), y);
			if (existingPositionCloseToGrid != null) {
				return existingPositionCloseToGrid;
			}
		}

		return new PositionWithIdAndType(position, positionType);
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
