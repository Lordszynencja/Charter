package log.charter.data;

import static log.charter.data.config.Config.maxStrings;

import java.io.File;

import log.charter.data.config.Config;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.modes.EditMode;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.handlers.AudioHandler;
import log.charter.gui.menuHandlers.CharterMenuBar;
import log.charter.song.Arrangement;
import log.charter.song.Level;
import log.charter.song.SongChart;

public class ChartData {
	public String path = Config.lastDir;
	public String projectFileName = "project.rscp";
	public boolean isEmpty = true;
	public SongChart songChart = null;

	public int currentArrangement = 0;
	public int currentLevel = 0;

	private AudioHandler audioHandler;
	private CharterFrame charterFrame;
	private CharterMenuBar charterMenuBar;
	private ModeManager modeManager;
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	public void setNewSong(final File songFolder, final SongChart song, final String projectFileName) {
		setSong(songFolder.getAbsolutePath(), song, projectFileName, EditMode.TEMPO_MAP, 0, 0);
	}

	public void setSong(final String dir, final SongChart song, final String projectFileName, final EditMode editMode,
			final int arrangement, final int level) {
		currentArrangement = arrangement;
		isEmpty = false;

		songChart = song;

		selectionManager.clear();
		currentLevel = level;
		modeManager.setMode(editMode);

		charterMenuBar.refreshMenus();
		charterFrame.updateSizes();

		path = dir;
		this.projectFileName = projectFileName;
		Config.lastDir = path;
		Config.lastPath = new File(path, projectFileName).getAbsolutePath();
		Config.markChanged();

		selectionManager.clear();
		undoSystem.clear();

		audioHandler.clear();
		audioHandler.setSong();
	}

	public void undo() {
		undoSystem.undo();
	}

	public int currentStrings() {
		if (modeManager.getMode() != EditMode.GUITAR) {
			return maxStrings;
		}

		return getCurrentArrangement().tuning.strings();
	}

	public Arrangement getCurrentArrangement() {
		if (songChart == null || currentArrangement < 0 || currentArrangement >= songChart.arrangements.size()) {
			return new Arrangement();
		}

		return songChart.arrangements.get(currentArrangement);
	}

	public Level getCurrentArrangementLevel() {
		return getCurrentArrangement().getLevel(currentLevel);
	}
}
