package log.charter.gui.handlers;

import static log.charter.gui.components.utils.ComponentUtils.askForInput;
import static log.charter.gui.components.utils.ComponentUtils.askYesNoCancel;
import static log.charter.gui.components.utils.ComponentUtils.showPopup;
import static log.charter.io.Logger.debug;
import static log.charter.io.Logger.error;
import static log.charter.io.rs.xml.vocals.VocalsXStreamHandler.saveVocals;
import static log.charter.io.rsc.xml.ChartProjectXStreamHandler.readChartProject;
import static log.charter.io.rsc.xml.ChartProjectXStreamHandler.writeChartProject;
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
import log.charter.data.managers.CharterContext.Initiable;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.modes.EditMode;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.containers.SongFolderSelectPane;
import log.charter.gui.components.tabs.TextTab;
import log.charter.gui.components.utils.ComponentUtils.ConfirmAnswer;
import log.charter.gui.handlers.data.ChartTimeHandler;
import log.charter.gui.handlers.data.ProjectAudioHandler;
import log.charter.gui.menuHandlers.CharterMenuBar;
import log.charter.io.Logger;
import log.charter.io.rs.xml.RSXMLToArrangement;
import log.charter.io.rs.xml.RSXMLToSongChart;
import log.charter.io.rs.xml.song.SongArrangement;
import log.charter.io.rs.xml.song.SongArrangementXStreamHandler;
import log.charter.io.rs.xml.vocals.ArrangementVocals;
import log.charter.io.rs.xml.vocals.VocalsXStreamHandler;
import log.charter.io.rsc.xml.ChartProject;
import log.charter.song.Arrangement;
import log.charter.song.SongChart;
import log.charter.song.vocals.Vocals;
import log.charter.sound.data.AudioDataShort;
import log.charter.util.FileChooseUtils;
import log.charter.util.RW;

public class SongFileHandler implements Initiable {
	public static final String vocalsFileName = "Vocals_RS2.xml";

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
	private ChartData chartData;
	private CharterFrame charterFrame;
	private CharterMenuBar charterMenuBar;
	private ChartTimeHandler chartTimeHandler;
	private ModeManager modeManager;
	private ProjectAudioHandler projectAudioHandler;
	private TextTab textTab;
	private UndoSystem undoSystem;

	@Override
	public void init() {
		new Thread(() -> {
			while (true) {
				try {
					Thread.sleep(Config.backupDelay * 1000);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}

				makeDefaultBackups(chartData);
			}
		}).start();
	}

	private void doWithLoadingDialog(final int steps, final Consumer<LoadingDialog> operation) {
		final LoadingDialog dialog = new LoadingDialog(charterFrame, steps);

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
		if (!askToSaveChanged()) {
			return;
		}

		final File songFile = FileChooseUtils.chooseMusicFile(charterFrame, Config.musicPath);
		if (songFile == null) {
			return;
		}

		final String songName = songFile.getName();
		final int dotIndex = songName.lastIndexOf('.');
		final String extension = songName.substring(dotIndex + 1).toLowerCase();
		if (!extension.equals("mp3") && !extension.equals("ogg")) {
			showPopup(charterFrame, Label.NOT_MP3_OGG);
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

		final AudioDataShort musicData = AudioDataShort.readFile(musicFile);
		if (musicData == null) {
			showPopup(charterFrame, Label.MUSIC_DATA_NOT_FOUND);
			return;
		}

		final SongChart songChart = new SongChart(musicData.msLength(), musicFileName);
		songChart.artistName(songData.getOrDefault("artist", ""));
		songChart.title(songData.getOrDefault("title", ""));
		songChart.albumName(songData.getOrDefault("album", ""));
		try {
			songChart.albumYear = Integer.valueOf(songData.getOrDefault("year", ""));
		} catch (final NumberFormatException e) {
			songChart.albumYear = null;
		}

		projectAudioHandler.setAudio(musicData);
		chartData.setNewSong(songFolder, songChart, "project.rscp");
		save();

		audioHandler.clear();
		audioHandler.setSong();
	}

	public AudioDataShort chooseMusicFile(final String startingDir) {
		final File musicFile = FileChooseUtils.chooseMusicFile(charterFrame, startingDir);
		if (musicFile == null) {
			return null;
		}

		return AudioDataShort.readFile(musicFile);
	}

	public File chooseSongFolder(final String audioFileDirectory, final String defaultFolderName) {
		File songFolder = null;

		while (songFolder == null) {
			final SongFolderSelectPane songFolderSelectPane = new SongFolderSelectPane(charterFrame, Config.songsPath,
					audioFileDirectory, defaultFolderName);
			songFolderSelectPane.setVisible(true);

			if (songFolderSelectPane.isAudioFolderChosen()) {
				return new File(audioFileDirectory);
			}

			String folderName = songFolderSelectPane.getFolderName();
			if (folderName == null || folderName.isBlank()) {
				return null;
			}

			songFolder = new File(Config.songsPath, folderName);

			if (songFolder.exists()) {
				folderName = askForInput(charterFrame, Label.FOLDER_EXISTS_CHOOSE_DIFFERENT, folderName);
				if (folderName == null) {
					return null;
				}

				songFolder = new File(Config.songsPath, folderName);
			}
		}
		songFolder.mkdir();

		return songFolder;
	}

	private ChartProject loadProjectFile(final LoadingDialog loadingDialog, final int progressAfter,
			final File projectFileChosen) {
		final String name = projectFileChosen.getName().toLowerCase();
		if (!name.endsWith(".rscp")) {
			showPopup(charterFrame, Label.UNSUPPORTED_FILE_TYPE);
			error("unsupported file: " + projectFileChosen.getName());
			return null;
		}
		final ChartProject project;
		try {
			project = readChartProject(RW.read(projectFileChosen));
			if (project.chartFormatVersion > 2) {
				Logger.error("project has wrong version");
				showPopup(charterFrame, Label.PROJECT_IS_NEWER_VERSION);
				return null;
			}
		} catch (final Exception e) {
			Logger.error("Error when reading project", e);
			showPopup(charterFrame, Label.MISSING_ARRANGEMENT_FILE, projectFileChosen.getAbsolutePath());
			return null;
		}

		loadingDialog.setProgress(progressAfter, Label.LOADING_MUSIC_FILE.label());

		return project;
	}

	private AudioDataShort loadMusicData(final LoadingDialog loadingDialog, final int progressAfter,
			final ChartProject project, final String dir) {
		final AudioDataShort musicData = AudioDataShort.readFile(new File(dir, project.musicFileName));
		if (musicData == null) {
			showPopup(charterFrame, Label.WRONG_MUSIC_FILE);
			return null;
		}

		loadingDialog.setProgress(progressAfter, Label.LOADING_ARRANGEMENTS.label());

		return musicData;
	}

	private void openInternal(final LoadingDialog loadingDialog, final String path) {
		loadingDialog.setProgress(0, Label.LOADING_PROJECT_FILE.label());

		final List<String> filesToBackup = new ArrayList<>();
		final File projectFileChosen = new File(path);
		final ChartProject project = loadProjectFile(loadingDialog, 1, projectFileChosen);
		if (project == null) {
			return;
		}
		filesToBackup.add(projectFileChosen.getName());
		filesToBackup.addAll(project.arrangementFiles);
		filesToBackup.add(vocalsFileName);

		final String dir = projectFileChosen.getParent() + File.separator;
		final AudioDataShort musicData = loadMusicData(loadingDialog, 2, project, dir);
		if (musicData == null) {
			return;
		}

		final SongChart songChart;
		try {
			songChart = new SongChart(project, dir);
		} catch (final Exception e) {
			showPopup(charterFrame, Label.COULDNT_LOAD_PROJECT, e.getMessage());
			return;
		}

		makeBackups(dir, filesToBackup);

		projectAudioHandler.setAudio(musicData);
		chartTimeHandler.nextTime(project.time);
		chartData.setSong(dir, songChart, projectFileChosen.getName(), project.editMode, project.arrangement,
				project.level);
		textTab.setText(project.text);

		loadingDialog.setProgress(3, Label.LOADING_DONE.label());
	}

	public void open(final String path) {
		doWithLoadingDialog(3, (dialog) -> openInternal(dialog, path));
	}

	public void open() {
		if (!askToSaveChanged()) {
			return;
		}

		String startingDir = chartData.path;
		if (startingDir.isEmpty() || !new File(startingDir).exists()) {
			startingDir = Config.songsPath;
		}

		final File projectFileChosen = chooseFile(charterFrame, startingDir, new String[] { ".rscp" },
				new String[] { Label.CHART_PROJECT.label() });
		if (projectFileChosen == null) {
			return;
		}

		open(projectFileChosen.getAbsolutePath());
	}

	public void openAudioFile() {
		final File musicFile = FileChooseUtils.chooseMusicFile(charterFrame, chartData.path);
		if (musicFile == null) {
			return;
		}

		final AudioDataShort musicData = AudioDataShort.readFile(musicFile);
		if (musicData != null) {
			projectAudioHandler.setAudio(musicData);
			chartData.songChart.musicFileName = musicFile.getName();
		}
	}

	public void openSongWithImportFromArrangementXML() {
		final File songFile = FileChooseUtils.chooseMusicFile(charterFrame, Config.songsPath);
		if (songFile == null) {
			return;
		}

		final String songName = songFile.getName();
		final int dotIndex = songName.lastIndexOf('.');
		final String extension = songName.substring(dotIndex + 1).toLowerCase();
		if (!extension.equals("mp3") && !extension.equals("ogg")) {
			showPopup(charterFrame, Label.NOT_MP3_OGG);
			return;
		}

		LoadingDialog loadingDialog = new LoadingDialog(charterFrame, 1);
		loadingDialog.setProgress(0, Label.LOADING_MUSIC_FILE.label());
		final AudioDataShort musicData = AudioDataShort.readFile(songFile);
		if (musicData == null) {
			loadingDialog.dispose();
			showPopup(charterFrame, Label.MUSIC_FILE_COULDNT_BE_LOADED);
			return;
		}
		loadingDialog.dispose();

		final String dir = songFile.getParent() + File.separator;
		final File arrangementFile = FileChooseUtils.chooseFile(charterFrame, dir, new String[] { ".xml" },
				new String[] { Label.RS_ARRANGEMENT_FILE.label() });
		if (arrangementFile == null) {
			return;
		}

		loadingDialog = new LoadingDialog(charterFrame, 1);
		loadingDialog.setProgress(0, Label.LOADING_ARRANGEMENTS.label());
		final SongArrangement songArrangement = SongArrangementXStreamHandler.readSong(RW.read(arrangementFile));
		final SongChart songChart = RSXMLToSongChart.makeSongChartForArrangement(songName, songArrangement);

		projectAudioHandler.setAudio(musicData);
		chartTimeHandler.nextTime(0);
		chartData.setSong(dir, songChart, "project.rscp", EditMode.GUITAR, 0, 0);
		loadingDialog.dispose();

		save();
	}

	public void importRSArrangementXML() {
		final String dir = chartData.isEmpty ? Config.songsPath : chartData.path;
		final File arrangementFile = FileChooseUtils.chooseFile(charterFrame, dir, new String[] { ".xml" },
				new String[] { Label.RS_ARRANGEMENT_FILE.label() });
		if (arrangementFile == null) {
			return;
		}

		try {
			final SongArrangement songArrangement = SongArrangementXStreamHandler.readSong(RW.read(arrangementFile));
			final Arrangement arrangementChart = RSXMLToArrangement.toArrangement(songArrangement,
					chartData.songChart.beatsMap.beats);
			chartData.songChart.arrangements.add(arrangementChart);

			charterMenuBar.refreshMenus();

			modeManager.setArrangement(chartData.songChart.arrangements.size() - 1);
			save();
		} catch (final Exception e) {
			Logger.error("Couldn't load arrangement", e);
			showPopup(charterFrame, Label.COULDNT_LOAD_ARRANGEMENT, e.getMessage());
		}
	}

	public void importRSVocalsArrangementXML() {
		final String dir = chartData.isEmpty ? Config.songsPath : chartData.path;
		final File arrangementFile = FileChooseUtils.chooseFile(charterFrame, dir, new String[] { ".xml" },
				new String[] { Label.RS_ARRANGEMENT_FILE.label() });
		if (arrangementFile == null) {
			return;
		}

		try {
			final ArrangementVocals vocals = VocalsXStreamHandler.readVocals(RW.read(arrangementFile));
			chartData.songChart.vocals = new Vocals(vocals);
			save();
		} catch (final Exception e) {
			Logger.error("Couldn't load arrangement", e);
			showPopup(charterFrame, Label.COULDNT_LOAD_ARRANGEMENT, e.getMessage());
		}
	}

	private static String generateArrangementFileName(final int id, final Arrangement arrangementChart) {
		return id + "_" + arrangementChart.arrangementType.name() + "_" + arrangementChart.arrangementSubtype.name()
				+ "_RS2.xml";
	}

	private void saveRSXML() {
		final File dir = new File(chartData.path, "RS XML");
		dir.mkdirs();
		for (final File f : dir.listFiles()) {
			if (f.isFile()) {
				f.delete();
			}
		}

		int id = 1;
		for (final Arrangement arrangement : chartData.songChart.arrangements) {
			final String arrangementFileName = generateArrangementFileName(id, arrangement);
			final SongArrangement songArrangement = new SongArrangement(projectAudioHandler.getAudio().msLength(),
					chartData.songChart, arrangement);
			RW.write(new File(dir, arrangementFileName), SongArrangementXStreamHandler.saveSong(songArrangement));
			id++;
		}

		if (!chartData.songChart.vocals.vocals.isEmpty()) {
			RW.write(new File(dir, vocalsFileName), saveVocals(new ArrangementVocals(chartData.songChart.vocals)),
					"UTF-8");
		}
	}

	public void save() {
		if (chartData.isEmpty) {
			return;
		}

		arrangementFixer.fixArrangements();
		if (!arrangementValidator.validate()) {
			return;
		}

		final ChartProject project = new ChartProject(chartTimeHandler.time(), modeManager.getMode(), chartData,
				chartData.songChart, textTab.getText());
		RW.write(new File(chartData.path, chartData.projectFileName), writeChartProject(project));
		saveRSXML();

		undoSystem.onSave();
	}

	public void saveAs() {
		if (chartData.isEmpty) {
			return;
		}

		arrangementFixer.fixArrangements();
		if (!arrangementValidator.validate()) {
			return;
		}

		final File newDir = FileChooseUtils.chooseDirectory(charterFrame, chartData.path);
		if (newDir == null) {
			return;
		}

		chartData.path = newDir.getAbsolutePath();
		Config.lastDir = chartData.path;
		Config.lastPath = new File(chartData.path, chartData.projectFileName).getAbsolutePath();
		Config.markChanged();
		Config.save();

		save();
	}

	public boolean askToSaveChanged() {
		if (undoSystem.isSaved()) {
			return true;
		}

		final ConfirmAnswer answer = askYesNoCancel(charterFrame, Label.UNSAVED_CHANGES_POPUP,
				Label.UNSAVED_CHANGES_MESSAGE);

		switch (answer) {
			case YES:
				save();
			case NO:
				return true;
			default:
				return false;
		}
	}
}
