package log.charter.services.audio;

import static java.lang.System.nanoTime;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.gui.components.toolbar.ChartToolbar;
import log.charter.io.Logger;
import log.charter.services.RepeatManager;
import log.charter.services.data.ChartTimeHandler;
import log.charter.services.data.ProjectAudioHandler;
import log.charter.sound.PassFilter.PassType;
import log.charter.sound.data.AudioData;
import log.charter.sound.effects.Effect;
import log.charter.sound.effects.TestFilter;
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

	private AudioData lastPlayedData = null;
	private int speed = 100;
	public boolean lowPassFilter = false;
	private double songTimeOnStart = 0;
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

	public void toggleLowPassFilter() {
		lowPassFilter = !lowPassFilter;

		chartToolbar.updateValues();
	}

	private void playMusic(final AudioData musicData) {
		stop();

		final double start = chartTimeHandler.time();
		if (repeatManager.isRepeating() && chartTimeHandler.nextTime() > repeatManager.repeatEnd()) {
			rewind(repeatManager.repeatStart());
			return;
		}

		lastPlayedData = musicData;

		try {
			final Effect effect = createEffect(lastPlayedData.playingFormat.getChannels(),
					(int) lastPlayedData.playingFormat.getSampleRate());

			songPlayer = SoundSystem.play(lastPlayedData, () -> Config.volume, speed, start, effect);
		} catch (final Exception | UnsatisfiedLinkError e) {
			Logger.error("Couldn't play sound", e);
		}
		songTimeOnStart = chartTimeHandler.time();
		playStartTime = nanoTime() / 1_000_000L;

		if (midiNotesPlaying) {
			midiChartNotePlayer.startPlaying(speed);
		}
	}

	private Effect createEffect(final int channels, final int sampleRate) {
		if (lowPassFilter) {
			return createLowPassEffect(channels, sampleRate);
		}

		return Effect.emptyEffect;
	}

	private Effect createLowPassEffect(final int channels, final int sampleRate) {
		return new TestFilter(channels, sampleRate, 1000, PassType.Lowpass);
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
			chartToolbar.setPlayButtonIcon();
			return;
		}

		playMusic(projectAudioHandler.getAudio());
		chartToolbar.setPlayButtonIcon();
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
				final double timePassed = (nanoTime() / 1_000_000.0 - playStartTime) * speed / 100;
				final double nextTime = songTimeOnStart + timePassed;
				chartTimeHandler.nextTime(nextTime);
				return;
			}

			stopMusic();
		}

		final double timePassed = (nanoTime() / 1_000_000.0 - playStartTime) * speed / 100;
		final double nextTime = songTimeOnStart + timePassed;
		chartTimeHandler.nextTime(nextTime);

		metronomeHandler.nextTime(nextTime);
		clapsHandler.nextTime(nextTime);

		if (!repeatManager.isRepeating()) {
			midiChartNotePlayer.frame();
		}
	}

	public void rewind(final double t) {
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
