package log.charter.services;

import log.charter.gui.components.toolbar.ChartToolbar;
import log.charter.services.audio.AudioHandler;
import log.charter.services.data.ChartTimeHandler;
import log.charter.util.collections.Pair;

public class RepeatManager {
	private boolean repeatingOn = true;
	private Double repeatStart = null;
	private Double repeatEnd = null;

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

	public Double repeatStart() {
		return repeatStart;
	}

	public void toggleRepeatStart() {
		repeatStart = (repeatStart == null || repeatStart.intValue() != ((int) chartTimeHandler.time()))
				? chartTimeHandler.time()
				: null;
	}

	public Double repeatEnd() {
		return repeatEnd;
	}

	public void toggleRepeatEnd() {
		repeatEnd = (repeatEnd == null || repeatEnd.intValue() != ((int) chartTimeHandler.time()))
				? chartTimeHandler.time()
				: null;
	}

	public Pair<Double, Double> repeatSpan() {
		return new Pair<>(repeatStart, repeatEnd);
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
		return repeatingOn && repeatStart != null && repeatEnd != null && repeatEnd > repeatStart;
	}
}
