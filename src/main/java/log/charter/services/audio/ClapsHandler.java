package log.charter.services.audio;

import static log.charter.sound.data.AudioUtils.generateSound;

import java.util.ArrayList;
import java.util.List;

import log.charter.data.ChartData;
import log.charter.data.song.position.IPosition;
import log.charter.gui.components.toolbar.ChartToolbar;
import log.charter.services.editModes.ModeManager;

public class ClapsHandler {
	private ChartData chartData;
	private ChartToolbar chartToolbar;
	private ModeManager modeManager;

	private final TickPlayer clapsPlayer = new TickPlayer(generateSound(1000, 0.01, 1), 4,
			this::getCurrentClapPositions);

	private List<? extends IPosition> getCurrentClapPositions() {
		switch (modeManager.getMode()) {
			case GUITAR:
				return chartData.currentArrangementLevel().sounds;
			case TEMPO_MAP:
				return chartData.songChart.beatsMap.beats;
			case VOCALS:
				return chartData.songChart.vocals.vocals;
			default:
				return new ArrayList<>();
		}
	}

	public boolean claps() {
		return clapsPlayer.on;
	}

	public void toggleClaps() {
		clapsPlayer.on = !clapsPlayer.on;

		chartToolbar.updateValues();
	}

	public void nextTime(final int nextTime) {
		clapsPlayer.nextTime(nextTime);
	}

	public void stop() {
		clapsPlayer.stop();
	}
}
