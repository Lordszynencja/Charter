package log.charter.gui.handlers;

import static java.lang.System.nanoTime;
import static log.charter.data.config.Config.stretchedMusicSpeed;
import static log.charter.song.notes.IPosition.findFirstAfter;
import static log.charter.sound.MusicData.generateSound;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingWorker;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.managers.ModeManager;
import log.charter.gui.CharterFrame;
import log.charter.song.notes.IPosition;
import log.charter.sound.IPlayer;
import log.charter.sound.MusicData;
import log.charter.sound.RepeatingPlayer;
import log.charter.sound.RotatingRepeatingPlayer;
import log.charter.sound.SoundPlayer;
import log.charter.sound.SoundPlayer.Player;
import log.charter.sound.StretchedFileLoader;
import log.charter.util.CollectionUtils.ArrayList2;

public class AudioHandler {
	private static class TickPlayer {
		private final IPlayer tickPlayer;
		private final Supplier<ArrayList2<? extends IPosition>> positionsSupplier;

		public boolean on = false;
		private int nextTime = -1;

		public TickPlayer(final IPlayer tickPlayer, final Supplier<ArrayList2<? extends IPosition>> positionsSupplier) {
			this.tickPlayer = tickPlayer;
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

	private ChartData data;
	private CharterFrame frame;
	private ModeManager modeManager;

	private List<Integer> speedsToProcess = new ArrayList<>();
	private MusicData slowedDownSong;
	private int loadingSpeed = 100;
	private int currentlyLoadedSpecialSpeed = loadingSpeed;

	private TickPlayer beatTickPlayer;
	private TickPlayer noteTickPlayer;
	private Player songPlayer;

	private int speed = 100;
	private int songTimeOnStart = 0;
	private long playStartTime;

	private boolean ignoreStops = false;

	public void init(final ChartData data, final CharterFrame frame, final ModeManager modeManager) {
		this.data = data;
		this.frame = frame;
		this.modeManager = modeManager;

		beatTickPlayer = new TickPlayer(new RepeatingPlayer(generateSound(4000, 0.01, 1)), //
				() -> data.songChart.beatsMap.beats);
		noteTickPlayer = new TickPlayer(new RotatingRepeatingPlayer(generateSound(1000, 0.02, 0.8), 4), //
				this::getCurrentClapPositions);
	}

	private ArrayList2<? extends IPosition> getCurrentClapPositions() {
		switch (modeManager.editMode) {
		case GUITAR:
			return data.getCurrentArrangementLevel().chordsAndNotes;
		case TEMPO_MAP:
			return data.songChart.beatsMap.beats;
		case VOCALS:
			return data.songChart.vocals.vocals;
		default:
			return new ArrayList2<>();
		}
	}

	public void toggleClaps() {
		noteTickPlayer.on = !noteTickPlayer.on;
	}

	public void toggleMetronome() {
		beatTickPlayer.on = !beatTickPlayer.on;
	}

	private void playMusic(final MusicData musicData, final int speed) {
		this.speed = speed;
		songPlayer = SoundPlayer.play(musicData, data.time * 100 / speed);
		songTimeOnStart = data.time;
		playStartTime = nanoTime() / 1_000_000L;
	}

	public void stopMusic() {
		if (ignoreStops) {
			return;
		}
		if (songPlayer != null) {
			songPlayer.stop();
			songPlayer = null;
			beatTickPlayer.stop();
			noteTickPlayer.stop();
		}
	}

	public void clear() {
		loadingSpeed = 100;
		currentlyLoadedSpecialSpeed = loadingSpeed;
		slowedDownSong = null;
		stopMusic();
	}

	public void togglePlayNormalSpeed() {
		if (data.isEmpty) {
			return;
		}
		if (songPlayer != null) {
			stopMusic();
			return;
		}

		playMusic(data.music, 100);
	}

	public void createDefaultStretches() {
		speedsToProcess = new ArrayList<>();
		if (stretchedMusicSpeed == 25) {
			speedsToProcess.add(25);
			speedsToProcess.add(50);
			speedsToProcess.add(75);
		} else if (stretchedMusicSpeed == 50) {
			speedsToProcess.add(50);
			speedsToProcess.add(25);
			speedsToProcess.add(75);
		} else if (stretchedMusicSpeed == 75) {
			speedsToProcess.add(75);
			speedsToProcess.add(50);
			speedsToProcess.add(25);
		} else {
			speedsToProcess.add(stretchedMusicSpeed);
			speedsToProcess.add(50);
			speedsToProcess.add(25);
			speedsToProcess.add(75);
		}

		new Thread(() -> {
			final List<Integer> speedsToProcess = this.speedsToProcess;
			final MusicData musicData = data.music;
			final String dir = data.path;

			while (!speedsToProcess.isEmpty()) {
				final int speedToProcess = speedsToProcess.get(0);

				final StretchedFileLoader stretchedFileLoader = new StretchedFileLoader(musicData, dir, speedToProcess);

				while (stretchedFileLoader.result == null) {
					try {
						Thread.sleep(1);
					} catch (final InterruptedException e) {
					}
				}

				speedsToProcess.remove(0);
			}
		}).start();
	}

	private void loadStretched() {
		final int speedToMake = stretchedMusicSpeed;
		loadingSpeed = speedToMake;
		slowedDownSong = null;

		StretchedFileLoader stretchedFileLoader = new StretchedFileLoader(data.music, data.path, speedToMake);

		while (stretchedFileLoader.result == null) {
			try {
				Thread.sleep(1);
			} catch (final InterruptedException e) {
			}
		}

		if (stretchedFileLoader.result.msLength() == 0) {
			return;
		}

		if (loadingSpeed == speedToMake) {
			slowedDownSong = stretchedFileLoader.result;
			currentlyLoadedSpecialSpeed = speedToMake;
			stretchedFileLoader = null;
		}
	}

	public void loadStretchedWithCheck() {
		while (speedsToProcess.contains(stretchedMusicSpeed) || loadingSpeed != currentlyLoadedSpecialSpeed) {
			try {
				Thread.sleep(1);
			} catch (final InterruptedException e) {
			}
		}

		if (currentlyLoadedSpecialSpeed == stretchedMusicSpeed) {
			return;
		}

		loadStretched();
	}

	public void togglePlaySetSpeed() {
		if (data.isEmpty) {
			return;
		}

		if (songPlayer != null) {
			stopMusic();
			return;
		}

		if (slowedDownSong != null) {
			playMusic(slowedDownSong, currentlyLoadedSpecialSpeed);
			return;
		}

		final JDialog dialog = new JDialog(frame, Label.LOADING.label());
		dialog.setLayout(null);
		dialog.setSize(300, 100);
		dialog.setLocation(frame.getWidth() / 2 - dialog.getWidth() / 2,
				frame.getHeight() / 2 - dialog.getHeight() / 2);

		final JLabel text = new JLabel(Label.LOADING.label());
		text.setHorizontalAlignment(JLabel.CENTER);
		text.setBounds(0, 30, 300, 20);
		dialog.add(text);

		dialog.setVisible(true);

		final SwingWorker<Void, Void> mySwingWorker = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				loadStretchedWithCheck();

				ignoreStops = true;
				if (slowedDownSong != null) {
					playMusic(slowedDownSong, currentlyLoadedSpecialSpeed);
				}
				dialog.dispose();
				ignoreStops = false;
				return null;
			}
		};

		mySwingWorker.execute();
	}

	public void frame() {
		if (songPlayer == null) {
			return;
		}
		if (songPlayer.isStopped()) {
			stopMusic();
		}

		final int timePassed = (int) ((nanoTime() / 1_000_000 - playStartTime) * speed / 100);
		final int nextTime = songTimeOnStart + timePassed;
		frame.setNextTime(nextTime);

		beatTickPlayer.handleFrame(nextTime);
		noteTickPlayer.handleFrame(nextTime);
	}
}
