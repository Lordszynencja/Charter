package log.charter.services.mouseAndKeyboard;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.data.config.Config.gridSize;
import static log.charter.data.config.Config.minNoteDistance;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.yToString;
import static log.charter.services.mouseAndKeyboard.PositionWithStringOrNoteId.fromNoteId;
import static log.charter.services.mouseAndKeyboard.PositionWithStringOrNoteId.fromPosition;
import static log.charter.util.CollectionUtils.closest;
import static log.charter.util.CollectionUtils.firstAfter;
import static log.charter.util.CollectionUtils.lastBefore;
import static log.charter.util.ScalingUtils.timeToX;
import static log.charter.util.ScalingUtils.xToTime;

import java.util.ArrayList;
import java.util.List;

import log.charter.data.ChartData;
import log.charter.data.song.Beat;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.position.IConstantPosition;
import log.charter.data.song.position.IVirtualConstantPosition;
import log.charter.data.song.position.Position;
import log.charter.data.types.PositionType;
import log.charter.data.types.PositionWithIdAndType;
import log.charter.services.data.ChartTimeHandler;
import log.charter.services.data.selection.SelectionManager;
import log.charter.services.editModes.ModeManager;
import log.charter.util.collections.ArrayList2;

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
				return yToString(fromY, chartData.currentArrangement().tuning.strings());
			}

			final int y = fromY + (toY - fromY) * distance / maxDistance;
			return yToString(y, chartData.currentArrangement().tuning.strings());
		}

		private void addAvailablePositions() {
			final List<Beat> beats = chartData.beats();
			final int beatIdFrom = lastBefore(beats, new Position(fromPosition), IConstantPosition::compareTo)
					.findId(0);
			final int beatIdTo = firstAfter(beats, new Position(toPosition), IConstantPosition::compareTo)
					.findId(beats.size());

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
			final List<ChordOrNote> sounds = chartData.currentSounds();
			final int idFrom = lastBefore(sounds, new Position(fromPosition), IConstantPosition::compareTo).findId(0);
			final int idTo = firstAfter(sounds, new Position(toPosition), IConstantPosition::compareTo)
					.findId(sounds.size() - 1);

			for (int i = idFrom; i <= idTo; i++) {
				final ChordOrNote chordOrNote = sounds.get(i);
				if (chordOrNote.position() >= fromPosition && chordOrNote.position() <= toPosition) {
					noteChordPositions.add(fromNoteId(i, chordOrNote, getLane(chordOrNote.position())));
				}
			}
		}

		public List<PositionWithStringOrNoteId> getPositionsWithStrings() {
			addAvailablePositions();
			addGuitarNotePositions();

			final List<PositionWithStringOrNoteId> finalPositions = new ArrayList<>();

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
			finalPositions.sort(IConstantPosition::compareTo);

			return finalPositions;
		}
	}

	private ChartData chartData;
	private ChartTimeHandler chartTimeHandler;
	private ModeManager modeManager;
	private SelectionManager selectionManager;

	private int snapPosition(final PositionType positionType, final int position) {
		if (positionType == PositionType.BEAT) {
			final Beat beat = closest(chartData.beats(), new Position(position), IConstantPosition::compareTo,
					p -> p.position()).find();
			return beat == null ? position : beat.position();
		}
		if (positionType != PositionType.ANCHOR) {
			return chartData.beats().getPositionFromGridClosestTo(new Position(position))
					.toPosition(chartData.beats()).position();
		}

		final IVirtualConstantPosition closestGridPositionA = chartData.beats()
				.getPositionFromGridClosestTo(new Position(position));

		final int closestGridPosition = closestGridPositionA.toPosition(chartData.beats()).position();

		final ChordOrNote closestSound = closest(chartData.currentSounds(), new Position(position),
				IConstantPosition::compareTo, x -> x.position()).find();
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
			position = max(0, min(chartData.beats().get(chartData.beats().size() - 1).position(), position));

			final PositionWithIdAndType existingPositionCloseToGrid = selectionManager
					.findExistingPosition(timeToX(position, chartTimeHandler.time()), y);
			if (existingPositionCloseToGrid != null) {
				return existingPositionCloseToGrid;
			}
		}

		return PositionWithIdAndType.of(chartData.beats(), position, positionType);
	}

	public List<PositionWithStringOrNoteId> getPositionsWithStrings(final int fromPosition, final int toPosition,
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
