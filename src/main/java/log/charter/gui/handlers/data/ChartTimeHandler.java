package log.charter.gui.handlers.data;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.song.notes.IConstantPosition.findFirstAfter;
import static log.charter.song.notes.IConstantPosition.findLastBefore;

import java.util.List;

import log.charter.data.ChartData;
import log.charter.data.managers.ModeManager;
import log.charter.gui.handlers.mouseAndKeyboard.KeyboardHandler;
import log.charter.song.notes.IConstantPosition;
import log.charter.util.CollectionUtils.ArrayList2;

public class ChartTimeHandler {
	private ChartData chartData;
	private KeyboardHandler keyboardHandler;
	private ModeManager modeManager;
	private ProjectAudioHandler projectAudioHandler;

	private double time = 0;
	private double nextTime = 0;

	public double nextTime() {
		return nextTime;
	}

	public void nextTime(final double t) {
		nextTime = max(0, min(maxTime(), t));
	}

	public double preciseTime() {
		return time;
	}

	public int time() {
		return (int) time;
	}

	public int maxTime() {
		return max(1, projectAudioHandler.getAudio().msLength());
	}

	public int audioTime() {
		return projectAudioHandler.getAudio().msLength();
	}

	private ArrayList2<? extends IConstantPosition> getCurrentItems() {
		switch (modeManager.getMode()) {
			case GUITAR:
				return chartData.getCurrentArrangementLevel().sounds;
			case TEMPO_MAP:
				return chartData.songChart.beatsMap.beats;
			case VOCALS:
				return chartData.songChart.vocals.vocals;
			default:
				return new ArrayList2<>();
		}
	}

	private int getPrevious(final List<? extends IConstantPosition> positions) {
		final IConstantPosition position = findLastBefore(positions, time());
		if (position == null) {
			return time();
		}

		return position.position();
	}

	public void moveToBeginning() {
		nextTime(0);
	}

	public void moveToPreviousBeat() {
		nextTime(getPrevious(chartData.songChart.beatsMap.beats));
	}

	public void moveToPreviousGrid() {
		nextTime(chartData.songChart.beatsMap.getPositionWithRemovedGrid(time(), 1));
	}

	public void moveToPreviousItem() {
		nextTime(getPrevious(getCurrentItems()));
	}

	public void moveToFirstItem() {
		final ArrayList2<? extends IConstantPosition> items = getCurrentItems();
		if (items.isEmpty()) {
			return;
		}

		nextTime(items.get(0).position());
	}

	private int getNext(final ArrayList2<? extends IConstantPosition> positions) {
		final IConstantPosition position = findFirstAfter(positions, time());
		if (position == null) {
			return time();
		}

		return position.position();
	}

	public void moveToEnd() {
		nextTime(maxTime());
	}

	public void moveToNextBeat() {
		nextTime(getNext(chartData.songChart.beatsMap.beats));
	}

	public void moveToNextGrid() {
		nextTime(chartData.songChart.beatsMap.getPositionWithAddedGrid(time(), 1));
	}

	public void moveToNextItem() {
		nextTime(getNext(getCurrentItems()));
	}

	public void moveToLastItem() {
		final ArrayList2<? extends IConstantPosition> items = getCurrentItems();
		if (items.isEmpty()) {
			return;
		}

		nextTime(items.getLast().position());
	}

	public void frame(final double frameTime) {
		double speed = keyboardHandler.heldAction()//
				.map(action -> switch (action) {
					case FAST_BACKWARD -> -32.0;
					case FAST_FORWARD -> 32.0;
					case MOVE_BACKWARD -> -4.0;
					case MOVE_FORWARD -> 4.0;
					case SLOW_BACKWARD -> -0.25;
					case SLOW_FORWARD -> 0.25;
					default -> 0.0;
				}).orElse(0.0);
		speed *= frameTime * 1000;

		time = max(0, min(maxTime(), nextTime + speed));
		nextTime = time;
	}
}
