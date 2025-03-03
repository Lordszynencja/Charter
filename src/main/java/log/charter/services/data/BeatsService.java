package log.charter.services.data;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.List;

import log.charter.data.ChartData;
import log.charter.data.song.Beat;
import log.charter.data.song.BeatsMap;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.types.PositionType;
import log.charter.data.types.PositionWithIdAndType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.services.mouseAndKeyboard.HighlightManager;
import log.charter.services.mouseAndKeyboard.KeyboardHandler;

public class BeatsService {
	private ChartData chartData;
	private ChartTimeHandler chartTimeHandler;
	private HighlightManager highlightManager;
	private KeyboardHandler keyboardHandler;
	private ProjectAudioHandler projectAudioHandler;
	private UndoSystem undoSystem;

	private void moveStart(final BeatsMap beatsMap, final double from, final double to) {
		final double positionAfter = max(0, min(chartTimeHandler.maxTime(), to));
		beatsMap.moveBeats(chartTimeHandler.maxTime(), positionAfter - from);
	}

	private double boundDraggedPosition(final BeatsMap beatsMap, final int id, double position,
			final boolean lookForRight) {
		final int leftId = beatsMap.findPreviousAnchoredBeat(id);
		final double leftPosition = beatsMap.beats.get(leftId).position();

		position = max(position, leftPosition + (id - leftId) * 10);

		if (lookForRight) {
			final Integer rightId = beatsMap.findNextAnchoredBeat(id);
			if (rightId != null) {
				final double rightPosition = beatsMap.beats.get(rightId).position();
				position = min(position, rightPosition - (rightId - id) * 10);
			}
		}

		return position;
	}

	private void straightenBeats(final ImmutableBeatsMap beats, final int from, final int to) {
		if (from >= to) {
			return;
		}

		final double positionFrom = beats.get(from).position();
		final double positionTo = beats.get(to).position();
		final int size = to - from;
		final double beatLength = (positionTo - positionFrom) / size;
		final double offset = positionFrom - from * beatLength;

		for (int i = from + 1; i < to; i++) {
			beats.get(i).position(offset + i * beatLength);
		}
	}

	private void fixLeftSideAfterDrag(final ImmutableBeatsMap beats, final int draggedId) {
		final int leftId = beats.findPreviousAnchoredBeat(draggedId);
		straightenBeats(beats, leftId, draggedId);
	}

	private void shiftRightSide(final ImmutableBeatsMap beats, final int draggedId, final double draggedFrom) {
		final double offset = beats.get(draggedId).position() - draggedFrom;
		for (int i = draggedId + 1; i < beats.size(); i++) {
			final Beat beat = beats.get(i);
			beat.position(beat.position() + offset);
		}
	}

	private void placeBeatsUntilEnd(final BeatsMap beatsMap, final int fromId) {
		double beatLength;
		if (fromId <= 0) {
			beatLength = 500;
		} else {
			beatLength = beatsMap.beats.get(fromId).position() - beatsMap.beats.get(fromId - 1).position();
		}

		beatsMap.setBPM(fromId, 60_000 / beatLength, projectAudioHandler.audioLengthMs());
	}

	private void fixRightSideAfterDrag(final BeatsMap beatsMap, final int draggedId, final double draggedFrom,
			final boolean shift) {
		if (shift) {
			shiftRightSide(beatsMap.immutable, draggedId, draggedFrom);
			return;
		}

		final Integer rightId = beatsMap.findNextAnchoredBeat(draggedId);
		if (rightId == null) {
			placeBeatsUntilEnd(beatsMap, draggedId);
		} else {
			straightenBeats(beatsMap.immutable, draggedId, rightId);
		}
	}

	public void dragTempo(final BeatsMap beatsMap, final PositionWithIdAndType pressHighlight, double to) {
		if (!pressHighlight.existingPosition) {
			return;
		}

		undoSystem.addUndo();

		final Beat movedBeat = beatsMap.beats.get(pressHighlight.id);
		final double dragPosition = movedBeat.position();

		if (pressHighlight.id == 0 || keyboardHandler.alt()) {
			moveStart(beatsMap, dragPosition, to);
			return;
		}

		final boolean shiftRightSide = keyboardHandler.shift();
		to = boundDraggedPosition(beatsMap, pressHighlight.id, to, !shiftRightSide);
		final double draggedFrom = movedBeat.position();

		movedBeat.position(to);
		movedBeat.anchor = true;

		fixLeftSideAfterDrag(beatsMap.immutable, pressHighlight.id);
		fixRightSideAfterDrag(beatsMap, pressHighlight.id, draggedFrom, shiftRightSide);

		chartData.songChart.beatsMap.makeBeatsUntilSongEnd(chartTimeHandler.maxTime());
	}

	private void moveBeats(final int fromId, final double offset) {
		final List<Beat> beats = chartData.songChart.beatsMap.beats;
		for (int i = fromId; i < beats.size(); i++) {
			final Beat beat = beats.get(i);
			beat.position(beat.position() + offset);
		}
	}

	private void moveBeatsForward(final int fromId) {
		final double offset = chartData.beats().get(fromId + 1).position() - chartData.beats().get(fromId).position();
		moveBeats(fromId, offset);
	}

	public void addBeat() {
		final PositionWithIdAndType highlight = highlightManager.getHighlight();
		if (highlight.type != PositionType.BEAT || !highlight.existingPosition
				|| highlight.id >= chartData.beats().size() - 1) {
			return;
		}

		undoSystem.addUndo();

		final double positionFrom = chartData.beats().get(highlight.id).position();

		chartData.songChart.moveContent(new FractionalPosition(highlight.id), new FractionalPosition(1));
		moveBeatsForward(highlight.id);
		chartData.songChart.beatsMap.beats.add(highlight.id, new Beat(positionFrom));
		chartData.songChart.beatsMap.fixFirstBeatInMeasures();
	}

	private void moveBeatsBackward(final int fromId) {
		final double offset = chartData.beats().get(fromId).position() - chartData.beats().get(fromId + 1).position();
		moveBeats(fromId, offset);
	}

	public void removeBeat() {
		final PositionWithIdAndType highlight = highlightManager.getHighlight();
		if (highlight.type != PositionType.BEAT || !highlight.existingPosition) {
			return;
		}

		undoSystem.addUndo();

		final int id = highlight.id;

		chartData.songChart.removeContent(new FractionalPosition(id), new FractionalPosition(id + 1));
		chartData.songChart.moveContent(new FractionalPosition(id), new FractionalPosition(-1));
		moveBeatsBackward(id);
		chartData.songChart.beatsMap.beats.remove(id);
		chartData.songChart.beatsMap.fixFirstBeatInMeasures();

	}

	public void toggleAnchor() {
		final PositionWithIdAndType highlight = highlightManager.getHighlight();
		if (!highlight.existingPosition || highlight.type != PositionType.BEAT || highlight.id == 0) {
			return;
		}

		undoSystem.addUndo();
		highlight.beat.anchor = !highlight.beat.anchor;

		if (!highlight.beat.anchor) {
			final int leftId = chartData.beats().findPreviousAnchoredBeat(highlight.id);
			final Integer rightId = chartData.beats().findNextAnchoredBeat(highlight.id);
			if (rightId == null) {
				return;
			}

			straightenBeats(chartData.beats(), leftId, rightId);
		}
	}
}
