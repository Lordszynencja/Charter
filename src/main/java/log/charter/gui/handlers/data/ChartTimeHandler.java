package log.charter.gui.handlers.data;

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

	public void init(final ChartData data, final ModeManager modeManager) {
		this.data = data;
		this.modeManager = modeManager;
	}

	public void setNextTime(final int t) {
		data.setNextTime(t);
	}

	public long getTime() {
		return data.time;
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
		final IConstantPosition position = findLastBefore(positions, data.time);
		if (position == null) {
			return data.time;
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
		setNextTime(data.songChart.beatsMap.getPositionWithRemovedGrid(data.time, 1));
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
		final IConstantPosition position = findFirstAfter(positions, data.time);
		if (position == null) {
			return data.time;
		}

		return position.position();
	}

	public void moveToEnd() {
		setNextTime(data.songChart.beatsMap.songLengthMs);
	}

	public void moveToNextBeat() {
		setNextTime(getNext(data.songChart.beatsMap.beats));
	}

	public void moveToNextGrid() {
		setNextTime(data.songChart.beatsMap.getPositionWithAddedGrid(data.time, 1));
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
}
