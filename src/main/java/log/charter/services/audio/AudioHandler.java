package log.charter.services.audio;

import static java.lang.System.nanoTime;
import static log.charter.data.config.Config.createDefaultStretchesInBackground;
import static log.charter.data.config.Config.stretchedMusicSpeed;
import static log.charter.gui.components.utils.ComponentUtils.showPopup;
import static log.charter.sound.StretchedFileLoader.loadStretchedAudio;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.toolbar.ChartToolbar;
import log.charter.services.RepeatManager;
import log.charter.services.data.ChartTimeHandler;
import log.charter.services.data.ProjectAudioHandler;
import log.charter.sound.StretchedFileLoader;
import log.charter.sound.data.AudioDataShort;
import log.charter.sound.system.SoundSystem;
import log.charter.sound.system.SoundSystem.Player;

public class AudioHandler {
	private ChartTimeHandler chartTimeHandler;
	private ChartData chartData;
	private CharterFrame charterFrame;
	private ChartToolbar chartToolbar;
	private ClapsHandler clapsHandler;
	private MetronomeHandler metronomeHandler;
	private MidiChartNotePlayer midiChartNotePlayer;
	private ProjectAudioHandler projectAudioHandler;
	private RepeatManager repeatManager;

	private AudioDataShort slowedDownSong;
	private int currentlyLoadedSpecialSpeed = 100;

	private Player songPlayer;

	private AudioDataShort lastUncutData = null;
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

	private int getSlowedMs(final int t) {
		return t * 100 / speed;
	}

	private void playMusic(final AudioDataShort musicData, final int speed) {
		stop();

		lastUncutData = musicData;
		this.speed = speed;

		int start;
		if (repeatManager.isRepeating()) {
			if (chartTimeHandler.nextTime() > repeatManager.repeatEnd()) {
				rewind(repeatManager.repeatStart());
				return;
			}

			final double cutStart = getSlowedMs(chartTimeHandler.time()) / 1000.0;
			final double cutEnd = getSlowedMs(repeatManager.repeatEnd()) / 1000.0;
			final AudioDataShort cutMusic = lastUncutData.cut(cutStart, cutEnd);
			lastPlayedData = cutMusic;
			start = 0;
		} else {
			lastPlayedData = lastUncutData;
			start = getSlowedMs(chartTimeHandler.time());
		}

		songPlayer = SoundSystem.playMusic(lastPlayedData, () -> Config.volume, start);
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
		currentlyLoadedSpecialSpeed = 100;
		slowedDownSong = null;
		stopMusic();
	}

	public void audioChanged() {
		StretchedFileLoader.clear();

		if (createDefaultStretchesInBackground) {
			addSpeedToStretch(stretchedMusicSpeed);
			addSpeedToStretch(50);
			addSpeedToStretch(25);
			addSpeedToStretch(75);
		}
	}

	public void addSpeedToStretch(final int speed) {
		if (speed == 100) {
			return;
		}

		loadStretchedAudio(projectAudioHandler.getAudio(), chartData.path, chartData.songChart.musicFileName, speed);
	}

	public void addSpeedToStretch() {
		addSpeedToStretch(stretchedMusicSpeed);
	}

	public void togglePlaySetSpeed() {
		if (chartData.isEmpty) {
			return;
		}
		if (songPlayer != null) {
			stopMusic();
			chartToolbar.setPlayButtonIcon();
			return;
		}

		if (stretchedMusicSpeed == 100) {
			playMusic(projectAudioHandler.getAudio(), 100);
			chartToolbar.setPlayButtonIcon();
			return;
		}
		if (currentlyLoadedSpecialSpeed == stretchedMusicSpeed && slowedDownSong != null) {
			playMusic(slowedDownSong, currentlyLoadedSpecialSpeed);
			chartToolbar.setPlayButtonIcon();
			return;
		}

		currentlyLoadedSpecialSpeed = stretchedMusicSpeed;
		slowedDownSong = loadStretchedAudio(projectAudioHandler.getAudio(), chartData.path,
				chartData.songChart.musicFileName, stretchedMusicSpeed);
		if (slowedDownSong == null) {
			showPopup(charterFrame, Label.GENERATING_SLOWED_SOUND);
			return;
		}

		playMusic(slowedDownSong, currentlyLoadedSpecialSpeed);
		chartToolbar.setPlayButtonIcon();
	}

	public void frame() {
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
		}

		playMusic(lastUncutData, speed);
	}

	public boolean isPlaying() {
		return songPlayer != null;
	}
}
