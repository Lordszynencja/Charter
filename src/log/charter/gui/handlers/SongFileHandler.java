package log.charter.gui.handlers;

import static log.charter.io.Logger.error;
import static log.charter.io.rs.xml.vocals.VocalsXStreamHandler.saveVocals;
import static log.charter.io.rsc.xml.RocksmithChartProjectXStreamHandler.readProject;
import static log.charter.io.rsc.xml.RocksmithChartProjectXStreamHandler.saveProject;
import static log.charter.util.FileChooseUtils.chooseFile;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import helliker.id3.MP3File;
import log.charter.data.ChartData;
import log.charter.data.Config;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.io.rs.xml.song.SongArrangement;
import log.charter.io.rs.xml.song.SongArrangementXStreamHandler;
import log.charter.io.rs.xml.vocals.ArrangementVocals;
import log.charter.io.rs.xml.vocals.VocalsXStreamHandler;
import log.charter.io.rsc.xml.RocksmithChartProject;
import log.charter.song.ArrangementChart;
import log.charter.song.SongChart;
import log.charter.song.Vocals;
import log.charter.sound.MusicData;
import log.charter.util.FileChooseUtils;
import log.charter.util.RW;

public class SongFileHandler {
	private static Map<String, String> extractNewSongData(final String path) {
		final Map<String, String> data = new HashMap<>();
		try {
			final MP3File mp3File = new MP3File(path);

			try {
				data.put("artist", mp3File.getArtist());
			} catch (final Exception e) {
				data.put("artist", "");
				error("Couldn't get artist from mp3 tags data", e);
			}
			try {
				data.put("title", mp3File.getTitle());
			} catch (final Exception e) {
				data.put("title", "");
				error("Couldn't get title from mp3 tags data", e);
			}
			try {
				data.put("album", mp3File.getAlbum());
			} catch (final Exception e) {
				data.put("album", "");
				error("Couldn't get album from mp3 tags data", e);
			}
			try {
				data.put("year", mp3File.getYear());
			} catch (final Exception e) {
				data.put("year", "");
				error("Couldn't get year from mp3 tags data", e);
			}
		} catch (final Exception e) {
			error("Couldn't get mp3 tags data", e);
		}

		return data;
	}

	private static void makeBackups(final String dir, final List<String> fileNames) {
		final String backupDir = dir//
				+ "backups" + File.separator//
				+ new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()) + File.separator;
		new File(backupDir).mkdirs();

		for (final String fileName : fileNames) {
			final File f = new File(dir + fileName);
			if (f.exists()) {
				RW.writeB(backupDir + fileName, RW.readB(f));
			}
		}
	}

	private ChartData data;
	private CharterFrame frame;
	private UndoSystem undoSystem;

	public void init(final ChartData data, final CharterFrame frame, final UndoSystem undoSystem) {
		this.data = data;
		this.frame = frame;
		this.undoSystem = undoSystem;
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
			frame.showPopup("Not an Mp3 or Ogg file!");
			return;
		}

		final Map<String, String> songData = extractNewSongData(songFile.getAbsolutePath());
		String folderName = songName.substring(0, songName.lastIndexOf('.'));

		folderName = frame.showInputDialog("Choose folder name", folderName);
		if (folderName == null) {
			return;
		}

		File f = new File(Config.songsPath + "/" + folderName);
		while (f.exists()) {
			folderName = frame.showInputDialog("Given folder already exists, choose different name", folderName);
			if (folderName == null) {
				return;
			}

			f = new File(Config.songsPath + "/" + folderName);
		}
		f.mkdir();

		final String songDir = f.getAbsolutePath();
		final String musicFileName = "guitar." + extension;
		final String musicPath = songDir + "/" + musicFileName;
		RW.writeB(musicPath, RW.readB(songFile));

		final MusicData musicData = MusicData.readFile(musicPath);
		if (musicData == null) {
			frame.showPopup(
					"Music file not found in song folder, something went wrong with copying or the file is invalid");
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

		data.setSong(songDir, songChart, musicData, "project.rscp");
		save();
	}

	public MusicData chooseMusicFile(final String startingDir) {
		final File musicFile = FileChooseUtils.chooseMusicFile(frame, startingDir);
		if (musicFile == null) {
			return null;
		}

		return MusicData.readFile(musicFile.getAbsolutePath());
	}

	public void open() {
		if (!frame.checkChanged()) {
			return;
		}

		String startingDir = data.path;
		if (!new File(startingDir).exists()) {
			startingDir = Config.songsPath;
		}

		final File projectFileChosen = chooseFile(frame, startingDir, new String[] { ".rscp" },
				new String[] { "Rocksmith Chart Project" });
		if (projectFileChosen == null) {
			return;
		}

		final String dir = projectFileChosen.getParent() + File.separator;
		final String name = projectFileChosen.getName().toLowerCase();

		if (!name.endsWith(".rscp")) {
			frame.showPopup("This file type is not supported");
			error("unsupported file: " + projectFileChosen.getName());
			return;
		}

		final RocksmithChartProject project = readProject(RW.read(projectFileChosen));
		if (project.chartFormatVersion > 1) {
			frame.showPopup("Project is newer version than program handles.");
			return;
		}

		MusicData musicData = MusicData.readFile(dir + project.musicFileName);
		if (musicData == null) {
			frame.showPopup(
					"Music file " + project.musicFileName + " not found in song folder, please choose new file.");

			final File musicFile = FileChooseUtils.chooseMusicFile(frame, startingDir);
			if (musicFile == null) {
				frame.showPopup("Operation canceled.");
				return;
			}

			musicData = MusicData.readFile(musicFile.getAbsolutePath());
			if (musicData == null) {
				frame.showPopup("Wrong music file.");
				return;
			}
		}

		final SongChart songChart = new SongChart(musicData.msLength(), project, dir);

		final List<String> filesToBackup = new ArrayList<>();
		filesToBackup.add(projectFileChosen.getName());
		filesToBackup.addAll(project.arrangementFiles);
		filesToBackup.add("Vocals_RS2.xml");
		makeBackups(dir, filesToBackup);

		Config.lastPath = dir;
		Config.save();
		data.setSong(dir, songChart, musicData, projectFileChosen.getName());
	}

	public void openAudioFile() {
		final MusicData musicData = chooseMusicFile(data.path);
		if (musicData != null) {
			data.music = musicData;
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
			frame.showPopup("Not an Mp3 or Ogg file!");
			return;
		}

		final MusicData musicData = MusicData.readFile(songFile.getAbsolutePath());
		if (musicData == null) {
			frame.showPopup("Music file couldn't be loaded");
			return;
		}

		final String dir = songFile.getParent() + File.separator;
		final File arrangementFile = FileChooseUtils.chooseFile(frame, dir, new String[] { ".xml" },
				new String[] { "RS arrangmenet file (XML)" });
		if (arrangementFile == null) {
			return;
		}

		final SongArrangement songArrangement = SongArrangementXStreamHandler.readSong(RW.read(arrangementFile));
		final SongChart songChart = new SongChart(musicData.msLength(), songName, songArrangement);

		data.setSong(dir, songChart, musicData, "project.rscp");
		save();
	}

	public void importRSArrangementXML() {
		final String dir = data.isEmpty ? Config.songsPath : data.path;
		final File arrangementFile = FileChooseUtils.chooseFile(frame, dir, new String[] { ".xml" },
				new String[] { "RS arrangmenet file (XML)" });
		if (arrangementFile == null) {
			return;
		}

		try {
			final SongArrangement songArrangement = SongArrangementXStreamHandler.readSong(RW.read(arrangementFile));
			final ArrangementChart arrangementChart = new ArrangementChart(songArrangement);
			data.songChart.arrangements.add(arrangementChart);
			save();
		} catch (final Exception e) {
			frame.showPopup("Couldn't load arrangement:\n" + e.getMessage());
		}
	}

	public void importRSVocalsArrangementXML() {
		final String dir = data.isEmpty ? Config.songsPath : data.path;
		final File arrangementFile = FileChooseUtils.chooseFile(frame, dir, new String[] { ".xml" },
				new String[] { "RS arrangmenet file (XML)" });
		if (arrangementFile == null) {
			return;
		}

		try {
			final ArrangementVocals vocals = VocalsXStreamHandler.readVocals(RW.read(arrangementFile));
			data.songChart.vocals = new Vocals(vocals);
			save();
		} catch (final Exception e) {
			frame.showPopup("Couldn't load arrangement:\n" + e.getMessage());
		}
	}

	public void importSong() {
		if (!frame.checkChanged()) {
			return;
		}

		final File songFile = FileChooseUtils.chooseMusicFile(frame, Config.lastPath);
		if (songFile == null) {
			return;
		}

		final String songName = songFile.getName();
		final int dotIndex = songName.lastIndexOf('.');
		final String extension = songName.substring(dotIndex + 1).toLowerCase();
		if (!extension.equals("mp3") && !extension.equals("ogg")) {
			frame.showPopup("Not an Mp3 or Ogg file!");
			return;
		}

		final Map<String, String> songData = extractNewSongData(songFile.getAbsolutePath());
		String folderName = songName.substring(0, songName.lastIndexOf('.'));

		folderName = frame.showInputDialog("Choose folder name", folderName);
		if (folderName == null) {
			return;
		}

		File f = new File(Config.songsPath + "/" + folderName);
		while (f.exists()) {
			folderName = frame.showInputDialog("Given folder already exists, choose different name", folderName);
			if (folderName == null) {
				return;
			}

			f = new File(Config.songsPath + "/" + folderName);
		}
		f.mkdir();

		final String songDir = f.getAbsolutePath();
		final String musicFileName = "guitar." + extension;
		final String musicPath = songDir + "/" + musicFileName;
		RW.writeB(musicPath, RW.readB(songFile));

		final MusicData musicData = MusicData.readFile(musicPath);
		if (musicData == null) {
			frame.showPopup(
					"Music file not found in song folder, something went wrong with copying or the file is invalid");
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

		data.setSong(songDir, songChart, musicData, "project.rscp");
		save();
	}

	public void save() {
		if (data.isEmpty) {
			return;
		}

		final RocksmithChartProject project = new RocksmithChartProject(data.songChart);

		final int id = 1;
		for (final ArrangementChart arrangementChart : data.songChart.arrangements) {
			final String arrangementFileName = id + "_" + arrangementChart.getTypeName() + "_RS2.xml";
			project.arrangementFiles.add(arrangementFileName);
			final SongArrangement songArrangement = new SongArrangement(data.songChart, arrangementChart);
			RW.write(data.path + arrangementFileName, SongArrangementXStreamHandler.saveSong(songArrangement));
		}

		if (!data.songChart.vocals.vocals.isEmpty()) {
			RW.write(data.path + "Vocals_RS2.xml", saveVocals(new ArrangementVocals(data.songChart.vocals)));
		}

		RW.write(data.path + data.projectFileName, saveProject(project));
		undoSystem.onSave();

		Config.save();
	}

	public void saveAs() {
		if (data.isEmpty) {
			return;
		}

		final File newDir = FileChooseUtils.chooseDirectory(frame, data.path);
		if (newDir == null) {
			return;
		}

		data.path = newDir.getAbsolutePath();
		save();
		Config.save();
	}
}
