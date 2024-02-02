package log.charter.data.managers;

import log.charter.data.ChartData;
import log.charter.gui.components.toolbar.ChartToolbar;
import log.charter.gui.handlers.AudioHandler;

public class RepeatManager {
	private boolean repeatingOn = true;
	private int repeatStart = -1;
	private int repeatEnd = -1;

	private AudioHandler audioHandler;
	private ChartToolbar chartToolbar;
	private ChartData data;

	public void init(final AudioHandler audioHandler, final ChartToolbar chartToolbar, final ChartData data) {
		this.audioHandler = audioHandler;
		this.chartToolbar = chartToolbar;
		this.data = data;
	}

	public boolean isOn() {
		return repeatingOn;
	}

	public void toggle() {
		repeatingOn = !repeatingOn;

		chartToolbar.updateValues();
	}

	public int getRepeatStart() {
		return repeatStart;
	}

	public void toggleRepeatStart(final int t) {
		repeatStart = (repeatStart < 0 || repeatStart != t) ? t : -1;
	}

	public int getRepeatEnd() {
		return repeatEnd;
	}

	public void toggleRepeatEnd(final int t) {
		repeatEnd = (repeatEnd < 0 || repeatEnd != t) ? t : -1;
	}

	public void frame() {
		if (!isRepeating() || !audioHandler.isPlaying()) {
			return;
		}

		if (data.nextTime >= repeatEnd) {
			audioHandler.rewind(repeatStart);
			data.setNextTime(repeatStart);
		}
	}

	public boolean isRepeating() {
		return repeatingOn && repeatStart >= 0 && repeatEnd >= 0 && repeatEnd > repeatStart;
	}

}
