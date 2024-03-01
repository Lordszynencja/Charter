package log.charter.gui.handlers.data;

import log.charter.gui.chartPanelDrawers.common.waveform.WaveFormDrawer;
import log.charter.sound.data.AudioDataShort;

public class ProjectAudioHandler {
	private WaveFormDrawer waveFormDrawer;

	private AudioDataShort audio = new AudioDataShort();

	public void init(final WaveFormDrawer waveFormDrawer) {
		this.waveFormDrawer = waveFormDrawer;
	}

	public void setAudio(final AudioDataShort audio) {
		this.audio = audio;

		waveFormDrawer.recalculateMap();
	}

	public AudioDataShort getAudio() {
		return audio;
	}
}
