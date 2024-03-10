package log.charter.services.data.files;

import static log.charter.gui.components.utils.ComponentUtils.showPopup;
import static log.charter.io.rsc.xml.ChartProjectXStreamHandler.readChartProject;
import static log.charter.services.data.files.LoadingDialog.doWithLoadingDialog;
import static log.charter.services.data.files.SongFilesBackuper.makeBackups;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.SongChart;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.tabs.TextTab;
import log.charter.io.Logger;
import log.charter.io.rsc.xml.ChartProject;
import log.charter.services.AudioHandler;
import log.charter.services.data.ChartTimeHandler;
import log.charter.services.data.ProjectAudioHandler;
import log.charter.sound.data.AudioDataShort;
import log.charter.util.RW;

public class ExistingProjectImporter {
	private AudioHandler audioHandler;
	private ChartData chartData;
	private CharterFrame charterFrame;
	private ChartTimeHandler chartTimeHandler;
	private ProjectAudioHandler projectAudioHandler;
	private TextTab textTab;

	private ChartProject loadProjectFile(final LoadingDialog loadingDialog, final int progressAfter,
			final File projectFileChosen) {
		final String name = projectFileChosen.getName().toLowerCase();
		if (!name.endsWith(".rscp")) {
			Logger.error("unsupported file: " + projectFileChosen.getName());
			showPopup(charterFrame, Label.UNSUPPORTED_FILE_TYPE);
			return null;
		}

		final ChartProject project;
		try {
			project = readChartProject(RW.read(projectFileChosen));
			if (project.chartFormatVersion > 2) {
				Logger.error("project has wrong version " + project.chartFormatVersion);
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
		filesToBackup.add(SongFileHandler.vocalsFileName);

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

		chartTimeHandler.nextTime(project.time);
		chartData.setSong(dir, songChart, projectFileChosen.getName(), project.editMode, project.arrangement,
				project.level);
		projectAudioHandler.setAudio(musicData, !project.musicFileName.equals("song.ogg"));
		textTab.setText(project.text);

		audioHandler.clear();
		audioHandler.setSong();

		loadingDialog.setProgress(3, Label.LOADING_DONE.label());
	}

	public void open(final String path) {
		doWithLoadingDialog(charterFrame, 3, dialog -> openInternal(dialog, path), "open " + path);
	}
}
