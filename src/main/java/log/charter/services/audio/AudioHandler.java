package log.charter.services.audio;

import static java.lang.System.nanoTime;
import static log.charter.data.config.Config.createDefaultStretchesInBackground;
import static log.charter.data.config.Config.stretchedMusicSpeed;
import static log.charter.gui.components.utils.ComponentUtils.showPopup;

import javax.sound.sampled.LineUnavailableException;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.toolbar.ChartToolbar;
import log.charter.gui.components.utils.ComponentUtils;
import log.charter.io.Logger;
import log.charter.services.RepeatManager;
import log.charter.services.data.ChartTimeHandler;
import log.charter.services.data.ProjectAudioHandler;
import log.charter.sound.SoundPlayer.Player;
import log.charter.sound.data.AudioDataShort;

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
	private StretchedAudioHandler stretchedAudioHandler;

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
			if (chartTimeHandler.nextTime() > repeatManager.getRepeatEnd()) {
				rewind(repeatManager.getRepeatStart());
				return;
			}

			final double cutStart = getSlowedMs(chartTimeHandler.time()) / 1000.0;
			final double cutEnd = getSlowedMs(repeatManager.getRepeatEnd()) / 1000.0;
			final AudioDataShort cutMusic = lastUncutData.cut(cutStart, cutEnd);
			lastPlayedData = cutMusic;
			start = 0;
		} else {
			lastPlayedData = lastUncutData;
			start = getSlowedMs(chartTimeHandler.time());
		}

		try {
			songPlayer = new Player(lastPlayedData, () -> Config.volume).start(start);
		} catch (final LineUnavailableException e) {
			ComponentUtils.showPopup(charterFrame, "No available lines");
			Logger.error("No available lines", e);
			return;
		}
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

	public void setSong() {
		stretchedAudioHandler.clear();
		stretchedAudioHandler.setData(chartData.path, projectAudioHandler.getAudio());

		if (createDefaultStretchesInBackground) {
			stretchedAudioHandler.addSpeedToGenerate(stretchedMusicSpeed);
			stretchedAudioHandler.addSpeedToGenerate(50);
			stretchedAudioHandler.addSpeedToGenerate(25);
			stretchedAudioHandler.addSpeedToGenerate(75);
		}
	}

	public void addSpeedToStretch() {
		stretchedAudioHandler.addSpeedToGenerate(stretchedMusicSpeed);
	}

	public void togglePlaySetSpeed() {
		if (chartData.isEmpty) {
			return;
		}

		if (songPlayer != null) {
			stopMusic();
			return;
		}

		if (stretchedMusicSpeed == 100) {
			playMusic(projectAudioHandler.getAudio(), 100);
			return;
		}

		if (currentlyLoadedSpecialSpeed == stretchedMusicSpeed && slowedDownSong != null) {
			playMusic(slowedDownSong, currentlyLoadedSpecialSpeed);
			return;
		}

		currentlyLoadedSpecialSpeed = stretchedMusicSpeed;
		slowedDownSong = stretchedAudioHandler.get(stretchedMusicSpeed);
		if (slowedDownSong != null) {
			playMusic(slowedDownSong, currentlyLoadedSpecialSpeed);
		} else {
			stretchedAudioHandler.addSpeedToGenerate(currentlyLoadedSpecialSpeed);
			showPopup(charterFrame, Label.GENERATING_SLOWED_SOUND);
		}
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
