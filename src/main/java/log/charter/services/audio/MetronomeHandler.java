package log.charter.services.audio;

import static log.charter.sound.data.AudioUtils.generateSound;

import log.charter.data.ChartData;
import log.charter.gui.components.toolbar.ChartToolbar;

public class MetronomeHandler {
	private ChartData chartData;
	private ChartToolbar chartToolbar;

	private final TickPlayer metronomePlayer = new TickPlayer(generateSound(500, 0.02, 1), () -> chartData.beats(),
			() -> chartData.beats());

	public boolean metronome() {
		return metronomePlayer.on;
	}

	public void toggleMetronome() {
		metronomePlayer.on = !metronomePlayer.on;

		chartToolbar.updateValues();
	}

	public void nextTime(final double nextTime) {
		metronomePlayer.nextTime(nextTime);
	}

	public void stop() {
		metronomePlayer.stop();
	}
}
