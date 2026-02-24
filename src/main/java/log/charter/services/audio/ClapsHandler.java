package log.charter.services.audio;

import java.util.ArrayList;
import java.util.List;

import log.charter.data.ChartData;
import log.charter.data.song.position.virtual.IVirtualConstantPosition;
import log.charter.gui.components.toolbar.ChartToolbar;
import log.charter.services.editModes.ModeManager;
import log.charter.sound.utils.AudioGenerator;

public class ClapsHandler {
	private ChartData chartData;
	private ChartToolbar chartToolbar;
	private ModeManager modeManager;

	private final TickPlayer clapsPlayer = new TickPlayer(AudioGenerator.generateSound(1000, 0.01, 1),
			this::getCurrentClapPositions, () -> chartData.beats());

	private List<? extends IVirtualConstantPosition> getCurrentClapPositions() {
		switch (modeManager.getMode()) {
			case GUITAR:
				return chartData.currentSounds();
			case TEMPO_MAP:
				return chartData.beats();
			case SHOWLIGHTS:
				return chartData.showlights();
			case VOCALS:
				return chartData.currentVocals().vocals;
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

	public void nextTime(final double nextTime) {
		clapsPlayer.nextTime(nextTime);
	}

	public void stop() {
		clapsPlayer.stop();
	}
}
