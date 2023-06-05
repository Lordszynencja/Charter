package log.charter.data;

import java.io.File;

import javax.swing.JScrollBar;

import log.charter.data.config.Config;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.modes.EditMode;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.handlers.AudioHandler;
import log.charter.gui.menuHandlers.CharterMenuBar;
import log.charter.song.ArrangementChart;
import log.charter.song.Level;
import log.charter.song.SongChart;
import log.charter.sound.MusicData;

public class ChartData {
	public String path = Config.lastPath;
	public String projectFileName = "project.rscp";
	public boolean isEmpty = true;
	public SongChart songChart = null;
	public MusicData music = new MusicData(new byte[0], 44100);

	public int currentArrangement = 0;
	public int currentLevel = 0;
	public int time = 0;
	public int nextTime = 0;

	private AudioHandler audioHandler;
	private CharterMenuBar charterMenuBar;
	private ModeManager modeManager;
	private JScrollBar scrollBar;
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	public void init(final AudioHandler audioHandler, final CharterMenuBar charterMenuBar,
			final ModeManager modeManager, final JScrollBar scrollBar, final SelectionManager selectionManager,
			final UndoSystem undoSystem) {
		this.audioHandler = audioHandler;
		this.charterMenuBar = charterMenuBar;
		this.modeManager = modeManager;
		this.scrollBar = scrollBar;
		this.selectionManager = selectionManager;
		this.undoSystem = undoSystem;
	}

	public void changeDifficulty(final int newDiff) {
		currentLevel = newDiff;
	}

	public void setNewSong(final File songFolder, final SongChart song, final MusicData musicData,
			final String projectFileName) {
		setSong(songFolder.getAbsolutePath(), song, musicData, projectFileName, EditMode.TEMPO_MAP, 0, 0, 0);
	}

	public void setSong(final String dir, final SongChart song, final MusicData musicData, final String projectFileName,
			final EditMode editMode, final int arrangement, final int level, final int time) {
		currentArrangement = arrangement;
		this.time = time;
		nextTime = time;
		isEmpty = false;

		songChart = song;
		music = musicData;

		audioHandler.clear();
		selectionManager.clear();
		changeDifficulty(level);
		modeManager.editMode = editMode;

		charterMenuBar.refreshMenus();
		scrollBar.setMaximum(musicData.msLength());
		scrollBar.setValue(time);

		path = dir;
		this.projectFileName = projectFileName;
		Config.lastDir = path;
		Config.lastPath = new File(path, projectFileName).getAbsolutePath();
		Config.markChanged();

		selectionManager.clear();
		undoSystem.clear();
	}

	public void undo() {
		undoSystem.undo();
	}

	public int currentStrings() {
		if (modeManager.editMode != EditMode.GUITAR) {
			return -1;
		}
		return getCurrentArrangement().tuning.strings;
	}

	public ArrangementChart getCurrentArrangement() {
		if (modeManager.editMode != EditMode.GUITAR) {
			return null;
		}
		if (songChart.arrangements.isEmpty()) {
			return null;
		}

		return songChart.arrangements.get(currentArrangement);
	}

	public Level getCurrentArrangementLevel() {
		if (modeManager.editMode != EditMode.GUITAR) {
			return null;
		}
		return songChart.arrangements.get(currentArrangement).levels.get(currentLevel);
	}

	public void setNextTime(final int t) {
		nextTime = t;
		if (nextTime < 0) {
			nextTime = 0;
		}
		if (nextTime > music.msLength()) {
			nextTime = music.msLength();
		}
	}
}
