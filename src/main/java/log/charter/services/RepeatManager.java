package log.charter.services;

import log.charter.gui.components.toolbar.ChartToolbar;
import log.charter.services.audio.AudioHandler;
import log.charter.services.data.ChartTimeHandler;
import log.charter.util.collections.Pair;

public class RepeatManager {
	private boolean repeatingOn = false;
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

	private Double toggleTime(final Double time) {
		return (time == null || time.intValue() != ((int) chartTimeHandler.time())) ? chartTimeHandler.time() : null;
	}

	private void setRepeatingIfStartEndSet() {
		repeatingOn = repeatStart != null && repeatEnd != null;
		chartToolbar.updateValues();
	}

	public Double repeatStart() {
		return repeatStart;
	}

	public void toggleRepeatStart() {
		repeatStart = toggleTime(repeatStart);
		setRepeatingIfStartEndSet();
	}

	public Double repeatEnd() {
		return repeatEnd;
	}

	public void toggleRepeatEnd() {
		repeatEnd = toggleTime(repeatEnd);
		setRepeatingIfStartEndSet();
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
