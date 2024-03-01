package log.charter.gui.handlers.data;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.song.notes.IConstantPosition.findFirstAfter;
import static log.charter.song.notes.IConstantPosition.findLastBefore;

import java.util.List;

import log.charter.data.ChartData;
import log.charter.data.managers.ModeManager;
import log.charter.song.notes.IConstantPosition;
import log.charter.util.CollectionUtils.ArrayList2;

public class ChartTimeHandler {
	private ChartData data;
	private ModeManager modeManager;
	private ProjectAudioHandler projectAudioHandler;

	private int time = 0;
	private int nextTime = 0;

	public void init(final ChartData data, final ModeManager modeManager,
			final ProjectAudioHandler projectAudioHandler) {
		this.data = data;
		this.modeManager = modeManager;
		this.projectAudioHandler = projectAudioHandler;
	}

	public int nextTime() {
		return nextTime;
	}

	public void setNextTime(final int t) {
		nextTime = max(0, min(audioLength(), t));
	}

	public int time() {
		return time;
	}

	public int audioLength() {
		return projectAudioHandler.getAudio().msLength();
	}

	private ArrayList2<? extends IConstantPosition> getCurrentItems() {
		switch (modeManager.getMode()) {
			case GUITAR:
				return data.getCurrentArrangementLevel().sounds;
			case TEMPO_MAP:
				return data.songChart.beatsMap.beats;
			case VOCALS:
				return data.songChart.vocals.vocals;
			default:
				return new ArrayList2<>();
		}
	}

	private int getPrevious(final List<? extends IConstantPosition> positions) {
		final IConstantPosition position = findLastBefore(positions, time);
		if (position == null) {
			return time;
		}

		return position.position();
	}

	public void moveToBeginning() {
		setNextTime(0);
	}

	public void moveToPreviousBeat() {
		setNextTime(getPrevious(data.songChart.beatsMap.beats));
	}

	public void moveToPreviousGrid() {
		setNextTime(data.songChart.beatsMap.getPositionWithRemovedGrid(time, 1));
	}

	public void moveToPreviousItem() {
		setNextTime(getPrevious(getCurrentItems()));
	}

	public void moveToFirstItem() {
		final ArrayList2<? extends IConstantPosition> items = getCurrentItems();
		if (items.isEmpty()) {
			return;
		}

		setNextTime(items.get(0).position());
	}

	private int getNext(final ArrayList2<? extends IConstantPosition> positions) {
		final IConstantPosition position = findFirstAfter(positions, time);
		if (position == null) {
			return time;
		}

		return position.position();
	}

	public void moveToEnd() {
		setNextTime(audioLength());
	}

	public void moveToNextBeat() {
		setNextTime(getNext(data.songChart.beatsMap.beats));
	}

	public void moveToNextGrid() {
		setNextTime(data.songChart.beatsMap.getPositionWithAddedGrid(time, 1));
	}

	public void moveToNextItem() {
		setNextTime(getNext(getCurrentItems()));
	}

	public void moveToLastItem() {
		final ArrayList2<? extends IConstantPosition> items = getCurrentItems();
		if (items.isEmpty()) {
			return;
		}

		setNextTime(items.getLast().position());
	}

	public void frame() {
		time = nextTime;
	}
}
