package log.charter.services.audio;

import static java.lang.System.nanoTime;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.gui.components.toolbar.ChartToolbar;
import log.charter.io.Logger;
import log.charter.services.RepeatManager;
import log.charter.services.data.ChartTimeHandler;
import log.charter.services.data.ProjectAudioHandler;
import log.charter.sound.data.AudioData;
import log.charter.sound.effects.Effect;
import log.charter.sound.system.SoundSystem;
import log.charter.sound.system.data.Player;

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
	public boolean lowPassFilterEnabled = false;
	public boolean bandPassFilterEnabled = false;
	public boolean highPassFilterEnabled = false;
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
		lowPassFilterEnabled = !lowPassFilterEnabled;
		highPassFilterEnabled = false;
		bandPassFilterEnabled = false;

		chartToolbar.updateValues();
	}

	public void toggleBandPassFilter() {
		lowPassFilterEnabled = false;
		highPassFilterEnabled = false;
		bandPassFilterEnabled = !bandPassFilterEnabled;

		chartToolbar.updateValues();
	}

	public void toggleHighPassFilter() {
		lowPassFilterEnabled = false;
		highPassFilterEnabled = !highPassFilterEnabled;
		bandPassFilterEnabled = false;

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

			songPlayer = SoundSystem.play(lastPlayedData, () -> projectAudioHandler.getVolume(), speed, start, effect);
		} catch (final Exception | UnsatisfiedLinkError e) {
			Logger.error("Couldn't play sound", e);
		}
		songTimeOnStart = chartTimeHandler.time();
		playStartTime = nanoTime() / 1_000_000L;

		if (midiNotesPlaying) {
			midiChartNotePlayer.startPlaying(speed);
		}
		chartToolbar.setPlayButtonIcon();
	}

	private Effect createEffect(final int channels, final int sampleRate) {
		return new Effect() {
			private final Effect lowPassFilter = Config.passFilters.createLowPassFilter(channels, sampleRate);
			private final Effect bandPassFilter = Config.passFilters.createBandPassFilter(channels, sampleRate);
			private final Effect highPassFilter = Config.passFilters.createHighPassFilter(channels, sampleRate);

			@Override
			public float apply(final int channel, final float sample) {
				if (lowPassFilterEnabled) {
					return lowPassFilter.apply(channel, sample);
				}
				if (bandPassFilterEnabled) {
					return bandPassFilter.apply(channel, sample);
				}
				if (highPassFilterEnabled) {
					return highPassFilter.apply(channel, sample);
				}

				return sample;
			}
		};
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

		chartToolbar.setPlayButtonIcon();
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
