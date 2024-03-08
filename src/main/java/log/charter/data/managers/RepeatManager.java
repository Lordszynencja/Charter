package log.charter.data.managers;

import log.charter.gui.components.toolbar.ChartToolbar;
import log.charter.gui.handlers.AudioHandler;
import log.charter.gui.handlers.data.ChartTimeHandler;

public class RepeatManager {
	private boolean repeatingOn = true;
	private int repeatStart = -1;
	private int repeatEnd = -1;

	private AudioHandler audioHandler;
	private ChartTimeHandler chartTimeHandler;
	private ChartToolbar chartToolbar;

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

	public void toggleRepeatStart() {
		repeatStart = (repeatStart < 0 || repeatStart != chartTimeHandler.time()) ? chartTimeHandler.time() : -1;
	}

	public int getRepeatEnd() {
		return repeatEnd;
	}

	public void toggleRepeatEnd() {
		repeatEnd = (repeatEnd < 0 || repeatEnd != chartTimeHandler.time()) ? chartTimeHandler.time() : -1;
	}

	public void frame() {
		if (!isRepeating() || !audioHandler.isPlaying()) {
			return;
		}

		if (chartTimeHandler.nextTime() >= repeatEnd) {
			audioHandler.rewind(repeatStart);
			chartTimeHandler.nextTime(repeatStart);
		}
	}

	public boolean isRepeating() {
		return repeatingOn && repeatStart >= 0 && repeatEnd >= 0 && repeatEnd > repeatStart;
	}
}
