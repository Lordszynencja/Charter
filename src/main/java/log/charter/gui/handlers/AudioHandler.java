package log.charter.gui.handlers;

import static java.lang.System.nanoTime;
import static log.charter.data.config.Config.createDefaultStretchesInBackground;
import static log.charter.data.config.Config.sfxVolume;
import static log.charter.data.config.Config.stretchedMusicSpeed;
import static log.charter.gui.components.utils.ComponentUtils.showPopup;
import static log.charter.song.notes.IConstantPosition.findFirstAfter;
import static log.charter.sound.data.AudioUtils.generateSound;

import java.util.function.Supplier;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.RepeatManager;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.toolbar.ChartToolbar;
import log.charter.gui.handlers.data.ChartTimeHandler;
import log.charter.gui.handlers.data.ProjectAudioHandler;
import log.charter.gui.handlers.midiPlayer.MidiChartNotePlayer;
import log.charter.song.notes.IPosition;
import log.charter.sound.IPlayer;
import log.charter.sound.RepeatingPlayer;
import log.charter.sound.RotatingRepeatingPlayer;
import log.charter.sound.SoundPlayer;
import log.charter.sound.SoundPlayer.Player;
import log.charter.sound.data.AudioData;
import log.charter.sound.data.AudioDataShort;
import log.charter.util.CollectionUtils.ArrayList2;

public class AudioHandler {
	private static class TickPlayer {
		private final IPlayer tickPlayer;
		private final Supplier<ArrayList2<? extends IPosition>> positionsSupplier;

		public boolean on = false;
		private int nextTime = -1;

		public TickPlayer(final AudioDataShort tick,
				final Supplier<ArrayList2<? extends IPosition>> positionsSupplier) {
			this(tick, 1, positionsSupplier);
		}

		public TickPlayer(final AudioDataShort tick, final int players,
				final Supplier<ArrayList2<? extends IPosition>> positionsSupplier) {
			final Supplier<AudioData<?>> tickSupplier = () -> tick.volume(sfxVolume);
			if (players == 1) {
				tickPlayer = new RepeatingPlayer(tickSupplier);
			} else {
				tickPlayer = new RotatingRepeatingPlayer(tickSupplier, players);
			}

			this.positionsSupplier = positionsSupplier;
		}

		public void handleFrame(final int t) {
			if (nextTime != -1 && nextTime < t) {
				tickPlayer.play();
				nextTime = -1;
			}

			if (on && nextTime == -1) {
				final IPosition nextPosition = findFirstAfter(positionsSupplier.get(), t);
				if (nextPosition != null) {
					nextTime = nextPosition.position();
				}
			}
		}

		public void stop() {
			nextTime = -1;
		}
	}

	private ChartTimeHandler chartTimeHandler;
	private ChartToolbar chartToolbar;
	private ChartData data;
	private CharterFrame frame;
	private ModeManager modeManager;
	private ProjectAudioHandler projectAudioHandler;
	private RepeatManager repeatManager;

	private final StretchedAudioHandler stretchedAudioHandler = new StretchedAudioHandler();

	private AudioDataShort slowedDownSong;
	private int currentlyLoadedSpecialSpeed = 100;

	private TickPlayer beatTickPlayer;
	private TickPlayer noteTickPlayer;
	private final MidiChartNotePlayer midiChartNotePlayer = new MidiChartNotePlayer();
	private Player songPlayer;

	private AudioDataShort lastUncutData = null;
	private AudioDataShort lastPlayedData = null;
	private int speed = 100;
	private int songTimeOnStart = 0;
	private long playStartTime;

	private final boolean ignoreStops = false;
	public boolean midiNotesPlaying = false;

	public void init(final ChartTimeHandler chartTimeHandler, final ChartToolbar chartToolbar, final ChartData data,
			final CharterFrame frame, final ModeManager modeManager, final ProjectAudioHandler projectAudioHandler,
			final RepeatManager repeatManager) {
		this.chartTimeHandler = chartTimeHandler;
		this.chartToolbar = chartToolbar;
		this.data = data;
		this.frame = frame;
		this.modeManager = modeManager;
		this.projectAudioHandler = projectAudioHandler;
		this.repeatManager = repeatManager;

		beatTickPlayer = new TickPlayer(generateSound(500, 0.02, 1), () -> data.songChart.beatsMap.beats);
		noteTickPlayer = new TickPlayer(generateSound(1000, 0.01, 1), 4, this::getCurrentClapPositions);

		midiChartNotePlayer.init(chartTimeHandler, data, modeManager);

		stretchedAudioHandler.init();
	}

	private ArrayList2<? extends IPosition> getCurrentClapPositions() {
		switch (modeManager.getMode()) {
			case GUITAR:
				return data.getCurrentArrangementLevel().sounds;
			case TEMPO_MAP:
				return data.songChart.beatsMap.beats;
			case VOCALS:
				return data.songChart.vocals.vocals;
			default:
				return new ArrayList2<>();
		}
	}

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

	public void toggleClaps() {
		noteTickPlayer.on = !noteTickPlayer.on;

		chartToolbar.updateValues();
	}

	public boolean claps() {
		return noteTickPlayer.on;
	}

	public void toggleMetronome() {
		beatTickPlayer.on = !beatTickPlayer.on;

		chartToolbar.updateValues();
	}

	public boolean metronome() {
		return beatTickPlayer.on;
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
			lastPlayedData = cutMusic.volume(Config.volume);
			start = 0;
		} else {
			lastPlayedData = lastUncutData.volume(Config.volume);
			start = getSlowedMs(chartTimeHandler.time());
		}

		songPlayer = SoundPlayer.play(lastPlayedData, start);
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
		beatTickPlayer.stop();
		noteTickPlayer.stop();

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
		stretchedAudioHandler.setData(data.path, projectAudioHandler.getAudio());

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
		if (data.isEmpty) {
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
			showPopup(frame, Label.GENERATING_SLOWED_SOUND);
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
				chartTimeHandler.setNextTime(nextTime);
				return;
			}

			stopMusic();
		}

		final int timePassed = (int) ((nanoTime() / 1_000_000 - playStartTime) * speed / 100);
		final int nextTime = songTimeOnStart + timePassed;
		chartTimeHandler.setNextTime(nextTime);

		beatTickPlayer.handleFrame(nextTime);
		noteTickPlayer.handleFrame(nextTime);

		if (!repeatManager.isRepeating()) {
			midiChartNotePlayer.frame();
		}
	}

	public void rewind(final int t) {
		stop();

		chartTimeHandler.setNextTime(t);

		beatTickPlayer.stop();
		beatTickPlayer.handleFrame(t);
		noteTickPlayer.stop();
		noteTickPlayer.handleFrame(t);
		if (midiNotesPlaying) {
			midiChartNotePlayer.stopPlaying();
		}

		playMusic(lastUncutData, speed);
	}

	public boolean isPlaying() {
		return songPlayer != null;
	}
}
