package log.charter.gui.handlers.data;

import java.io.File;

import log.charter.data.ChartData;
import log.charter.gui.chartPanelDrawers.common.waveform.WaveFormDrawer;
import log.charter.gui.handlers.files.SongFilesBackuper;
import log.charter.sound.data.AudioDataShort;
import log.charter.sound.ogg.OggWriter;

public class ProjectAudioHandler {
	private ChartData chartData;
	private WaveFormDrawer waveFormDrawer;

	private AudioDataShort audio = new AudioDataShort();

	public void importAudio(final File file) {
		final AudioDataShort musicData = AudioDataShort.readFile(file);
		if (musicData != null) {
			setAudio(musicData, true);
		}
	}

	public void setAudio(final AudioDataShort audio, final boolean save) {
		this.audio = audio;

		waveFormDrawer.recalculateMap();
		if (save) {
			final File file = new File(chartData.path, "song.ogg");
			if (file.exists()) {
				SongFilesBackuper.makeAudioBackup(file);
			}

			OggWriter.writeOgg(file, audio);
			chartData.songChart.musicFileName = file.getName();
		}
	}

	public AudioDataShort getAudio() {
		return audio;
	}
}
