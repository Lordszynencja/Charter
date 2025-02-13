package log.charter.services.mouseAndKeyboard;

import static java.lang.Math.abs;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.yToString;
import static log.charter.services.mouseAndKeyboard.PositionWithStringOrNoteId.fromNoteId;
import static log.charter.services.mouseAndKeyboard.PositionWithStringOrNoteId.fromPosition;
import static log.charter.util.CollectionUtils.closest;
import static log.charter.util.CollectionUtils.firstAfter;
import static log.charter.util.CollectionUtils.lastBefore;
import static log.charter.util.CollectionUtils.max;
import static log.charter.util.CollectionUtils.min;
import static log.charter.util.ScalingUtils.xToPosition;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import log.charter.data.ChartData;
import log.charter.data.song.Beat;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.fractional.IConstantFractionalPosition;
import log.charter.data.song.position.time.ConstantPosition;
import log.charter.data.song.position.time.IConstantPosition;
import log.charter.data.song.position.time.Position;
import log.charter.data.song.position.virtual.IVirtualConstantPosition;
import log.charter.data.types.PositionType;
import log.charter.data.types.PositionWithIdAndType;
import log.charter.services.data.ChartTimeHandler;
import log.charter.services.data.selection.SelectionManager;
import log.charter.services.editModes.ModeManager;
import log.charter.util.grid.GridPosition;

public class HighlightManager {
	private class PositionsWithStringsCalculator {
		private final double fromPosition;
		private final double toPosition;
		private final FractionalPosition fromPositionFractional;
		private final FractionalPosition toPositionFractional;
		private final int fromY;
		private final int toY;

		private final List<PositionWithStringOrNoteId> positions = new ArrayList<>();
		private final List<PositionWithStringOrNoteId> noteChordPositions = new ArrayList<>();

		public PositionsWithStringsCalculator(final double fromPosition, final double toPosition, final int fromY,
				final int toY) {
			this.fromPosition = fromPosition;
			this.toPosition = toPosition;
			fromPositionFractional = FractionalPosition.fromTime(chartData.beats(), fromPosition);
			toPositionFractional = FractionalPosition.fromTime(chartData.beats(), toPosition);
			this.fromY = fromY;
			this.toY = toY;
		}

		private int getLane(final double position) {
			final double distance = position - fromPosition;
			final double maxDistance = toPosition - fromPosition;
			if (distance == 0 || maxDistance == 0) {
				return yToString(fromY, chartData.currentArrangement().tuning.strings());
			}

			final int y = (int) (fromY + (toY - fromY) * distance / maxDistance);
			return yToString(y, chartData.currentArrangement().tuning.strings());
		}

		private void addAvailablePositions() {
			final ImmutableBeatsMap beats = chartData.beats();
			final GridPosition<Beat> gridPosition = GridPosition.create(beats, fromPositionFractional);

			while (gridPosition.compareTo(toPositionFractional) <= 0) {
				final FractionalPosition position = gridPosition.fractionalPosition();
				final int lane = getLane(position.toPosition(beats).position());
				positions.add(fromPosition(position, lane));

				gridPosition.next();
			}
		}

		private void addGuitarNotePositions() {
			final List<ChordOrNote> sounds = chartData.currentSounds();
			final int idFrom = lastBefore(sounds, fromPositionFractional).findId(0);
			final int idTo = firstAfter(sounds, toPositionFractional).findId(sounds.size() - 1);

			final ImmutableBeatsMap beats = chartData.beats();
			for (int i = idFrom; i <= idTo; i++) {
				final ChordOrNote chordOrNote = sounds.get(i);
				if (chordOrNote.compareTo(fromPositionFractional) >= 0
						&& chordOrNote.compareTo(toPositionFractional) <= 0) {
					noteChordPositions.add(fromNoteId(i, chordOrNote, getLane(chordOrNote.position(beats))));
				}
			}
		}

		public List<PositionWithStringOrNoteId> getPositionsWithStrings() {
			addAvailablePositions();
			addGuitarNotePositions();

			final List<PositionWithStringOrNoteId> finalPositions = new ArrayList<>();

			final ImmutableBeatsMap beats = chartData.beats();
			for (final PositionWithStringOrNoteId position : positions) {
				boolean isCloseToNoteOrChord = false;
				for (final PositionWithStringOrNoteId noteOrChord : noteChordPositions) {
					if (abs(noteOrChord.position(beats) - position.position(beats)) < 20) {
						isCloseToNoteOrChord = true;
						break;
					}
				}

				if (!isCloseToNoteOrChord) {
					finalPositions.add(position);
				}
			}
			finalPositions.addAll(noteChordPositions);
			finalPositions.sort(IConstantFractionalPosition::compareTo);

			return finalPositions;
		}
	}

	private ChartData chartData;
	private ChartTimeHandler chartTimeHandler;
	private ModeManager modeManager;
	private SelectionManager selectionManager;

	private IConstantPosition snapBeat(final double position) {
		final Beat beat = closest(chartData.beats(), new Position(position), IConstantPosition::compareTo,
				p -> p.position()).find();
		return new ConstantPosition(beat == null ? position : beat.position());
	}

	private IConstantFractionalPosition snapNotFHP(final double position) {
		return chartData.beats().getPositionFromGridClosestTo(new Position(position)).toFraction(chartData.beats());
	}

	private IConstantFractionalPosition snapFHP(final double position) {
		final FractionalPosition closestGridPosition = chartData.beats()
				.getPositionFromGridClosestTo(new Position(position));

		final ChordOrNote closestSound = closest(chartData.currentSounds(),
				closestGridPosition.toFraction(chartData.beats())).find();
		if (closestSound == null) {
			return closestGridPosition;
		}

		final FractionalPosition closestNotePosition = closestSound.position();

		if (abs(closestGridPosition.position(chartData.beats()) - position)
				- abs(closestNotePosition.position(chartData.beats()) - position) < 10) {
			return closestGridPosition;
		}

		return closestNotePosition;
	}

	private IVirtualConstantPosition snapPosition(final PositionType positionType, final double position) {
		if (positionType == PositionType.BEAT) {
			return snapBeat(position);
		}
		if (positionType != PositionType.FHP) {
			return snapNotFHP(position);
		}

		return snapFHP(position);
	}

	public PositionWithIdAndType getHighlight(final int x, final int y) {
		final PositionType positionType = PositionType.fromY(y, modeManager.getMode());

		final PositionWithIdAndType existingPosition = selectionManager.findExistingPosition(x, y);
		if (existingPosition != null) {
			return existingPosition;
		}

		final ImmutableBeatsMap beats = chartData.beats();
		final double mouseTime = xToPosition(x, chartTimeHandler.time());
		if (positionType == PositionType.BEAT) {
			return PositionWithIdAndType.of(beats, mouseTime, positionType);
		}

		final Comparator<IVirtualConstantPosition> comparator = IVirtualConstantPosition.comparator(beats);
		IVirtualConstantPosition position = snapPosition(positionType, mouseTime);
		position = max(comparator, new FractionalPosition(),
				min(comparator, new Position(chartTimeHandler.maxTime()), position));
		final int positionX = chartTimeHandler.positionToX(position.toPosition(beats).position());

		final PositionWithIdAndType existingPositionCloseToGrid = selectionManager.findExistingPosition(positionX, y);
		if (existingPositionCloseToGrid != null) {
			return existingPositionCloseToGrid;
		}

		return PositionWithIdAndType.of(beats, position, positionType);
	}

	public List<PositionWithStringOrNoteId> getPositionsWithStrings(final double fromPosition, final double toPosition,
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
