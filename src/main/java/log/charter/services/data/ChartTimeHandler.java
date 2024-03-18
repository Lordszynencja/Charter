package log.charter.services.data;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.util.CollectionUtils.firstAfter;
import static log.charter.util.CollectionUtils.lastBefore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import log.charter.data.ChartData;
import log.charter.data.song.Arrangement;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.Level;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.fractional.IConstantFractionalPosition;
import log.charter.data.song.position.time.ConstantPosition;
import log.charter.data.song.position.time.IConstantPosition;
import log.charter.data.song.position.time.Position;
import log.charter.data.song.position.virtual.IVirtualConstantPosition;
import log.charter.services.Action;
import log.charter.services.editModes.EditMode;
import log.charter.services.editModes.ModeManager;
import log.charter.services.mouseAndKeyboard.KeyboardHandler;
import log.charter.util.CollectionUtils;
import log.charter.util.ScalingUtils;
import log.charter.util.collections.Pair;

public class ChartTimeHandler {
	private static class MaxPositionAccumulator {
		public int maxTime = 1;

		public void add(final int time) {
			maxTime = max(maxTime, time);
		}

		public void add(final List<? extends IConstantPosition> positions) {
			if (!positions.isEmpty()) {
				maxTime = max(maxTime, positions.get(positions.size() - 1).position());
			}
		}

		public void addFractional(final ImmutableBeatsMap beats,
				final List<? extends IConstantFractionalPosition> positions) {
			if (!positions.isEmpty()) {
				try {
					maxTime = max(maxTime, positions.get(positions.size() - 1).position().getPosition(beats));
				} catch (final Exception e) {
				}
			}
		}
	}

	private ChartData chartData;
	private KeyboardHandler keyboardHandler;
	private ModeManager modeManager;
	private ProjectAudioHandler projectAudioHandler;

	private double time = 0;
	private FractionalPosition fractionalTime = new FractionalPosition();
	private double nextTime = 0;
	private FractionalPosition nextFractionalTime = new FractionalPosition();

	public double nextTime() {
		return nextTime;
	}

	public void nextTime(final double t) {
		nextTime = max(0, min(maxTime(), t));
		nextFractionalTime = FractionalPosition.fromTime(chartData.beats(), (int) nextTime);
	}

	public void nextTime(final IConstantPosition t) {
		nextTime(t.position());
	}

	public FractionalPosition nextFractionalTime() {
		return nextFractionalTime;
	}

	public void nextFractionalTime(final IConstantFractionalPosition t) {
		nextFractionalTime = t.position();
		nextTime = nextFractionalTime.getPosition(chartData.beats());
	}

	public void nextTime(final IVirtualConstantPosition p) {
		if (p.isFraction()) {
			nextFractionalTime(p.asConstantFraction());
		} else {
			nextTime(p.asConstantPosition());
		}
	}

	public void moveTo(final Integer arrangementId, final Integer levelId, final IVirtualConstantPosition time) {
		if (arrangementId != null) {
			modeManager.setArrangement(arrangementId);
		}

		if (levelId != null) {
			modeManager.setLevel(levelId);
		}

		if (time != null) {
			nextTime(time);
		}
	}

	public double preciseTime() {
		return time;
	}

	public int time() {
		return (int) time;
	}

	public FractionalPosition timeFractional() {
		return fractionalTime;
	}

	public int maxTime() {
		final MaxPositionAccumulator accumulator = new MaxPositionAccumulator();

		if (modeManager.getMode() != EditMode.EMPTY) {
			final ImmutableBeatsMap beats = chartData.beats();
			accumulator.add(audioTime());
			accumulator.add(beats);

			for (final Arrangement arrangement : chartData.songChart.arrangements) {
				accumulator.addFractional(beats, arrangement.eventPoints);
				accumulator.addFractional(beats, arrangement.toneChanges);

				for (final Level level : arrangement.levels) {
					accumulator.addFractional(beats, level.anchors);
					accumulator.addFractional(beats, level.sounds);
					accumulator.addFractional(beats, level.handShapes);
				}
			}
		}

		return accumulator.maxTime;
	}

	public FractionalPosition maxTimeFractional() {
		return FractionalPosition.fromTime(chartData.beats(), maxTime());
	}

	public int audioTime() {
		return projectAudioHandler.getAudio().msLength();
	}

	public int positionToX(final int position) {
		return ScalingUtils.positionToX(position, time());
	}

	public int xToPosition(final int x) {
		return ScalingUtils.xToPosition(x, time());
	}

	private List<? extends IVirtualConstantPosition> getCurrentItems() {
		switch (modeManager.getMode()) {
			case GUITAR:
				return chartData.currentSounds();
			case TEMPO_MAP:
				return chartData.songChart.beatsMap.beats;
			case VOCALS:
				return chartData.songChart.vocals.vocals;
			default:
				return new ArrayList<>();
		}
	}

	public void moveToBeginning() {
		nextTime(0);
	}

	public void moveToPreviousGrid() {
		nextTime(chartData.beats().addGrid(new Position(time()), -1));
	}

	private IConstantPosition getPrevious(final List<? extends IConstantPosition> positions) {
		final IConstantPosition timePosition = new ConstantPosition(time());
		final IConstantPosition position = lastBefore(positions, timePosition).find();
		if (position == null) {
			return timePosition;
		}

		return position;
	}

	private IConstantFractionalPosition getPreviousFractional(
			final List<? extends IConstantFractionalPosition> positions) {
		final IConstantFractionalPosition timePosition = FractionalPosition.fromTime(chartData.beats(), time());
		final IConstantFractionalPosition position = lastBefore(positions, timePosition).find();
		if (position == null) {
			return timePosition;
		}

		return position;
	}

	public void moveToPreviousBeat() {
		nextTime(getPrevious(chartData.beats()));
	}

	@SuppressWarnings("unchecked")
	public void moveToPreviousItem() {
		final List<? extends IVirtualConstantPosition> currentItems = getCurrentItems();
		if (currentItems.isEmpty()) {
			return;
		}

		if (currentItems.get(0).isFraction()) {
			nextFractionalTime(getPreviousFractional((List<? extends IConstantFractionalPosition>) currentItems));
		} else {
			nextTime(getPrevious((List<? extends IConstantPosition>) currentItems));
		}
	}

	public void moveToFirstItem() {
		final List<? extends IVirtualConstantPosition> items = getCurrentItems();
		if (items.isEmpty()) {
			return;
		}

		nextTime(items.get(0));
	}

	private int getNext(final List<? extends IConstantPosition> positions) {
		final IConstantPosition position = firstAfter(positions, new Position(time())).find();
		if (position == null) {
			return time();
		}

		return position.position();
	}

	private IConstantFractionalPosition getNextFractional(final List<? extends IConstantFractionalPosition> positions) {
		final IConstantFractionalPosition timePosition = FractionalPosition.fromTime(chartData.beats(), time());
		final IConstantFractionalPosition position = firstAfter(positions, timePosition).find();
		if (position == null) {
			return timePosition;
		}

		return position;
	}

	public void moveToEnd() {
		nextTime(maxTime());
	}

	public void moveToNextBeat() {
		nextTime(getNext(chartData.beats()));
	}

	public void moveToNextGrid() {
		nextTime(chartData.beats().addGrid(new Position(time()), 1));
	}

	@SuppressWarnings("unchecked")
	public void moveToNextItem() {
		final List<? extends IVirtualConstantPosition> currentItems = getCurrentItems();
		if (currentItems.isEmpty()) {
			return;
		}

		if (currentItems.get(0).isFraction()) {
			nextFractionalTime(getNextFractional((List<? extends IConstantFractionalPosition>) currentItems));
		} else {
			nextTime(getNext((List<? extends IConstantPosition>) currentItems));
		}
	}

	public void moveToLastItem() {
		final List<? extends IVirtualConstantPosition> items = getCurrentItems();
		if (items.isEmpty()) {
			return;
		}

		nextTime(items.get(items.size() - 1));
	}

	private static final Map<Action, Double> moveSpeeds = CollectionUtils.toMap(//
			new Pair<>(Action.FAST_FORWARD, 32.0), //
			new Pair<>(Action.FAST_BACKWARD, -32.0), //
			new Pair<>(Action.MOVE_FORWARD, 4.0), //
			new Pair<>(Action.MOVE_BACKWARD, -4.0), //
			new Pair<>(Action.SLOW_FORWARD, 0.25), //
			new Pair<>(Action.SLOW_BACKWARD, -0.25));

	private void moveFromArrows(final double frameTime) {
		final Optional<Action> heldAction = keyboardHandler.heldAction();
		if (heldAction.filter(moveSpeeds::containsKey).isEmpty()) {
			return;
		}

		final double speed = moveSpeeds.get(heldAction.get()) * frameTime * 1000;
		nextTime(time() + speed);
	}

	public void frame(final double frameTime) {
		moveFromArrows(frameTime);

		time = nextTime;
		fractionalTime = nextFractionalTime;
		nextTime = time;
		nextFractionalTime = fractionalTime;
	}
}
