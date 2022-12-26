package log.charter.data.managers;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.data.managers.HighlightManager.PositionWithStringOrNoteId.fromNoteId;
import static log.charter.data.managers.HighlightManager.PositionWithStringOrNoteId.fromPosition;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.yToLane;
import static log.charter.song.enums.Position.findFirstIdAfter;
import static log.charter.song.enums.Position.findLastIdBefore;
import static log.charter.util.ScalingUtils.timeToX;
import static log.charter.util.ScalingUtils.xToTime;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.data.managers.selection.ChordOrNote;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.types.PositionType;
import log.charter.data.types.PositionWithIdAndType;
import log.charter.song.Beat;
import log.charter.song.enums.Position;
import log.charter.util.CollectionUtils.ArrayList2;

public class HighlightManager {
	public static class PositionWithStringOrNoteId extends Position {
		public static PositionWithStringOrNoteId fromChordId(final int chordId, final int position, final int string) {
			return new PositionWithStringOrNoteId(position, chordId, null, string);
		}

		public static PositionWithStringOrNoteId fromNoteId(final int noteId, final int position, final int string) {
			return new PositionWithStringOrNoteId(position, null, noteId, string);
		}

		public static PositionWithStringOrNoteId fromPosition(final int position, final int string) {
			return new PositionWithStringOrNoteId(position, null, null, string);
		}

		public final Integer chordId;
		public final Integer noteId;
		public final int string;

		private PositionWithStringOrNoteId(final int position, final Integer chordId, final Integer noteId,
				final int string) {
			super(position);
			this.chordId = chordId;
			this.noteId = noteId;
			this.string = string;
		}
	}

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
				return yToLane(fromY, data.getCurrentArrangement().tuning.strings);
			}

			final int y = fromY + (toY - fromY) * distance / maxDistance;
			return yToLane(y, data.getCurrentArrangement().tuning.strings);
		}

		private void addAvailablePositions() {
			final ArrayList2<Beat> beats = data.songChart.beatsMap.beats;
			final int beatIdFrom = max(0, findLastIdBefore(beats, fromPosition));
			final int beatIdTo = min(beats.size(), findFirstIdAfter(beats, toPosition));
			final int gridSize = data.songChart.beatsMap.gridSize;

			for (int beatId = beatIdFrom; beatId < beatIdTo; beatId++) {
				final Beat beat = beats.get(beatId);
				final Beat next = beats.get(beatId + 1);
				for (int gridId = 0; gridId < gridSize; gridId++) {
					final int gridPosition = beat.position + ((next.position - beat.position) * gridId / gridSize);
					if (gridPosition >= fromPosition && gridPosition <= toPosition) {
						positions.add(fromPosition(gridPosition, getLane(gridPosition)));
					}
				}
			}
		}

		private void addGuitarNotePositions() {
			final ArrayList2<ChordOrNote> chordsAndNotes = data.getCurrentArrangementLevel().chordsAndNotes;
			final int idFrom = max(0, findLastIdBefore(chordsAndNotes, fromPosition));
			final int idTo = min(chordsAndNotes.size() - 1, findFirstIdAfter(chordsAndNotes, toPosition));
			for (int i = idFrom; i <= idTo; i++) {
				final ChordOrNote chordOrNote = chordsAndNotes.get(i);
				if (chordOrNote.position >= fromPosition && chordOrNote.position <= toPosition) {
					noteChordPositions.add(fromNoteId(i, chordOrNote.position, getLane(chordOrNote.position)));
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
					if (abs(noteOrChord.position - position.position) < Config.minNoteDistance) {
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

	public PositionWithIdAndType getHighlight(final int x, final int y) {
		int position = xToTime(x, data.time);
		position = data.songChart.beatsMap.getPositionFromGridClosestTo(position);
		position = max(0, min(data.songChart.beatsMap.songLengthMs, position));

		final PositionWithIdAndType existingPosition = selectionManager
				.findExistingPosition(timeToX(position, data.time), y);

		if (existingPosition != null) {
			return existingPosition;
		}

		return PositionWithIdAndType.create(position, PositionType.fromY(y, modeManager.editMode));
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
