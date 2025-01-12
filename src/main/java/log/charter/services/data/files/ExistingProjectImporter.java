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
import log.charter.gui.components.tabs.chordEditor.ChordTemplatesEditorTab;
import log.charter.io.Logger;
import log.charter.io.rsc.xml.ChartProject;
import log.charter.services.audio.AudioHandler;
import log.charter.services.data.ChartTimeHandler;
import log.charter.services.data.ProjectAudioHandler;
import log.charter.sound.data.AudioDataShort;

public class ExistingProjectImporter {
	private AudioHandler audioHandler;
	private ChartData chartData;
	private CharterFrame charterFrame;
	private ChartTimeHandler chartTimeHandler;
	private ChordTemplatesEditorTab chordTemplatesEditorTab;
	private ProjectAudioHandler projectAudioHandler;
	private TextTab textTab;

	private ChartProject loadProjectFile(final File projectFileChosen) {
		final String name = projectFileChosen.getName().toLowerCase();
		if (!name.endsWith(".rscp")) {
			Logger.error("unsupported file: " + projectFileChosen.getName());
			showPopup(charterFrame, Label.UNSUPPORTED_FILE_TYPE);
			return null;
		}

		final ChartProject project;
		try {
			project = readChartProject(projectFileChosen);
			if (project.chartFormatVersion > 3) {
				Logger.error("project has wrong version " + project.chartFormatVersion);
				showPopup(charterFrame, Label.PROJECT_IS_NEWER_VERSION);
				return null;
			}
		} catch (final Exception e) {
			Logger.error("Error when reading project", e);
			showPopup(charterFrame, Label.MISSING_ARRANGEMENT_FILE, projectFileChosen.getAbsolutePath());
			return null;
		}

		return project;
	}

	private AudioDataShort loadMusicData(final ChartProject project, final String dir) {
		final AudioDataShort musicData = AudioDataShort.readFile(new File(dir, project.musicFileName));
		if (musicData == null) {
			showPopup(charterFrame, Label.WRONG_MUSIC_FILE, project.musicFileName);
			return null;
		}

		return musicData;
	}

	private void openInternal(final LoadingDialog loadingDialog, final String path) {
		loadingDialog.setProgress(0, Label.LOADING_PROJECT_FILE.label());

		final List<String> filesToBackup = new ArrayList<>();
		final File projectFileChosen = new File(path);
		final ChartProject project = loadProjectFile(projectFileChosen);
		if (project == null) {
			return;
		}
		loadingDialog.addProgress(Label.LOADING_MUSIC_FILE);

		filesToBackup.add(projectFileChosen.getName());
		filesToBackup.addAll(project.arrangementFiles);
		filesToBackup.add(SongFileHandler.vocalsFileName);

		final String dir = projectFileChosen.getParent() + File.separator;
		final AudioDataShort musicData = loadMusicData(project, dir);
		if (musicData == null) {
			return;
		}
		loadingDialog.addProgress(Label.LOADING_ARRANGEMENTS);

		final SongChart songChart;
		try {
			songChart = new SongChart(project, dir);
		} catch (final Exception e) {
			showPopup(charterFrame, Label.COULDNT_LOAD_PROJECT, e.getMessage());
			return;
		}

		makeBackups(dir, filesToBackup);

		chartData.setSong(dir, songChart, projectFileChosen.getName(), project.editMode, project.arrangement,
				project.level);
		chartTimeHandler.nextTime(project.time);
		projectAudioHandler.setAudio(musicData);
		textTab.setText(project.text);

		audioHandler.clear();
		chordTemplatesEditorTab.refreshTemplates();

		loadingDialog.addProgress(Label.LOADING_DONE);
	}

	public void open(final String path) {
		doWithLoadingDialog(charterFrame, 3, dialog -> openInternal(dialog, path), "open " + path);
	}
}
