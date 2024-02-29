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
import log.charter.sound.data.MusicDataShort;

public class ChartData {
	public String path = Config.lastDir;
	public String projectFileName = "project.rscp";
	public boolean isEmpty = true;
	public SongChart songChart = null;
	public MusicDataShort music = new MusicDataShort();

	public int currentArrangement = 0;
	public int currentLevel = 0;
	public int time = 0;
	public int nextTime = 0;

	private CharterFrame frame;
	private AudioHandler audioHandler;
	private CharterMenuBar charterMenuBar;
	private ModeManager modeManager;
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	public void init(final CharterFrame frame, final AudioHandler audioHandler, final CharterMenuBar charterMenuBar,
			final ModeManager modeManager, final SelectionManager selectionManager, final UndoSystem undoSystem) {
		this.frame = frame;
		this.audioHandler = audioHandler;
		this.charterMenuBar = charterMenuBar;
		this.modeManager = modeManager;
		this.selectionManager = selectionManager;
		this.undoSystem = undoSystem;
	}

	public void setNewSong(final File songFolder, final SongChart song, final MusicDataShort musicData,
			final String projectFileName) {
		setSong(songFolder.getAbsolutePath(), song, musicData, projectFileName, EditMode.TEMPO_MAP, 0, 0, 0);
	}

	public void setSong(final String dir, final SongChart song, final MusicDataShort musicData,
			final String projectFileName, final EditMode editMode, final int arrangement, final int level,
			final int time) {
		currentArrangement = arrangement;
		this.time = time;
		nextTime = time;
		isEmpty = false;

		songChart = song;
		music = musicData;

		selectionManager.clear();
		currentLevel = level;
		modeManager.setMode(editMode);

		charterMenuBar.refreshMenus();
		frame.updateEditAreaSizes();

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

		return getCurrentArrangement().tuning.strings;
	}

	public Arrangement getCurrentArrangement() {
		if (modeManager.getMode() != EditMode.GUITAR) {
			return null;
		}
		if (songChart.arrangements.isEmpty()) {
			return null;
		}

		return songChart.arrangements.get(currentArrangement);
	}

	public Level getCurrentArrangementLevel() {
		if (modeManager.getMode() != EditMode.GUITAR) {
			return null;
		}

		return songChart.arrangements.get(currentArrangement).getLevel(currentLevel);
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
