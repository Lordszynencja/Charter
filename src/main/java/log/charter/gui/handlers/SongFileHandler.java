package log.charter.gui.handlers;

import static log.charter.io.Logger.debug;
import static log.charter.io.Logger.error;
import static log.charter.io.rs.xml.vocals.VocalsXStreamHandler.saveVocals;
import static log.charter.io.rsc.xml.RocksmithChartProjectXStreamHandler.readProject;
import static log.charter.io.rsc.xml.RocksmithChartProjectXStreamHandler.saveProject;
import static log.charter.song.SongChart.vocalsFileName;
import static log.charter.util.FileChooseUtils.chooseFile;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import helliker.id3.MP3File;
import log.charter.data.ArrangementFixer;
import log.charter.data.ArrangementValidator;
import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.modes.EditMode;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.SongFolderSelectPane;
import log.charter.gui.menuHandlers.CharterMenuBar;
import log.charter.io.Logger;
import log.charter.io.rs.xml.song.SongArrangement;
import log.charter.io.rs.xml.song.SongArrangementXStreamHandler;
import log.charter.io.rs.xml.vocals.ArrangementVocals;
import log.charter.io.rs.xml.vocals.VocalsXStreamHandler;
import log.charter.io.rsc.xml.RocksmithChartProject;
import log.charter.song.ArrangementChart;
import log.charter.song.SongChart;
import log.charter.song.vocals.Vocals;
import log.charter.sound.MusicData;
import log.charter.util.FileChooseUtils;
import log.charter.util.RW;

public class SongFileHandler {
	private static class LoadingDialog extends JDialog {
		private static final long serialVersionUID = 1L;

		private final JLabel text;
		private final JProgressBar progressBar;

		public LoadingDialog(final CharterFrame frame, final int steps) {
			super(frame, Label.LOADING.label());
			setLayout(null);
			setSize(300, 200);
			setLocation(frame.getX() + frame.getWidth() / 2 - getWidth() / 2,
					frame.getY() + frame.getHeight() / 2 - getHeight() / 2);

			text = new JLabel(Label.LOADING.label());
			text.setHorizontalAlignment(JLabel.CENTER);
			text.setBounds(0, 30, 300, 20);
			add(text);

			progressBar = new JProgressBar(0, steps);
			progressBar.setBounds(50, 100, getWidth() - 100, 30);
			add(progressBar);

			setVisible(true);
		}

		public void setProgress(final int progress, final String description) {
			progressBar.setValue(progress);
			text.setText(description);
		}
	}

	private static Map<String, String> extractNewSongData(final String path) {
		final Map<String, String> data = new HashMap<>();
		try {
			final MP3File mp3File = new MP3File(path);

			try {
				data.put("artist", mp3File.getArtist());
			} catch (final Exception e) {
				data.put("artist", "");
				debug("Couldn't get artist from mp3 tags data", e);
			}
			try {
				data.put("title", mp3File.getTitle());
			} catch (final Exception e) {
				data.put("title", "");
				debug("Couldn't get title from mp3 tags data", e);
			}
			try {
				data.put("album", mp3File.getAlbum());
			} catch (final Exception e) {
				data.put("album", "");
				debug("Couldn't get album from mp3 tags data", e);
			}
			try {
				data.put("year", mp3File.getYear());
			} catch (final Exception e) {
				data.put("year", "");
				debug("Couldn't get year from mp3 tags data", e);
			}
		} catch (final Exception e) {
			debug("Couldn't get mp3 tags data", e);
		}

		return data;
	}

	public static void makeDefaultBackups(final ChartData data) {
		if (data.isEmpty) {
			return;
		}

		final List<String> filesToBackup = new ArrayList<>();
		filesToBackup.add(data.projectFileName);
		for (int i = 0; i < data.songChart.arrangements.size(); i++) {
			filesToBackup.add(data.songChart.arrangements.get(i).getFileName(i + 1));
		}
		filesToBackup.add(vocalsFileName);
		System.out.println("Doing backup of " + data.path + ", files: " + filesToBackup);

		makeBackups(data.path, filesToBackup);
	}

	private static void makeBackups(final String dir, final List<String> fileNames) {
		final String backupFolderName = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date());
		final File backupFolder = new File(new File(new File(dir), "backups"), backupFolderName);
		backupFolder.mkdirs();

		for (final String fileName : fileNames) {
			final File f = new File(dir, fileName);
			if (f.exists()) {
				RW.writeB(new File(backupFolder, fileName), RW.readB(f));
			}
		}
	}

	private ArrangementFixer arrangementFixer;
	private ArrangementValidator arrangementValidator;
	private AudioHandler audioHandler;
	private ChartData data;
	private CharterFrame frame;
	private CharterMenuBar charterMenuBar;
	private ModeManager modeManager;
	private UndoSystem undoSystem;

	public void init(final ArrangementFixer arrangementFixer, final ArrangementValidator arrangementValidator,
			final AudioHandler audioHandler, final ChartData data, final CharterFrame frame,
			final CharterMenuBar charterMenuBar, final ModeManager modeManager, final UndoSystem undoSystem) {
		this.arrangementFixer = arrangementFixer;
		this.arrangementValidator = arrangementValidator;
		this.audioHandler = audioHandler;
		this.data = data;
		this.frame = frame;
		this.charterMenuBar = charterMenuBar;
		this.modeManager = modeManager;
		this.undoSystem = undoSystem;

		new Thread(() -> {
			while (true) {
				try {
					Thread.sleep(Config.backupDelay * 1000);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}

				makeDefaultBackups(data);
			}
		}).start();
	}

	private void doWithLoadingDialog(final int steps, final Consumer<LoadingDialog> operation) {
		final LoadingDialog dialog = new LoadingDialog(frame, steps);

		final SwingWorker<Void, Void> mySwingWorker = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				try {
					operation.accept(dialog);
				} catch (final Exception e) {
					Logger.error("error when loading", e);
					dialog.dispose();
				}

				dialog.dispose();
				return null;
			}
		};

		mySwingWorker.execute();
	}

	public void newSong() {
		if (!frame.checkChanged()) {
			return;
		}

		final File songFile = FileChooseUtils.chooseMusicFile(frame, Config.musicPath);
		if (songFile == null) {
			return;
		}

		final String songName = songFile.getName();
		final int dotIndex = songName.lastIndexOf('.');
		final String extension = songName.substring(dotIndex + 1).toLowerCase();
		if (!extension.equals("mp3") && !extension.equals("ogg")) {
			frame.showPopup(Label.NOT_MP3_OGG.label());
			return;
		}

		final Map<String, String> songData = extractNewSongData(songFile.getAbsolutePath());

		final String artist = songData.get("artist");
		final String title = songData.get("title");

		final String defaultFolderName = artist.isBlank()//
				? title.isBlank() //
						? songName.substring(0, songName.lastIndexOf('.'))//
						: "unknown artist - " + title
				: title.isBlank() //
						? artist + " - unknown title"//
						: artist + " - " + title;

		final File songFolder = chooseSongFolder(songFile.getParent(), defaultFolderName);
		if (songFolder == null) {
			return;
		}

		final String musicFileName = "guitar." + extension;
		final File musicFile = new File(songFolder, musicFileName);
		RW.writeB(musicFile, RW.readB(songFile));

		final MusicData musicData = MusicData.readFile(musicFile);
		if (musicData == null) {
			frame.showPopup(Label.MUSIC_DATA_NOT_FOUND.label());
			return;
		}

		final SongChart songChart = new SongChart(musicData.msLength(), musicFileName);
		songChart.artistName = songData.getOrDefault("artist", "");
		songChart.title = songData.getOrDefault("title", "");
		songChart.albumName = songData.getOrDefault("album", "");
		try {
			songChart.albumYear = Integer.valueOf(songData.getOrDefault("year", ""));
		} catch (final NumberFormatException e) {
			songChart.albumYear = null;
		}

		data.setNewSong(songFolder, songChart, musicData, "project.rscp");
		save();

		audioHandler.clear();
		audioHandler.setSong();
	}

	public MusicData chooseMusicFile(final String startingDir) {
		final File musicFile = FileChooseUtils.chooseMusicFile(frame, startingDir);
		if (musicFile == null) {
			return null;
		}

		return MusicData.readFile(musicFile);
	}

	public File chooseSongFolder(final String audioFileDirectory, final String defaultFolderName) {
		final SongFolderSelectPane songFolderSelectPane = new SongFolderSelectPane(frame, Config.songsPath,
				audioFileDirectory, defaultFolderName);

		if (songFolderSelectPane.isAudioFolderChosen()) {
			return new File(audioFileDirectory);
		}

		String folderName = songFolderSelectPane.getFolderName();
		if (folderName == null || folderName.isBlank()) {
			return null;
		}

		File songFolder = new File(Config.songsPath, folderName);
		while (songFolder.exists()) {
			folderName = frame.showInputDialog(Label.FOLDER_EXISTS_CHOOSE_DIFFERENT.label(), folderName);
			if (folderName == null) {
				return null;
			}

			songFolder = new File(Config.songsPath, folderName);
		}
		songFolder.mkdir();

		return songFolder;
	}

	private void openInternal(final LoadingDialog loadingDialog, final String path) {
		loadingDialog.setProgress(0, Label.LOADING_PROJECT_FILE.label());

		final File projectFileChosen = new File(path);
		final String dir = projectFileChosen.getParent() + File.separator;
		final String name = projectFileChosen.getName().toLowerCase();

		if (!name.endsWith(".rscp")) {
			frame.showPopup(Label.UNSUPPORTED_FILE_TYPE.label());
			error("unsupported file: " + projectFileChosen.getName());
			return;
		}
		final RocksmithChartProject project;
		try {
			project = readProject(RW.read(projectFileChosen));
			if (project.chartFormatVersion > 2) {
				frame.showPopup(Label.PROJECT_IS_NEWER_VERSION.label());
				return;
			}
		} catch (final Exception e) {
			frame.showPopup(String.format(Label.MISSING_ARRANGEMENT_FILE.label(), projectFileChosen.getAbsolutePath()));
			return;
		}
		loadingDialog.setProgress(1, Label.LOADING_MUSIC_FILE.label());

		final MusicData musicData = MusicData.readFile(new File(dir, project.musicFileName));
		if (musicData == null) {
			frame.showPopup(Label.WRONG_MUSIC_FILE.label());
			return;
		}

		loadingDialog.setProgress(2, Label.LOADING_ARRANGEMENTS.label());
		final SongChart songChart;
		try {
			songChart = new SongChart(musicData.msLength(), project, dir);
		} catch (final Exception e) {
			frame.showPopup(e.getMessage());
			return;
		}

		final List<String> filesToBackup = new ArrayList<>();
		filesToBackup.add(projectFileChosen.getName());
		filesToBackup.addAll(project.arrangementFiles);
		filesToBackup.add(vocalsFileName);
		makeBackups(dir, filesToBackup);

		data.setSong(dir, songChart, musicData, projectFileChosen.getName(), project.editMode, project.arrangement,
				project.level, project.time);

		loadingDialog.setProgress(3, Label.LOADING_DONE.label());
	}

	public void open(final String path) {
		doWithLoadingDialog(3, (dialog) -> openInternal(dialog, path));
	}

	public void open() {
		if (!frame.checkChanged()) {
			return;
		}

		String startingDir = data.path;
		if (startingDir.isEmpty() || !new File(startingDir).exists()) {
			startingDir = Config.songsPath;
		}

		final File projectFileChosen = chooseFile(frame, startingDir, new String[] { ".rscp" },
				new String[] { Label.ROCKSMITH_CHART_PROJECT.label() });
		if (projectFileChosen == null) {
			return;
		}

		open(projectFileChosen.getAbsolutePath());
	}

	public void openAudioFile() {
		final File musicFile = FileChooseUtils.chooseMusicFile(frame, data.path);
		if (musicFile == null) {
			return;
		}

		final MusicData musicData = MusicData.readFile(musicFile);
		if (musicData != null) {
			data.music = musicData;
			data.songChart.musicFileName = musicFile.getName();
		}
	}

	public void openSongWithImportFromArrangementXML() {
		final File songFile = FileChooseUtils.chooseMusicFile(frame, Config.songsPath);
		if (songFile == null) {
			return;
		}

		final String songName = songFile.getName();
		final int dotIndex = songName.lastIndexOf('.');
		final String extension = songName.substring(dotIndex + 1).toLowerCase();
		if (!extension.equals("mp3") && !extension.equals("ogg")) {
			frame.showPopup(Label.NOT_MP3_OGG.label());
			return;
		}

		LoadingDialog loadingDialog = new LoadingDialog(frame, 1);
		loadingDialog.setProgress(0, Label.LOADING_MUSIC_FILE.label());
		final MusicData musicData = MusicData.readFile(songFile);
		if (musicData == null) {
			loadingDialog.dispose();
			frame.showPopup(Label.MUSIC_FILE_COULDNT_BE_LOADED.label());
			return;
		}
		loadingDialog.dispose();

		final String dir = songFile.getParent() + File.separator;
		final File arrangementFile = FileChooseUtils.chooseFile(frame, dir, new String[] { ".xml" },
				new String[] { Label.RS_ARRANGEMENT_FILE.label() });
		if (arrangementFile == null) {
			return;
		}

		loadingDialog = new LoadingDialog(frame, 1);
		loadingDialog.setProgress(0, Label.LOADING_ARRANGEMENTS.label());
		final SongArrangement songArrangement = SongArrangementXStreamHandler.readSong(RW.read(arrangementFile));
		final SongChart songChart = new SongChart(musicData.msLength(), songName, songArrangement);

		data.setSong(dir, songChart, musicData, "project.rscp", EditMode.GUITAR, 0, 0, 0);
		loadingDialog.dispose();

		save();
	}

	public void importRSArrangementXML() {
		final String dir = data.isEmpty ? Config.songsPath : data.path;
		final File arrangementFile = FileChooseUtils.chooseFile(frame, dir, new String[] { ".xml" },
				new String[] { Label.RS_ARRANGEMENT_FILE.label() });
		if (arrangementFile == null) {
			return;
		}

		try {
			final SongArrangement songArrangement = SongArrangementXStreamHandler.readSong(RW.read(arrangementFile));
			final ArrangementChart arrangementChart = new ArrangementChart(songArrangement,
					data.songChart.beatsMap.beats);
			data.songChart.arrangements.add(arrangementChart);

			modeManager.editMode = EditMode.GUITAR;
			data.currentArrangement = data.songChart.arrangements.size() - 1;
			save();

			charterMenuBar.refreshMenus();
			frame.updateEditAreaSizes();
		} catch (final Exception e) {
			Logger.error("Couldn't load arrangement", e);
			frame.showPopup(Label.COULDNT_LOAD_ARRANGEMENT.label() + ":\n" + e.getMessage());
		}
	}

	public void importRSVocalsArrangementXML() {
		final String dir = data.isEmpty ? Config.songsPath : data.path;
		final File arrangementFile = FileChooseUtils.chooseFile(frame, dir, new String[] { ".xml" },
				new String[] { Label.RS_ARRANGEMENT_FILE.label() });
		if (arrangementFile == null) {
			return;
		}

		try {
			final ArrangementVocals vocals = VocalsXStreamHandler.readVocals(RW.read(arrangementFile));
			data.songChart.vocals = new Vocals(vocals);
			save();
		} catch (final Exception e) {
			Logger.error("Couldn't load arrangement", e);
			frame.showPopup(Label.COULDNT_LOAD_ARRANGEMENT.label() + ":\n" + e.getMessage());
		}
	}

	public void save() {
		if (data.isEmpty) {
			return;
		}

		arrangementFixer.fixArrangement();
		if (!arrangementValidator.validate()) {
			return;
		}

		final RocksmithChartProject project = new RocksmithChartProject(modeManager, data, data.songChart);

		int id = 1;
		for (final ArrangementChart arrangementChart : data.songChart.arrangements) {
			final String arrangementFileName = arrangementChart.getFileName(id);
			project.arrangementFiles.add(arrangementFileName);
			final SongArrangement songArrangement = new SongArrangement(data.songChart, arrangementChart);
			RW.write(new File(data.path, arrangementFileName), SongArrangementXStreamHandler.saveSong(songArrangement));
			id++;
		}

		if (!data.songChart.vocals.vocals.isEmpty()) {
			RW.write(new File(data.path, vocalsFileName), saveVocals(new ArrangementVocals(data.songChart.vocals)),
					"UTF-8");
		}

		RW.write(new File(data.path, data.projectFileName), saveProject(project));
		undoSystem.onSave();

		Config.markChanged();
	}

	public void saveAs() {
		if (data.isEmpty) {
			return;
		}

		arrangementFixer.fixArrangement();
		if (!arrangementValidator.validate()) {
			return;
		}

		final File newDir = FileChooseUtils.chooseDirectory(frame, data.path);
		if (newDir == null) {
			return;
		}

		data.path = newDir.getAbsolutePath();
		Config.lastDir = data.path;
		Config.lastPath = new File(data.path, data.projectFileName).getAbsolutePath();
		Config.markChanged();

		save();
		Config.save();
	}
}
