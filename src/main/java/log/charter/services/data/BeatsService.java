package log.charter.services.data;

import static java.lang.Math.max;
import static java.lang.Math.min;

import log.charter.data.ChartData;
import log.charter.data.song.Beat;
import log.charter.data.song.BeatsMap;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.types.PositionWithIdAndType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.services.editModes.EditMode;
import log.charter.services.editModes.ModeManager;
import log.charter.services.mouseAndKeyboard.KeyboardHandler;

public class BeatsService {
	private ChartData chartData;
	private ChartTimeHandler chartTimeHandler;
	private KeyboardHandler keyboardHandler;
	private ModeManager modeManager;
	private ProjectAudioHandler projectAudioHandler;
	private UndoSystem undoSystem;

	private void moveStart(final double from, final double to) {
		final double positionAfter = max(0, min(chartTimeHandler.maxTime(), to));
		chartData.songChart.beatsMap.moveBeats(chartTimeHandler.maxTime(), positionAfter - from);
	}

	private double boundDraggedPosition(final int id, double position, final boolean lookForRight) {
		final ImmutableBeatsMap beats = chartData.beats();
		final int leftId = beats.findPreviousAnchoredBeat(id);
		final double leftPosition = beats.get(leftId).position();

		position = max(position, leftPosition + (id - leftId) * 10);

		if (lookForRight) {
			final Integer rightId = beats.findNextAnchoredBeat(id);
			if (rightId != null) {
				final double rightPosition = beats.get(rightId).position();
				position = min(position, rightPosition - (rightId - id) * 10);
			}
		}

		return position;
	}

	private void straightenBeats(final int from, final int to) {
		if (from >= to) {
			return;
		}

		final ImmutableBeatsMap beats = chartData.beats();

		final double positionFrom = beats.get(from).position();
		final double positionTo = beats.get(to).position();
		final int size = to - from;
		final double beatLength = (positionTo - positionFrom) / size;
		final double offset = positionFrom - from * beatLength;

		for (int i = from + 1; i < to; i++) {
			beats.get(i).position(offset + i * beatLength);
		}
	}

	private void fixLeftSideAfterDrag(final int draggedId) {
		final int leftId = chartData.beats().findPreviousAnchoredBeat(draggedId);
		straightenBeats(leftId, draggedId);
	}

	private void shiftRightSide(final int draggedId, final double draggedFrom) {
		final ImmutableBeatsMap beats = chartData.beats();
		final double offset = beats.get(draggedId).position() - draggedFrom;
		for (int i = draggedId + 1; i < beats.size(); i++) {
			final Beat beat = beats.get(i);
			beat.position(beat.position() + offset);
		}
	}

	private void placeBeatsUntilEnd(final int fromId) {
		final BeatsMap beatsMap = chartData.songChart.beatsMap;

		double beatLength;
		if (fromId <= 0) {
			beatLength = 500;
		} else {
			beatLength = beatsMap.beats.get(fromId).position() - beatsMap.beats.get(fromId - 1).position();
		}

		beatsMap.setBPM(fromId, 60_000 / beatLength, projectAudioHandler.audioLengthMs());
	}

	private void fixRightSideAfterDrag(final int draggedId, final double draggedFrom, final boolean shift) {
		final ImmutableBeatsMap beats = chartData.beats();
		if (shift) {
			shiftRightSide(draggedId, draggedFrom);
			return;
		}

		final Integer rightId = beats.findNextAnchoredBeat(draggedId);
		if (rightId == null) {
			placeBeatsUntilEnd(draggedId);
		} else {
			straightenBeats(draggedId, rightId);
		}
	}

	public void dragTempo(final PositionWithIdAndType pressHighlight, double to) {
		if (modeManager.getMode() != EditMode.TEMPO_MAP || !pressHighlight.existingPosition) {
			return;
		}

		undoSystem.addUndo();

		final Beat movedBeat = pressHighlight.beat;
		final double dragPosition = movedBeat.position();

		if (pressHighlight.id == 0 || keyboardHandler.alt()) {
			moveStart(dragPosition, to);
			return;
		}

		final boolean shiftRightSide = keyboardHandler.shift();
		to = boundDraggedPosition(pressHighlight.id, to, !shiftRightSide);
		final double draggedFrom = movedBeat.position();

		movedBeat.position(to);
		movedBeat.anchor = true;

		fixLeftSideAfterDrag(pressHighlight.id);
		fixRightSideAfterDrag(pressHighlight.id, draggedFrom, shiftRightSide);

		chartData.songChart.beatsMap.makeBeatsUntilSongEnd(chartTimeHandler.maxTime());
	}
}
