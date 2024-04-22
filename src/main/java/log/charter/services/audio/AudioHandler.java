package log.charter.services.audio;

import static java.lang.System.nanoTime;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.gui.components.toolbar.ChartToolbar;
import log.charter.services.RepeatManager;
import log.charter.services.data.ChartTimeHandler;
import log.charter.services.data.ProjectAudioHandler;
import log.charter.sound.data.AudioDataShort;
import log.charter.sound.system.SoundSystem;
import log.charter.sound.system.SoundSystem.Player;

public class AudioHandler {
	private ChartTimeHandler chartTimeHandler;
	private ChartData chartData;
	private ChartToolbar chartToolbar;
	private ClapsHandler clapsHandler;
	private MetronomeHandler metronomeHandler;
	private MidiChartNotePlayer midiChartNotePlayer;
	private ProjectAudioHandler projectAudioHandler;
	private RepeatManager repeatManager;

	private Player songPlayer;

	private AudioDataShort lastPlayedData = null;
	private int speed = 100;
	private int songTimeOnStart = 0;
	private long playStartTime;

	private final boolean ignoreStops = false;
	public boolean midiNotesPlaying = false;

	public void toggleMidiNotes() {
		if (midiNotesPlaying) {
			if (songPlayer != null) {
				midiChartNotePlayer.stopPlaying();
			}
			midiNotesPlaying = false;
		} else {
			if (songPlayer != null) {
				midiChartNotePlayer.startPlaying(speed);
			}
			midiNotesPlaying = true;
		}

		chartToolbar.updateValues();
	}

	private void playMusic(final AudioDataShort musicData) {
		stop();

		final int start = chartTimeHandler.time();
		if (repeatManager.isRepeating() && chartTimeHandler.nextTime() > repeatManager.repeatEnd()) {
			rewind(repeatManager.repeatStart());
			return;
		}

		lastPlayedData = musicData;

		songPlayer = SoundSystem.play(lastPlayedData, () -> Config.volume, speed, start);
		songTimeOnStart = chartTimeHandler.time();
		playStartTime = nanoTime() / 1_000_000L;

		if (midiNotesPlaying) {
			midiChartNotePlayer.startPlaying(speed);
		}
	}

	private void stop() {
		if (songPlayer == null) {
			return;
		}

		songPlayer.stop();
		songPlayer = null;
		metronomeHandler.stop();
		clapsHandler.stop();

		if (midiNotesPlaying) {
			midiChartNotePlayer.stopPlaying();
		}
	}

	public void stopMusic() {
		if (ignoreStops) {
			return;
		}

		stop();
	}

	public void clear() {
		stopMusic();
	}

	public void togglePlaySetSpeed() {
		if (chartData.isEmpty) {
			return;
		}
		if (songPlayer != null) {
			stopMusic();
			return;
		}

		playMusic(projectAudioHandler.getAudio());
		return;
	}

	public void frame() {
		if (speed != Config.stretchedMusicSpeed) {
			speed = Config.stretchedMusicSpeed;

			if (isPlaying()) {
				stopMusic();
				playMusic(projectAudioHandler.getAudio());
			}
		}

		if (songPlayer == null) {
			return;
		}

		if (songPlayer.isStopped()) {
			if (repeatManager.isRepeating()) {
				final int timePassed = (int) ((nanoTime() / 1_000_000 - playStartTime) * speed / 100);
				final int nextTime = songTimeOnStart + timePassed;
				chartTimeHandler.nextTime(nextTime);
				return;
			}

			stopMusic();
		}

		final int timePassed = (int) ((nanoTime() / 1_000_000 - playStartTime) * speed / 100);
		final int nextTime = songTimeOnStart + timePassed;
		chartTimeHandler.nextTime(nextTime);

		metronomeHandler.nextTime(nextTime);
		clapsHandler.nextTime(nextTime);

		if (!repeatManager.isRepeating()) {
			midiChartNotePlayer.frame();
		}
	}

	public void rewind(final int t) {
		stop();

		chartTimeHandler.nextTime(t);

		metronomeHandler.stop();
		metronomeHandler.nextTime(t);
		clapsHandler.stop();
		clapsHandler.nextTime(t);
		if (midiNotesPlaying) {
			midiChartNotePlayer.stopPlaying();
			midiChartNotePlayer.startPlaying(speed);
		}

		playMusic(lastPlayedData);
	}

	public boolean isPlaying() {
		return songPlayer != null;
	}
}
