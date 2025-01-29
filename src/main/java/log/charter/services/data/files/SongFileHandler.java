package log.charter.services.data.files;

import static log.charter.gui.components.utils.ComponentUtils.askYesNoCancel;
import static log.charter.gui.components.utils.ComponentUtils.showPopup;
import static log.charter.io.rs.xml.vocals.VocalsXStreamHandler.saveVocals;
import static log.charter.io.rsc.xml.ChartProjectXStreamHandler.writeChartProject;
import static log.charter.util.FileChooseUtils.chooseFile;

import java.io.File;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.Arrangement;
import log.charter.data.song.SongChart;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.tabs.TextTab;
import log.charter.gui.components.tabs.chordEditor.ChordTemplatesEditorTab;
import log.charter.gui.components.utils.ComponentUtils.ConfirmAnswer;
import log.charter.io.rs.xml.RSXMLToSongChart;
import log.charter.io.rs.xml.song.SongArrangement;
import log.charter.io.rs.xml.song.SongArrangementXStreamHandler;
import log.charter.io.rs.xml.vocals.ArrangementVocals;
import log.charter.io.rsc.xml.ChartProject;
import log.charter.services.audio.AudioHandler;
import log.charter.services.data.ChartTimeHandler;
import log.charter.services.data.ProjectAudioHandler;
import log.charter.services.data.fixers.ArrangementFixer;
import log.charter.services.editModes.EditMode;
import log.charter.services.editModes.ModeManager;
import log.charter.sound.data.AudioDataShort;
import log.charter.util.FileChooseUtils;
import log.charter.util.RW;

public class SongFileHandler {
	public static final String vocalsFileName = "Vocals_RS2.xml";

	private ArrangementFixer arrangementFixer;
	private AudioHandler audioHandler;
	private ChartData chartData;
	private CharterFrame charterFrame;
	private ChartTimeHandler chartTimeHandler;
	private ChordTemplatesEditorTab chordTemplatesEditorTab;
	private ExistingProjectImporter existingProjectImporter;
	private ModeManager modeManager;
	private ProjectAudioHandler projectAudioHandler;
	private TextTab textTab;
	private UndoSystem undoSystem;

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

		existingProjectImporter.open(projectFileChosen.getAbsolutePath());
	}

	public void createSongWithImportFromArrangementXML() {
		final String fileChooseDir = modeManager.getMode() == EditMode.EMPTY ? Config.songsPath : chartData.path;
		final File songFile = FileChooseUtils.chooseMusicFile(charterFrame, fileChooseDir);
		if (songFile == null) {
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
		final SongArrangement songArrangement = SongArrangementXStreamHandler.readSong(arrangementFile);
		final SongChart songChart = RSXMLToSongChart.makeSongChartForArrangement(songFile.getName(), songArrangement);

		chartTimeHandler.nextTime(0);
		chartData.setSong(dir, songChart, "project.rscp", EditMode.GUITAR, 0, 0);
		projectAudioHandler.setAudio(musicData);
		loadingDialog.dispose();

		audioHandler.clear();
		chordTemplatesEditorTab.refreshTemplates();

		save();
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
			final SongArrangement songArrangement = new SongArrangement((int) projectAudioHandler.getAudio().msLength(),
					chartData.songChart, arrangement);
			RW.write(new File(dir, arrangementFileName), SongArrangementXStreamHandler.saveSong(songArrangement));
			id++;
		}

		if (!chartData.currentVocals().vocals.isEmpty()) {
			RW.write(new File(dir, vocalsFileName),
					saveVocals(new ArrangementVocals(chartData.beats(), chartData.songChart.vocals)), "UTF-8");
		}
	}

	public void save() {
		if (modeManager.getMode() == EditMode.EMPTY) {
			return;
		}

		arrangementFixer.fixArrangements();

		final ChartProject project = new ChartProject(chartTimeHandler.time(), modeManager.getMode(), chartData,
				chartData.songChart, textTab.getText());
		RW.write(new File(chartData.path, chartData.projectFileName), writeChartProject(project));
		saveRSXML();

		undoSystem.onSave();
	}

	public void saveAs() {
		if (modeManager.getMode() == EditMode.EMPTY) {
			return;
		}

		arrangementFixer.fixArrangements();

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
